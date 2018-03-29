package cn.itcast

import akka.actor.{Actor, ActorRef, ActorSelection, ActorSystem, Props}
import com.typesafe.config.ConfigFactory


//todo:利用akka中的actor实现2个进程间的通信-----Worker端
class Worker  extends Actor{
  println("Worker constructor invoked")

  //prestart方法会在构造代码块之后被调用，并且只会被调用一次
  override def preStart(): Unit = {
    println("preStart method invoked")
    //获取master actor的引用
    //ActorContext全局变量，可以通过在已经存在的actor中(无需自定义)，可用于寻找目标actor
    //调用对应actorSelection方法来获取 master的引用
    // 方法需要一个path路径：1、通信协议、2、master的IP地址、3、master的端口 4、创建master actor老大 5、actor层级
    val master: ActorSelection = context.actorSelection("akka.tcp://masterActorSystem@172.16.43.63:8888/user/masterActor")

    //向master发送消息
    master ! "connect"

  }

  //receive方法会在prestart方法执行后被调用，不断的接受消息
  override def receive: Receive = {
    case "connect" =>{
      println("a client connected")
    }
    case "success" =>{
      println("注册成功")
    }
  }
}

object Worker{
  def main(args: Array[String]): Unit = {
    //定义worker的IP地址
    val host=args(0)
    //定义worker的端口
    val port=args(1)

    //准备配置文件
    val configStr=
      s"""
         |akka.actor.provider = "akka.remote.RemoteActorRefProvider"
         |akka.remote.netty.tcp.hostname = "$host"
         |akka.remote.netty.tcp.port = "$port"
      """.stripMargin

    //通过configFactory来解析配置信息
    val config=ConfigFactory.parseString(configStr)
    // 1、创建ActorSystem，它是整个进程中的老大，它负责创建和监督actor
    val workerActorSystem = ActorSystem("workerActorSystem",config)
    // 2、通过actorSystem来创建 worker actor
    val workerActor: ActorRef = workerActorSystem.actorOf(Props(new Worker),"workerActor")

    //向worker actor发送消息
    workerActor ! "connect"
  }
}