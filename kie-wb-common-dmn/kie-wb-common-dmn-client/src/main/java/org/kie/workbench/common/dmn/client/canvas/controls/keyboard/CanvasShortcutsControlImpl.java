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

package org.kie.workbench.common.dmn.client.canvas.controls.keyboard;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.kie.workbench.common.dmn.api.qualifiers.DMNEditor;
import org.kie.workbench.common.stunner.core.client.canvas.controls.keyboard.KeysMatcher;
import org.kie.workbench.common.stunner.core.client.components.toolbox.actions.GeneralCreateNodeAction;
import org.kie.workbench.common.stunner.core.client.event.keyboard.KeyboardEvent;
import org.kie.workbench.common.stunner.core.lookup.domain.CommonDomainLookups;

@DMNEditor
@Dependent
public class CanvasShortcutsControlImpl extends org.kie.workbench.common.stunner.core.client.canvas.controls.keyboard.CanvasShortcutsControlImpl {

    @Inject
    public CanvasShortcutsControlImpl(final CommonDomainLookups commonDomainLookups,
                                      final @DMNEditor GeneralCreateNodeAction createNodeAction) {
        super(commonDomainLookups, createNodeAction);
    }

    @Override
    public void onKeyDownEvent(final KeyboardEvent.Key... keys) {
        if (selectedNodeId() != null) {
            if (KeysMatcher.doKeysMatch(keys,
                                        KeyboardEvent.Key.D)) {
                if (selectedNodeIsDecision()) {
                    // TODO
                    appendNode(selectedNodeId());
                }
            }

            // TODO
            // ...
        }
    }

    private boolean selectedNodeIsDecision() {
        // TODO
        return true;
    }
}
