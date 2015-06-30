package org.jacop.biomecrops

import java.io.{File, FileWriter, PrintWriter}

import net.minecraft.block.Block
import net.minecraftforge.client.ClientCommandHandler
import net.minecraftforge.common.MinecraftForge
import org.apache.logging.log4j.Logger
import org.jacop.biomecrops.client.ClientCommands
import org.jacop.biomecrops.config.Config
import org.jacop.biomecrops.server.PlantGrowthHandler

class ClientProxy extends Proxy {
  override def init(config : Config, logger : Logger) {
    ClientCommandHandler.instance.registerCommand(new ClientCommands)
  }

  override def postInit(config : Config, logger : Logger): Unit = {
    val plantGrowthHandler = new PlantGrowthHandler(config, logger)
    MinecraftForge.EVENT_BUS.register(plantGrowthHandler)

    if(config.dumpCropBiomeInfo) {
      val dumpsDir = new File("dumps")
      if(!dumpsDir.exists)
        dumpsDir.mkdir()

      if(dumpsDir.isDirectory) {
        val lines = for{(blockID, settings) <- plantGrowthHandler.blockToCropSettings
                        blockName = Block.blockRegistry.getNameForObject(Block.blockRegistry.getObjectById(blockID))
                        biomes = settings.thrivingBiomes.map(_.biomeName)
                        requiresSunlight = settings.requireDirectSunlight}
          yield s"$blockName = $biomes (sunlight: $requiresSunlight)"

        val writer = new PrintWriter(new FileWriter(new File("dumps", "biomecrops.csv")))
        try lines.foreach(writer.println)
        finally writer.close()

        logger.info("Dumped crop information in dumps/biomecrops.csv; edit main.cfg to disable this")
      }
    }
  }
}
