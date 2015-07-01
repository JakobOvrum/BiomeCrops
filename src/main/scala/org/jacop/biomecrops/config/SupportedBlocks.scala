package org.jacop.biomecrops.config

import net.minecraft.block.Block
import net.minecraft.world.World

object SupportedBlocks {
  val list = List(
    "net.minecraft.block.BlockCrops",
    "net.minecraft.block.BlockReed",
    "net.minecraft.block.BlockCactus",
    "net.minecraft.block.BlockCocoa",
    "net.minecraft.block.BlockMushroom",
    "net.minecraft.block.BlockNetherWart",
    "net.minecraft.block.BlockSapling",
    "net.minecraft.block.BlockStem",
    "com.pam.harvestcraft.BlockPamFruit",
    "com.pam.harvestcraft.BlockPamSapling",
    "mods.natura.blocks.crops.BerryBush",
    "mods.natura.blocks.crops.NetherBerryBush",
    "mods.natura.blocks.crops.CropBlock",
    "mods.natura.blocks.crops.Glowshroom")
    .flatMap(name => try { List[Class[_]](Class.forName(name)) } catch { case e: ClassNotFoundException => Nil })
    .map(_.asSubclass(classOf[Block]))

  case class SupportLevel(order: Int) extends Ordered[SupportLevel] {
    override def compare(that: SupportLevel): Int = this.order - that.order
  }

  val Guarantee = SupportLevel(3)
  val Maybe = SupportLevel(2)
  val Unsupported = SupportLevel(1)

  private val obfuscatedUpdateTick_1_7_10 = "func_149674_a"

  def supportLevel(cls : Class[_ <: Block]) = {
    val tickClass = cls.getMethod(obfuscatedUpdateTick_1_7_10,
      classOf[World], classOf[Int], classOf[Int], classOf[Int], classOf[java.util.Random])
      .getDeclaringClass.asSubclass(classOf[Block])

    list.find(_ == tickClass) match {
      case Some(exactClass) => List(exactClass -> Guarantee)
      case None => list.find(_.isAssignableFrom(tickClass)) match {
        case Some(superClass) => List(superClass -> Guarantee, tickClass -> Maybe)
        case None => List(tickClass -> Unsupported)
      }
    }
  }
}
