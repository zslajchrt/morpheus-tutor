package org.cloudio.morpheus.dci.socnet.networks

import org.morpheus.Morpheus._
import org.morpheus._

import scala.util.{Failure, Try}

/**
 * Created by zslajchrt on 15/07/15.
 */
abstract class AbstractNetworkMapper {
  val sourceMorphModel: MorphModel[_]
  val targetMorphModel: MorphModel[_]
  val sourceNetwork: Map[String, sourceMorphModel.Kernel]
  type Subject

  class TargetKernelHolder(srcKernel: sourceMorphModel.Kernel) {
    lazy val kernel: targetMorphModel.Kernel = mapKernel(srcKernel)
  }

  protected def makeSelf(selfKernelHolder: TargetKernelHolder): &![Subject]

  def mapKernel(srcKernel: sourceMorphModel.Kernel): targetMorphModel.Kernel

  def login(nodeId: String): Either[&![Subject], String] = {
    // todo: some authentication and authorization
    targetKernelHolders.get(nodeId) match {
      case None => Right(s"Unknown node $nodeId")
      case Some(kh) => Left(makeSelf(kh))
    }
  }

  lazy val targetKernelHolders: Map[String, TargetKernelHolder] =
    sourceNetwork.map(srcKernEntry => (srcKernEntry._1, new TargetKernelHolder(srcKernEntry._2)))

  // a view
  lazy val targetNetwork: Map[String, targetMorphModel.Kernel] = targetKernelHolders.mapValues(_.kernel).view.force

}
