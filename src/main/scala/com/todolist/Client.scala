package com.todolist

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object Client {
  case class Connect(server: ActorRef)
  case class Process(cmd: String)
}

class Client extends Actor {
  override def receive: Actor.Receive = {
    case Client.Connect(server) =>
      context become process(server)
  }
  def process(server: ActorRef): Receive = {
    case Client.Process(cmd) =>
      server ! cmd
    case x:String =>
      println(s"$x")
    case _ =>
  }
}

object ClientApp extends App {
  import com.typesafe.config.ConfigFactory
  import scala.concurrent.duration._
  val actorSystem = ActorSystem("client-system", ConfigFactory.load("client"))
  val client: ActorRef = actorSystem.actorOf(Props[Client])
  val serverPath = "akka://serversystem@127.0.0.1:2552/user/todolistserver"
  val serverSelection = actorSystem.actorSelection(serverPath)
  serverSelection.resolveOne(FiniteDuration(10, SECONDS)).foreach { (server: ActorRef) =>
    println(s"Connected to $server")
    client ! Client.Connect(server)
    while (true){
      println("Enter command:")
      scala.io.StdIn.readLine() match {
        case cmd => client ! Client.Process(cmd)
      }
    }
  } (actorSystem.dispatcher)
}
