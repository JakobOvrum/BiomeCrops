package org.jacop.biomecrops.server

import cpw.mods.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.event.world.WorldEvent
import org.apache.logging.log4j.Logger
import org.jacop.biomecrops.config.Config

class WorldEventHandler(config : Config, logger : Logger) {
  @SubscribeEvent
  def onWorldLoad(event: WorldEvent.Load) {
    if(event.world.isRemote) return


  }
}
