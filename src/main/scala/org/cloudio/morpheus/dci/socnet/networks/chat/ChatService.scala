package org.cloudio.morpheus.dci.socnet.networks.chat

import org.cloudio.morpheus.dci.socnet.objects.{Offline, Online, PersonPublicEntity, PersonConnectionsEntity}
import org.morpheus._
import org.morpheus.Morpheus._

/**
 * Created by zslajchrt on 29/07/15.
 */

@fragment
trait ChatService {

  this: Online with PersonPublicEntity with PersonConnectionsEntity with PersonLookup =>

  def open(remoteParties: List[String]): ChatSession = {
    val a = &&(this)
    null
  }

}

//@fragment
//trait LocalParty {
//
//}
//
//@fragment
//trait RemoteParty {
//  def sendMessage()
//}


@fragment
trait ChatSession {

}

trait PersonLookup {

  def lookupPerson(nick: String): Option[&[PersonPublicEntity with (Online or Offline)]]

}
