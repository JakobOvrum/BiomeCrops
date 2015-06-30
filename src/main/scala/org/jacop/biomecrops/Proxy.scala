package org.jacop.biomecrops

import org.apache.logging.log4j.Logger
import org.jacop.biomecrops.config.Config

trait Proxy {
  def init(config : Config, logger : Logger) : Unit
  def postInit(config : Config, logger : Logger) : Unit
}
