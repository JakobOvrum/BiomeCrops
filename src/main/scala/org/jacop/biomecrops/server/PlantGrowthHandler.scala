package org.jacop.biomecrops.server

import cpw.mods.fml.common.eventhandler.Event.Result
import cpw.mods.fml.common.eventhandler.{Event, SubscribeEvent}
import net.minecraft.block.Block
import net.minecraft.world.World
import net.minecraft.world.biome.BiomeGenBase
import net.minecraftforge.common.BiomeDictionary
import org.apache.logging.log4j.Logger
import org.jacop.biomecrops.config.{Config, SupportedBlocks}
import org.jacop.biomecrops.getBlockInheritance
import squeek.applecore.api.plants.PlantGrowthEvent

class PlantGrowthHandler(config : Config, logger : Logger) {
  private val allCropSettings =
    for(crops <- config.cropCategories; categoryName = crops.name)
      yield new CropSettings(
          categoryName = categoryName,
          blockNames = crops.blockNames,
          requireDirectSunlight = crops.requireDirectSunlight,
          biomeTypeNames = crops.biomeTypes.map(_.toIterable),
          rejectBiomeTypeNames = crops.rejectBiomeTypes
        )

  val blockToCropSettings : Map[Int, CropSettings] =
    (for{cropSettings <- allCropSettings
        blockID <- cropSettings.blockIDs}
      yield blockID -> cropSettings).toMap

  object CropSettings {
    def biomeTypeByName(name : String) =
      try Left(BiomeDictionary.Type.valueOf(name))
      catch {
        case e: IllegalArgumentException => Right(name)
      }

    def blockByName(name : String) = {
      Block.getBlockFromName(name) match {
        case block: Block => Left(block)
        case null => Right(name)
      }
    }
  }

  class CropSettings(categoryName : String,
                 blockNames: Iterable[String],
                 val requireDirectSunlight : Boolean,
                 biomeTypeNames : Option[Iterable[String]],
                 rejectBiomeTypeNames : Iterable[String]) {
    private val resolvedBlocks = blockNames
      .map(CropSettings.blockByName)

    resolvedBlocks.collect { case Right(x) => x }
      .foreach(invalidBlockName => logger.error(s"Invalid block name $invalidBlockName in category $categoryName"))

    val blocks = resolvedBlocks.collect { case Left(x) => x }
    val blockIDs = blocks.map(Block.getIdFromBlock)

    blocks.zip(blocks.map(_.getClass).map(blockClass => SupportedBlocks.list.exists(_.isAssignableFrom(blockClass))))
      .filter(!_._2)
      .unzip._1
      .map(block => Block.blockRegistry.getNameForObject(block) -> getBlockInheritance(block.getClass))
      .foreach(pair => {
        val blockName = pair._1
        val superChain = pair._2.map(_.getName).mkString(" -> ")
        logger.warn(s"block $blockName does not derive from a known plant block; this block requires manual AppleCore integration | $superChain")
    })

    private def resolveBiomeNames(biomeTypeNames : Iterable[String]) = {
      val biomeTypes = biomeTypeNames.map(CropSettings.biomeTypeByName)
      (biomeTypes.collect{case Left(x) => x}, biomeTypes.collect{case Right(x) => x})
    }

    private val (biomeTypes, unrecognizedBiomeTypes) = resolveBiomeNames(biomeTypeNames.getOrElse(Iterable[String]()))
    private val (rejectBiomeTypes, unrecognizedRejectBiomeTypes) = resolveBiomeNames(rejectBiomeTypeNames)

    (unrecognizedBiomeTypes ++ unrecognizedRejectBiomeTypes)
      .foreach(name => logger.error(s"Unrecognized biome type $name in category $categoryName"))

    private val biomes = if(biomeTypeNames.isDefined)
      biomeTypes.flatMap(BiomeDictionary.getBiomesForType)
    else
      BiomeGenBase.getBiomeGenArray.toIterable

    private val rejectBiomes = rejectBiomeTypes.flatMap(BiomeDictionary.getBiomesForType)

    val thrivingBiomes = biomes.toSet.diff(rejectBiomes.toSet)
  }

  private val debugBlockFilter = List("minecraft:red_mushroom", "minecraft:brown_mushroom").map(Block.getBlockFromName).map(Block.getIdFromBlock)

  private def handleEvent(world : World, block : Block, x : Int, y : Int, z : Int): Event.Result = {
    val blockID = Block.getIdFromBlock(block)
    blockToCropSettings.get(blockID) match {
      case None =>
        if(config.warnUnconfiguredPlantGrowth)
          logger.warn("growth tick for unconfigured plant " + Block.blockRegistry.getNameForObject(block))
        Result.DEFAULT
      case Some(cropSettings) =>
        val biome = world.getWorldChunkManager.getBiomeGenAt(x, z)
        val validBiome = cropSettings.thrivingBiomes.contains(biome)
        val blockName = Block.blockRegistry.getNameForObject(block)
        val biomeName = biome.biomeName
        if(!debugBlockFilter.contains(blockID)) logger.info(s"growth tick or fertilize event for recognized block $blockName in biome $biomeName. Valid biome: $validBiome")
        if(!validBiome ||
          (cropSettings.requireDirectSunlight && !world.isDaytime && !world.canBlockSeeTheSky(x, y, z)))
          Result.DENY
        else
          Result.DEFAULT
    }
  }

  @SubscribeEvent
  def onGrowthTick(event : PlantGrowthEvent.AllowGrowthTick) {
    if(!event.world.isRemote)
      event.setResult(handleEvent(event.world, event.block, event.x, event.y, event.z))
  }

  /*
  @SubscribeEvent
  def onFertilize(event : FertilizationEvent.Fertilize) {
    event.setResult(handleEvent(event.world, event.block, event.x, event.y, event.z))
  }
  */
}
