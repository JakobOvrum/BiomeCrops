package org.jacop.biomecrops.client

import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.command.ICommandSender
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.MovingObjectPosition.MovingObjectType
import net.minecraft.util.{ChatComponentText, EnumChatFormatting}
import org.jacop.biomecrops.config.SupportedBlocks
import org.jacop.biomecrops.config.SupportedBlocks.SupportLevel
import org.jacop.biomecrops.{ICommand_ScalaCompatible, getBlockInheritance}

import scala.collection.JavaConversions._
import scala.collection.immutable.HashMap

class ClientCommands extends ICommand_ScalaCompatible {
  private val aliases = new java.util.ArrayList[String]

  aliases.add("biomecrops")
  aliases.add("bc")

  private val subCommands = HashMap[String, (EntityPlayer, Seq[String]) => Any](
    "info" -> {(player, args) =>
      val world = player.getEntityWorld
      val reply =
        Option(Minecraft.getMinecraft.objectMouseOver).filter(_.typeOfHit == MovingObjectType.BLOCK).map(trace => {
          val block = world.getBlock(trace.blockX, trace.blockY, trace.blockZ)
          val blockClass = block.getClass
          val classes = getBlockInheritance(blockClass)
          val supportLevels = SupportedBlocks.supportLevel(blockClass)

          def colorForSupportLevel(level : SupportLevel) = level match {
              case SupportedBlocks.Guarantee => EnumChatFormatting.GREEN
              case SupportedBlocks.Maybe => EnumChatFormatting.YELLOW
              case SupportedBlocks.Unsupported => EnumChatFormatting.RED
            }

          def colorForClass(cls : Class[_ <: Block]) = supportLevels.find(_._1 == cls) match {
              case Some(pair) => Some(colorForSupportLevel(pair._2))
              case None => None
            }

          val concludeColor = colorForSupportLevel(supportLevels.unzip._2.min)

          concludeColor + Block.blockRegistry.getNameForObject(block) + EnumChatFormatting.RESET + " | " +
            classes.map(cls => colorForClass(cls) match {
              case Some(color) => color + cls.getName + EnumChatFormatting.RESET
              case None => cls.getName
            }).mkString(" -> ")
        })
      reply.foreach(msg => player.addChatMessage(new ChatComponentText(msg)))
    }
  )

  private val commandUsage = "Usage: /" + aliases.mkString("|") + " " + subCommands.keys.mkString("|")

  override def getCommandName = aliases.head
  override def getCommandUsage(sender: ICommandSender) = commandUsage
  override def getCommandAliases = aliases

  override def processCommand(sender: ICommandSender, args: Array[String]) {
    sender match {
      case player: EntityPlayer => subCommands.get(args.headOption.getOrElse("").toLowerCase) match {
        case Some (subCommand) => subCommand (player, args.drop(1).toSeq)
        case None => sender.addChatMessage(new ChatComponentText(commandUsage))
      }
    }
  }

  override def canCommandSenderUseCommand(sender: ICommandSender) = true
  override def addTabCompletionOptions(sender: ICommandSender, args: Array[String]) = null
  override def isUsernameIndex(args: Array[String], i: Int) = false
}
