package io.hydrosphere.serving.manager.repository.db

import io.hydrosphere.serving.manager.db.Tables
import io.hydrosphere.serving.manager.model._
import io.hydrosphere.serving.manager.repository.ModelServiceRepository
import org.apache.logging.log4j.scala.Logging

import scala.concurrent.{ExecutionContext, Future}

/**
  *
  */
class ModelServiceRepositoryImpl(databaseService: DatabaseService)(implicit executionContext: ExecutionContext)
  extends ModelServiceRepository with Logging {

  import databaseService._
  import databaseService.driver.api._
  import ModelServiceRepositoryImpl._


  override def create(entity: ModelService): Future[ModelService] =
    db.run(
      Tables.ModelService returning Tables.ModelService += Tables.ModelServiceRow(
        -1,
        entity.serviceName,
        entity.cloudDriverId,
        entity.modelRuntime.id
      )
    ).map(s => mapFromDb(s, Some(entity.modelRuntime)))

  override def get(id: Long): Future[Option[ModelService]] =
    db.run(
      Tables.ModelService
        .filter(_.serviceId === id)
        .joinLeft(Tables.ModelRuntime)
        .on({ case (ms, mr) => ms.runtimeId === mr.runtimeId })
        .joinLeft(Tables.RuntimeType)
        .on({ case ((ms, mr), rt) => mr.flatMap(_.runtimeTypeId) === rt.runtimeTypeId })
        .result.headOption
    ).map(m => mapFromDb(m))

  override def delete(id: Long): Future[Int] = db.run(
    Tables.ModelService
      .filter(_.serviceId === id)
      .delete
  )

  override def all(): Future[Seq[ModelService]] =
    db.run(
      Tables.ModelService
        .joinLeft(Tables.ModelRuntime)
        .on({ case (ms, mr) => ms.runtimeId === mr.runtimeId })
        .joinLeft(Tables.RuntimeType)
        .on({ case ((ms, mr), rt) => mr.flatMap(_.runtimeTypeId) === rt.runtimeTypeId })
        .result
    ).map(s => mapFromDb(s))
}

object ModelServiceRepositoryImpl {

  def mapFromDb(model: Option[(
    (Tables.ModelService#TableElementType, Option[Tables.ModelRuntime#TableElementType]),
      Option[Tables.RuntimeType#TableElementType]
    )]): Option[ModelService] = model match {
    case Some(tuple) =>
      Some(mapFromDb(tuple._1._1,
        tuple._1._2.map(
          t => ModelRuntimeRepositoryImpl.mapFromDb(t, RuntimeTypeRepositoryImpl.mapFromDb(tuple._2))
        )
      ))
    case _ => None
  }

  def mapFromDb(tuples: Seq[(
    (Tables.ModelService#TableElementType, Option[Tables.ModelRuntime#TableElementType]),
      Option[Tables.RuntimeType#TableElementType]
    )]): Seq[ModelService] = {
    tuples.map(tuple =>
      mapFromDb(tuple._1._1,
        tuple._1._2.map(
          t => ModelRuntimeRepositoryImpl.mapFromDb(t, RuntimeTypeRepositoryImpl.mapFromDb(tuple._2))
        )
      )
    )
  }

  def mapFromDb(model: Tables.ModelService#TableElementType, modelRuntime: Option[ModelRuntime]): ModelService = {
    ModelService(
      serviceId = model.serviceId,
      serviceName = model.serviceName,
      cloudDriverId = model.cloudDriverId,
      modelRuntime = modelRuntime.getOrElse(throw new RuntimeException()),
      status = model.status,
      statusText = model.statustext
    )
  }
}