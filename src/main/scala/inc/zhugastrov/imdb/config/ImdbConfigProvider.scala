package inc.zhugastrov.imdb.config

import com.google.inject.{AbstractModule, Provider, Provides, Singleton}
import com.typesafe.config.{Config, ConfigFactory}

class ImdbConfigProvider extends Provider[Config] {
  override def get(): Config = ConfigFactory.load()
}
