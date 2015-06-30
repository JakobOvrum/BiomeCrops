package org.jacop.biomecrops

import java.io.File

import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import cpw.mods.fml.common.{Mod, SidedProxy}
import org.apache.logging.log4j.Logger
import org.jacop.biomecrops.config.Config

@Mod(modid = "biomecrops", name = "BiomeCrops", version = "${version}", dependencies = "required-after:AppleCore", modLanguage = "scala")
object BiomeCrops {
  private var config : Option[Config] = None
  private var logger : Option[Logger] = None

  @SidedProxy(clientSide="org.jacop.biomecrops.ClientProxy", serverSide="org.jacop.biomecrops.ServerProxy")
  var sidedProxy : Proxy = null

  @EventHandler
  def preInit(event : FMLPreInitializationEvent) {
    val logger = Option(event.getModLog).getOrElse(throw new AssertionError("failed getting logger for BiomeCrops mod"))

    this.logger = Some(logger)
    this.config = Some(new Config(
      configDirectory = new File(event.getModConfigurationDirectory, "BiomeCrops"),
      logger = logger
    ))
  }

  @EventHandler
  def init(event: FMLInitializationEvent) {
    sidedProxy.init(config.get, logger.get)
  }

  @EventHandler
  def postInit(event: FMLPostInitializationEvent) {
    sidedProxy.postInit(config.get, logger.get)
  }
}
