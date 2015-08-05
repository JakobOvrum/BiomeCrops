package org.jacop.biomecrops

import java.io.{File, FileWriter, PrintWriter}

import net.minecraft.block.Block
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import org.apache.logging.log4j.Logger
import org.jacop.biomecrops.client.ClientCommands
import org.jacop.biomecrops.config.Config
import org.jacop.biomecrops.common.PlantGrowthHandler

class CombinedProxy extends CommonProxy {
  override def init(config : Config, logger : Logger) {
    super.init(config, logger)
    ClientCommandHandler.instance.registerCommand(new ClientCommands)
  }

  override def postInit(config : Config, logger : Logger): Unit = {
    super.postInit(config, logger)
  }
}
