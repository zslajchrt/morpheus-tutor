package org.cloudio.morpheus.dci.socnet.networks

import org.morpheus.Morpheus._
import org.morpheus._

/**
 * Created by zslajchrt on 15/07/15.
 */
abstract class AbstractNetworkMapper {
  val sourceMorphModel: MorphModel[_]
  val targetMorphModel: MorphModel[_]
  val sourceNetwork: Map[String, sourceMorphModel.Kernel]
  type SelfType

  class TargetKernelHolder(srcKernel: sourceMorphModel.Kernel) {
    lazy val kernel: targetMorphModel.Kernel = mapKernel(srcKernel)
  }

  protected def nodeSelf(nodeKernelHolder: TargetKernelHolder): &![SelfType]

  def mapKernel(srcKernel: sourceMorphModel.Kernel): targetMorphModel.Kernel

  def login(nodeId: String): Option[&![SelfType]] = {
    // todo: some authentication and authorization
    for (kh <- targetKernelHolders.get(nodeId)) yield nodeSelf(kh)
  }

  lazy val targetKernelHolders: Map[String, TargetKernelHolder] =
    sourceNetwork.map(srcKernEntry => (srcKernEntry._1, new TargetKernelHolder(srcKernEntry._2)))

  // a view
  lazy val targetNetwork: Map[String, targetMorphModel.Kernel] = targetKernelHolders.mapValues(_.kernel).view.force

}
