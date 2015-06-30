package org.jacop.biomecrops.config

import java.io.File

import net.minecraftforge.common.config.Configuration
import org.apache.logging.log4j.Logger

import scala.collection.JavaConversions._

object Config {
  private val BLOCK_NAMES_COMMENT = "Name of plant blocks to configure."
  private val BIOME_TYPES_COMMENT = "Thrive in biomes that have any of these types."
  private val REJECT_BIOME_TYPES_COMMENT = "Don't grow in biomes of these types, even if the biomes have types from biomeTypes."
  private val SUNLIGHT_COMMENT = "Whether or not direct sunlight is required to grow."

  def createExampleConfig(vanillaConfigFile: File) {
    val exampleConfig: Configuration = new Configuration(vanillaConfigFile)

    val temperateCategory = "Temperate Vanilla Crops"

    exampleConfig.get(temperateCategory, "blockNames",
      Array("minecraft:wheat", "minecraft:potatoes", "minecraft:carrots", "minecraft:pumpkin_stem"),
      BLOCK_NAMES_COMMENT)

    exampleConfig.get(temperateCategory, "biomeTypes",
      Array("plains", "forest"),
      BIOME_TYPES_COMMENT)

    exampleConfig.get(temperateCategory, "rejectBiomeTypes",
      Array("ocean", "river", "dry", "hot", "cold", "mesa", "swamp", "nether", "dead", "wasteland"),
      REJECT_BIOME_TYPES_COMMENT)

    exampleConfig.get(temperateCategory, "requireDirectSunlight", true, SUNLIGHT_COMMENT)

    val tropicalCategory = "Tropical Vanilla Crops"

    exampleConfig.get(tropicalCategory, "blockNames",
      Array("minecraft:reeds", "minecraft:melon_stem"))

    exampleConfig.get(tropicalCategory, "biomeTypes",
      Array("wet"))

    exampleConfig.get(tropicalCategory, "rejectBiomeTypes",
      Array("swamp", "cold", "nether", "dead", "wasteland"))

    exampleConfig.get(tropicalCategory, "requireDirectSunlight", true)

    val cocoaCategory = "Cocoa"

    exampleConfig.get(cocoaCategory, "blockNames",
      Array("minecraft:cocoa"))

    exampleConfig.get(cocoaCategory, "biomeTypes",
      Array("jungle"))

    exampleConfig.get(cocoaCategory, "rejectBiomeTypes",
      Array("nether", "dead", "spooky"))

    exampleConfig.get(cocoaCategory, "requireDirectSunlight", false)

    val cactusCategory = "Cactus"

    exampleConfig.get(cactusCategory, "blockNames",
      Array("minecraft:cactus"))

    exampleConfig.get(cactusCategory, "biomeTypes",
      Array("sandy"))

    exampleConfig.get(cactusCategory, "rejectBiomeTypes",
      Array("cold", "nether"))

    exampleConfig.get(cactusCategory, "requireDirectSunlight", true)

    val netherwartCategory = "Netherwart"

    exampleConfig.get(netherwartCategory, "blockNames",
      Array("minecraft:nether_wart"))

    exampleConfig.get(netherwartCategory, "biomeTypes",
      Array("nether", "spooky"))

    exampleConfig.get(netherwartCategory, "rejectBiomeTypes",
      Array[String]())

    exampleConfig.get(netherwartCategory, "requireDirectSunlight", false)

    val vineCategory = "Vines"

    exampleConfig.get(vineCategory, "blockNames",
      Array("minecraft:vine"))

    exampleConfig.get(vineCategory, "biomeTypes",
      "all")

    exampleConfig.get(vineCategory, "rejectBiomeTypes",
      Array("ocean", "cold", "sandy"))

    exampleConfig.get(vineCategory, "requireDirectSunlight", false)

    val mushroomCategory = "Mushrooms"

    exampleConfig.get(mushroomCategory, "blockNames",
      Array("minecraft:red_mushroom", "minecraft:brown_mushroom"))

    exampleConfig.get(mushroomCategory, "biomeTypes",
      Array("spooky", "swamp", "nether"))

    exampleConfig.get(mushroomCategory, "rejectBiomeTypes",
      Array("ocean", "cold", "sandy"))

    exampleConfig.get(mushroomCategory, "requireDirectSunlight", false)

    exampleConfig.save()
  }
}

class Config(configDirectory : File, val logger : Logger) {
  if(!configDirectory.exists)
    configDirectory.mkdirs()

  private val mainConfigFile = new File(configDirectory, "main.cfg")
  private val mainConfigExisted = mainConfigFile.exists
  private val mainConfig = new Configuration(mainConfigFile)
  mainConfig.setCategoryComment("globalSettings", "General settings.")

  private val generateVanillaExample = mainConfig.get(
    "globalSettings",
    "generateVanillaExample", true,
    "If it doesn't exist, generate and load vanilla.cfg with crop settings for vanilla crops.").getBoolean

  val dumpCropBiomeInfo = mainConfig.get(
    "globalSettings",
    "dumpCropBiomeInfo", false,
    "Dump information about crops and the biomes in which they can grow.").getBoolean

  val warnUnconfiguredPlantGrowth = mainConfig.get(
    "globalSettings",
    "warnUnconfiguredPlantGrowth", false,
    "Warn when growth ticks happen for unconfigured plants.").getBoolean

  if(!mainConfigExisted)
    mainConfig.save()

  private val vanillaConfigFile = new File(configDirectory, "vanilla.cfg")
  if (generateVanillaExample && !vanillaConfigFile.exists) {
    Config.createExampleConfig(vanillaConfigFile)
    logger.info("generated vanilla.cfg example configuration file; edit main.cfg to disable this")
  }

  private val configs = for {configFile <- configDirectory.listFiles()
                             if configFile.isFile
                             name = configFile.getName
                             if name != "main.cfg"
                             if name.endsWith(".cfg")}
    yield new Configuration(configFile)

  class CropCategory(config : Configuration, val name : String) {
    val blockNames = config.get(name, "blockNames", Array[String]()).getStringList
    private val biomeTypeList = config.get(name, "biomeTypes", Array[String]()).getStringList.map(_.toUpperCase)
    val biomeTypes = if(biomeTypeList.length == 1 && biomeTypeList(0) == "ALL") None else Some(biomeTypeList)
    val rejectBiomeTypes = config.get(name, "rejectBiomeTypes", Array[String]()).getStringList.map(_.toUpperCase)
    val requireDirectSunlight = config.get(name, "requireDirectSunlight", false).getBoolean
  }

  val cropCategories =
    for {config <- configs; category <- config.getCategoryNames}
      yield new CropCategory(config, category)
}
