package org.jacop.biomecrops

import org.apache.logging.log4j.Logger
import org.jacop.biomecrops.config.Config

class ServerProxy extends CommonProxy {
  override def init(config : Config, logger : Logger): Unit = {
    super.init(config, logger)
  }

  override def postInit(config : Config, logger : Logger): Unit = {
    super.postInit(config, logger)
  }
}
