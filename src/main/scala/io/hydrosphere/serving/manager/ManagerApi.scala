package io.hydrosphere.serving.manager

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives
import io.hydrosphere.serving.controller.{CommonController, SwaggerDocController}
import io.hydrosphere.serving.manager.controller.{ModelRuntimeController, _}
import akka.http.scaladsl.server.Directives._

import scala.concurrent.ExecutionContext
import scala.reflect.runtime.{universe => ru}

/**
  *
  */
class ManagerApi(managerServices: ManagerServices)
  (implicit val system: ActorSystem, implicit val ex: ExecutionContext) {
  val commonController = new CommonController()

  val runtimeTypeController = new RuntimeTypeController(managerServices.modelManagementService)

  val modelController = new ModelController(managerServices.modelManagementService)

  val modelRuntimeController = new ModelRuntimeController(managerServices.modelManagementService)

  val swaggerController = new SwaggerDocController(system) {
    override val apiTypes: Seq[ru.Type] = Seq(
      ru.typeOf[RuntimeTypeController],
      ru.typeOf[ModelController],
      ru.typeOf[ModelRuntimeController]
    )
  }

  val routes: Route = {
    commonController.routes ~ swaggerController.routes ~ CorsDirectives.cors() {
      runtimeTypeController.routes ~ modelController.routes ~ modelRuntimeController.routes
    }
  }
}