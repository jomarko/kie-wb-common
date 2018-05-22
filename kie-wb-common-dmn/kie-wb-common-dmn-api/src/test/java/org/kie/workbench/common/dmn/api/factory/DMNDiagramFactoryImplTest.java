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

package org.kie.workbench.common.dmn.api.factory;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.dmn.api.definition.v1_1.DMNDiagram;
import org.kie.workbench.common.dmn.api.definition.v1_1.DMNModelInstrumentedBase;
import org.kie.workbench.common.dmn.api.definition.v1_1.Definitions;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.diagram.Metadata;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Graph;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.Bounds;
import org.kie.workbench.common.stunner.core.graph.content.definition.DefinitionSet;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.content.view.ViewImpl;
import org.kie.workbench.common.stunner.core.graph.impl.GraphImpl;
import org.kie.workbench.common.stunner.core.graph.impl.NodeImpl;
import org.kie.workbench.common.stunner.core.graph.store.GraphNodeStoreImpl;
import org.kie.workbench.common.stunner.core.util.UUID;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class DMNDiagramFactoryImplTest {

    private static final String NAME = "name";

    @Mock
    private Metadata metadata;

    @Mock
    private Bounds bounds;

    private GraphImpl<DefinitionSet> graph;

    private DMNDiagramFactoryImpl factory;

    @Before
    public void setup() {
        this.factory = new DMNDiagramFactoryImpl();
        this.graph = new GraphImpl<>(UUID.uuid(), new GraphNodeStoreImpl());
        this.graph.addNode(newNode(new DMNDiagram()));
    }

    private Node<View, Edge> newNode(final Object definition) {
        final Node<View, Edge> node = new NodeImpl<>(UUID.uuid());
        final View<Object> content = new ViewImpl<>(definition, bounds);
        node.setContent(content);
        return node;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDefaultNameSpaces() {
        final Diagram<Graph, Metadata> diagram = factory.build(NAME, metadata, graph);

        //We can safely get the first object on the iterator as we know the graph only contains one node
        final Node<View, Edge> root = (Node<View, Edge>) diagram.getGraph().nodes().iterator().next();
        final DMNDiagram dmnDiagram = (DMNDiagram) root.getContent().getDefinition();

        final Definitions dmnDefinitions = dmnDiagram.getDefinitions();
        final Map<String, String> dmnDefaultNameSpaces = dmnDefinitions.getNsContext();

        assertTrue(dmnDefaultNameSpaces.containsKey(DMNModelInstrumentedBase.PREFIX_FEEL));
        assertEquals(DMNModelInstrumentedBase.URI_FEEL,
                     dmnDefaultNameSpaces.get(DMNModelInstrumentedBase.PREFIX_FEEL));

        assertTrue(dmnDefaultNameSpaces.containsKey(DMNModelInstrumentedBase.PREFIX_DMN));
        assertEquals(DMNModelInstrumentedBase.URI_DMN,
                     dmnDefaultNameSpaces.get(DMNModelInstrumentedBase.PREFIX_DMN));

        assertTrue(dmnDefaultNameSpaces.containsKey(DMNModelInstrumentedBase.PREFIX_KIE));
        assertEquals(DMNModelInstrumentedBase.URI_KIE,
                     dmnDefaultNameSpaces.get(DMNModelInstrumentedBase.PREFIX_KIE));
    }
}
