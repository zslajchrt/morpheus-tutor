package org.cloudio.morpheus.dci.loadBalancer.data

/**
 *
 * Created by zslajchrt on 25/06/15.
 */

trait Machine {
  def machineName: String
  def appendRecord(fileName: String, data: Array[Byte]): Unit
  def readLastRecords(fileName: String, recordCount: Int): List[Array[Byte]]
  // other methods ...
}

case class MachineMock(machineName: String) extends Machine {

  private var files = Map.empty[String, List[Array[Byte]]]

  override def appendRecord(fileName: String, data: Array[Byte]): Unit = {
    println(s"Appending ${data.length} bytes to $fileName on $machineName")
    files.get(fileName) match {
      case None =>
        files += (fileName -> List(data))
      case Some(records) =>
        files += (fileName -> (data :: records))
    }
  }

  override def readLastRecords(fileName: String, recordCount: Int): List[Array[Byte]] = {
    files.get(fileName) match {
      case None => List.empty
      case Some(records) => records.take(recordCount)
    }
  }

}
