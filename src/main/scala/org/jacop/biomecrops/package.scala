package org.jacop

import net.minecraft.block.Block
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Vec3

package object biomecrops {
  def getBlockInheritance(cls: Class[_ <: Block]): List[Class[_ <: Block]] = {
    if (cls == classOf[Block]) List(cls) else cls :: getBlockInheritance(cls.getSuperclass.asSubclass(classOf[Block]))
  }

  def tracePlayerCursor(player : EntityPlayer) = {
    val world = player.getEntityWorld
    val eyeHeight = 1.62
    val reachDistance = 300
    val startPos = Vec3.createVectorHelper(player.posX, player.posY, player.posZ)
    val adjustedStartPos = if (!world.isRemote) startPos.addVector(0, eyeHeight, 0) else startPos
    val look = player.getLook(1.0F)
    val endPos = adjustedStartPos.addVector(look.xCoord * reachDistance, look.yCoord * reachDistance, look.zCoord * reachDistance)
    Option(world.rayTraceBlocks(adjustedStartPos, endPos))
  }
}
