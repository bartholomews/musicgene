package model.helpers

import cats.data.NonEmptySet

import scala.collection.immutable.SortedSet

object CollectionsHelpers {
  implicit class SetHelpers[A](set: Set[A]) {
    def groupedNes(size: Int)(implicit ordering: Ordering[A]): List[NonEmptySet[A]] =
      SortedSet.from(set).grouped(size).flatMap(xs => NonEmptySet.fromSet(xs)).toList
  }
}
