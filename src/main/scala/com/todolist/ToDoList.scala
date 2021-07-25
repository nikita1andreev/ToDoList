package com.todolist


import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.util.Success

object Server extends App{
  val config =ConfigFactory.load("application")
  val actorSystem = ActorSystem("serversystem", config)
  val server: ActorRef = actorSystem.actorOf(Props[ToDoListServer],"todolistserver")
}
class ToDoListServer extends Actor{
  import slick.driver.PostgresDriver.api._
  implicit val db = Database.forConfig("my.myDb",Server.config)
  implicit val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(10))

  override def receive: Receive = {
    case x:String if (x.trim.isEmpty) =>
    case x:String =>
      val snd = sender()
          val arr = x.split(' ')
       arr(0) match {
        case "list" =>
          db.run(com.todolist.shared.Tables.Tasks.filter(_.done === false).sortBy(_.id).result)
            .onComplete{ e => e match {
            case Success(e) =>
              val res = e.map(e=>s"${e.id}. ${e.text}").mkString("\n")
              println(res)
              snd ! res
          }}
        case "add" =>
          db.run(com.todolist.shared.Tables.Tasks += com.todolist.shared.Tables.TasksRow(text = arr.drop(1).mkString(" "),done = false,id=0))
        case "done" =>
          val arr1 = arr(1).toInt
          db.run((for { c <- com.todolist.shared.Tables.Tasks  if c.id === arr1 } yield c.done).update(true))
        case _ =>  sender() ! "Unknown command"
      }
  }
}
