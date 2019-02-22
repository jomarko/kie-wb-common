/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.stunner.core.client.session.command.impl;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.kie.workbench.common.stunner.core.client.components.layout.LayoutExecutor;
import org.kie.workbench.common.stunner.core.client.components.layout.qualifier.UndoableLayout;
import org.kie.workbench.common.stunner.core.client.session.ClientSession;
import org.kie.workbench.common.stunner.core.client.session.command.AbstractClientSessionCommand;
import org.kie.workbench.common.stunner.core.client.session.impl.EditorSession;
import org.kie.workbench.common.stunner.core.diagram.Diagram;

@Dependent
@Default
public class PerformAutomaticLayoutCommand extends AbstractClientSessionCommand<EditorSession> {

    private final LayoutExecutor layoutExecutor;

    @Inject
    public PerformAutomaticLayoutCommand(@UndoableLayout final LayoutExecutor layoutExecutor) {
        super(true);
        this.layoutExecutor = layoutExecutor;
    }

    @Override
    public boolean accepts(final ClientSession session) {
        return session instanceof EditorSession;
    }

    @Override
    public <V> void execute(final Callback<V> callback) {
        final Diagram diagram = getDiagram();

        layoutExecutor.applyLayout(diagram, true);

        callback.onSuccess();
    }

    Diagram getDiagram() {
        return getSession().getCanvasHandler().getDiagram();
    }
}