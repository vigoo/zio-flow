/*
 * Copyright 2021-2022 John A. De Goes and the ZIO Contributors
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

package zio.flow

import zio.schema.{Schema, TypeId}

/**
 * Represents a reference to a persisted remote variable of type A
 *
 * Remote variables can not be shared between top level workflows, but they can
 * be accessed from forked workflows. For more information about scoping of
 * remote variables see zio.flow.runtime.internal.RemoteVariableScope
 */
case class RemoteVariableReference[A](name: RemoteVariableName) {

  /**
   * Gets a [[Remote]] which represents the value stored in this remote variable
   */
  def toRemote: Remote.Variable[A] = Remote.Variable(name)
}
object RemoteVariableReference {

  private val anySchema: Schema[RemoteVariableReference[Any]] =
    Schema.CaseClass1(
      TypeId.parse("zio.flow.RemoteVariableReference"),
      Schema.Field(
        "name",
        Schema[RemoteVariableName],
        get0 = _.name,
        set0 = (a: RemoteVariableReference[Any], v: RemoteVariableName) => a.copy(name = v)
      ),
      RemoteVariableReference.apply[Any]
    )
  implicit def schema[A]: Schema[RemoteVariableReference[A]] =
    anySchema.asInstanceOf[Schema[RemoteVariableReference[A]]]
}
