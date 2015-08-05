package org.jacop.biomecrops

import java.io.{FileWriter, PrintWriter, File}

import net.minecraft.block.Block
import net.minecraftforge.common.MinecraftForge
import org.apache.logging.log4j.Logger
import org.jacop.biomecrops.common.PlantGrowthHandler
import org.jacop.biomecrops.config.Config

class CommonProxy {
  def init(config : Config, logger : Logger) : Unit = {
  }

  def postInit(config : Config, logger : Logger) : Unit = {
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
