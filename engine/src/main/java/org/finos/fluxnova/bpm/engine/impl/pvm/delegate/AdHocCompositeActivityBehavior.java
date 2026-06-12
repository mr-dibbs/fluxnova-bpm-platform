/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.finos.fluxnova.bpm.engine.impl.pvm.delegate;

/**
 * Composite behavior of an ad-hoc scope which may complete eagerly while child
 * executions are still active, e.g. when a completion condition is satisfied.
 */
public interface AdHocCompositeActivityBehavior extends CompositeActivityBehavior {

  /**
   * Invoked before a direct child execution of the ad-hoc scope leaves its
   * current activity via an outgoing transition. If this returns {@code true},
   * the child execution is ended instead of taking the transition, which in
   * turn completes the ad-hoc scope and cancels its remaining children.
   *
   * @param scopeExecution scope execution for the activity which defined the behavior
   * @return {@code true} if the ad-hoc scope should complete instead of
   *         propagating the child execution along the transition
   */
  boolean shouldCompleteOnChildTransition(ActivityExecution scopeExecution);
}
