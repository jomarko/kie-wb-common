/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.core.client.canvas.controls.keyboard;

import org.kie.workbench.common.stunner.core.client.canvas.CanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.controls.CanvasControl;
import org.kie.workbench.common.stunner.core.client.event.keyboard.KeyboardEvent;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;

public interface CanvasShortcutsControl<C extends CanvasHandler, S extends ClientSession> extends CanvasControl<C>,
                                                                                                  CanvasControl.SessionAware<S> {

    /**
     * Each implementation recognizes different shortucts
     * @param keys
     */
    void onKeyDownEvent(final KeyboardEvent.Key... keys);

    /**
     * TODO change signature to take source and target id
     * connector will be computed automatically
     * generic implementation for both DMN and BPMN
     * @param sourceNodeId
     */
    void appendNode(final String sourceNodeId);

}
