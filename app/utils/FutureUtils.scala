package utils

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

import scala.concurrent.ExecutionContext.Implicits.global

object FutureUtils {

  def futureToFutureTry[T](f: Future[T]): Future[Try[T]] = {
    f map(Success(_)) recover { case x => Failure(x) }
  }

  // collect disregarding failures
  def collectFutures[T](seq: Seq[Future[T]]): Future[Seq[T]] = {
    val f: Future[Seq[Try[T]]] = Future.sequence(seq map futureToFutureTry)
    f.map(xs => xs flatMap(_.toOption))
  }

}
