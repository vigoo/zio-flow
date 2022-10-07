/*
 * Copyright 2019-2022 John A. De Goes and the ZIO Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zio.flow.remote

import zio.flow.Remote
import zio.schema.{CaseSet, Schema, TypeId}

sealed trait RemoteOptic[-A, +B]

object RemoteOptic {
  final case class Lens[F, A, B](fieldName: String) extends RemoteOptic[A, B] {
    def get(s: Remote[A]): Remote[B] =
      Remote.OpticGet[A, B, B](this, s)

    def set(s: Remote[A])(value: Remote[B]): Remote[A] =
      Remote.OpticSet[A, B, B, A](this, s, value)

    def update(s: Remote[A])(f: Remote[B] => Remote[B]): Remote[A] =
      set(s)(f(get(s)))
  }

  final case class Prism[F, A, B](sumTypeId: TypeId, termName: String) extends RemoteOptic[A, B] {
    def get(s: Remote[A]): Remote[Option[B]] =
      Remote.OpticGet[A, B, Option[B]](this, s)

    def set(value: Remote[B]): Remote[A] =
      Remote.OpticSet[A, B, B, A](this, Remote.fail("not used"), value)
  }

  final case class Traversal[A, B]() extends RemoteOptic[A, B] {
    def get(s: Remote[A]): Remote[List[B]] =
      Remote.OpticGet[A, B, List[B]](this, s)

    def set(s: Remote[A])(values: Remote[List[B]]): Remote[A] =
      Remote.OpticSet[A, B, List[B], A](this, s, values)

    def update(s: Remote[A])(f: Remote[List[B]] => Remote[List[B]]): Remote[A] =
      set(s)(f(get(s)))
  }

  def schema[A, B]: Schema[RemoteOptic[A, B]] =
    Schema.EnumN(
      TypeId.parse("zio.flow.remote.RemoteOptic"),
      CaseSet
        .Cons(
          Schema.Case[Lens[Any, A, B], RemoteOptic[A, B]](
            "Lens",
            Schema.CaseClass1[String, Lens[Any, A, B]](
              TypeId.parse("zio.flow.remote.RemoteOptic.Lens"),
              Schema.Field("fieldName", Schema[String]),
              Lens(_),
              _.fieldName
            ),
            _.asInstanceOf[Lens[Any, A, B]]
          ),
          CaseSet.Empty[RemoteOptic[A, B]]()
        )
        .:+:(
          Schema.Case[Prism[Any, A, B], RemoteOptic[A, B]](
            "Prism",
            Schema.CaseClass2[TypeId, String, Prism[Any, A, B]](
              TypeId.parse("zio.flow.remote.RemoteOptic.Prism"),
              Schema.Field("sumTypeId", Schema[TypeId]),
              Schema.Field("termName", Schema[String]),
              Prism(_, _),
              _.sumTypeId,
              _.termName
            ),
            _.asInstanceOf[Prism[Any, A, B]]
          )
        )
        .:+:(
          Schema.Case[Traversal[A, B], RemoteOptic[A, B]](
            "Traversal",
            Schema.singleton(Traversal[A, B]()),
            _.asInstanceOf[Traversal[A, B]]
          )
        )
    )

  implicit val schemaAny: Schema[RemoteOptic[Any, Any]] = schema[Any, Any]
}
