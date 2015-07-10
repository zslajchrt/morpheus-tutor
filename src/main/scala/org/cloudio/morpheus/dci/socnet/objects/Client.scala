package org.cloudio.morpheus.dci.socnet.objects

import org.morpheus.{fragment, dimension}

/**
* Created by zslajchrt on 10/07/15.
*/
@dimension
trait Client {

  def receive(rcvFn: PartialFunction[Any, Unit]): Unit

  def send(msg: Map[String, Any]): Unit

}


@fragment
trait ClientMock extends Client {
  override def receive(rcvFn: PartialFunction[Any, Unit]): Unit = {}

  override def send(msg: Map[String, Any]): Unit = {
    println(msg)
  }
}

