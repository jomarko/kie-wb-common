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

package org.kie.workbench.common.dmn.client.editors.expressions;

import java.util.Optional;
import java.util.function.Supplier;

import com.ait.lienzo.client.core.mediator.Mediators;
import com.ait.lienzo.client.core.shape.Viewport;
import com.ait.lienzo.client.core.types.Transform;
import com.ait.lienzo.test.LienzoMockitoTestRunner;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Element;
import org.jboss.errai.common.client.api.IsElement;
import org.jboss.errai.common.client.dom.Anchor;
import org.jboss.errai.common.client.dom.Div;
import org.jboss.errai.common.client.dom.Document;
import org.jboss.errai.common.client.dom.HTMLElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.workbench.common.dmn.api.definition.HasExpression;
import org.kie.workbench.common.dmn.api.definition.HasName;
import org.kie.workbench.common.dmn.api.definition.v1_1.Expression;
import org.kie.workbench.common.dmn.api.property.dmn.Name;
import org.kie.workbench.common.dmn.client.editors.expressions.types.ExpressionEditorDefinition;
import org.kie.workbench.common.dmn.client.editors.expressions.types.ExpressionEditorDefinitions;
import org.kie.workbench.common.dmn.client.editors.expressions.types.undefined.UndefinedExpressionEditorDefinition;
import org.kie.workbench.common.dmn.client.events.ExpressionEditorSelectedEvent;
import org.kie.workbench.common.dmn.client.widgets.grid.BaseExpressionGrid;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.container.CellEditorControls;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.list.ListSelector;
import org.kie.workbench.common.dmn.client.widgets.grid.model.GridCellTuple;
import org.kie.workbench.common.dmn.client.widgets.layer.DMNGridLayer;
import org.kie.workbench.common.dmn.client.widgets.panel.DMNGridPanel;
import org.kie.workbench.common.stunner.core.client.api.SessionManager;
import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.command.SessionCommandManager;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.uberfire.ext.wires.core.grids.client.model.impl.BaseGridData;
import org.uberfire.ext.wires.core.grids.client.widget.grid.GridWidget;
import org.uberfire.ext.wires.core.grids.client.widget.layer.pinning.TransformMediator;
import org.uberfire.ext.wires.core.grids.client.widget.layer.pinning.impl.RestrictedMousePanMediator;
import org.uberfire.mocks.EventSourceMock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(LienzoMockitoTestRunner.class)
public class ExpressionEditorViewImplTest {

    @Mock
    private Anchor returnToDRG;

    @Mock
    private Div expressionEditorControls;

    @Mock
    private Document document;

    @Mock
    private DMNGridPanel gridPanel;

    @Mock
    private DMNGridLayer gridLayer;

    @Mock
    private RestrictedMousePanMediator mousePanMediator;

    @Mock
    private EventSourceMock<ExpressionEditorSelectedEvent> editorSelectedEvent;

    @Mock
    private CellEditorControls cellEditorControls;

    @Mock
    private TranslationService translationService;

    @Mock
    private ListSelector listSelector;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private SessionCommandManager<AbstractCanvasHandler> sessionCommandManager;

    @Mock
    private Supplier<ExpressionEditorDefinitions> expressionEditorDefinitionsSupplier;

    @Mock
    private UndefinedExpressionEditorDefinition undefinedExpressionEditorDefinition;

    @Mock
    private BaseExpressionGrid undefinedExpressionEditor;

    @Mock
    private Viewport viewport;

    @Mock
    private Element gridPanelElement;

    @Mock
    private Mediators viewportMediators;

    @Mock
    private ExpressionEditorDefinition<Expression> editorDefinition;

    @Mock
    private BaseExpressionGrid editor;

    @Mock
    private HasExpression hasExpression;

    @Captor
    private ArgumentCaptor<Transform> transformArgumentCaptor;

    @Captor
    private ArgumentCaptor<GridWidget> expressionContainerArgumentCaptor;

    @Captor
    private ArgumentCaptor<TransformMediator> transformMediatorArgumentCaptor;

    private ExpressionEditorViewImpl view;

    @Before
    @SuppressWarnings("unchecked")
    public void setup() {
        doReturn(viewport).when(gridPanel).getViewport();
        doReturn(viewportMediators).when(viewport).getMediators();
        doReturn(gridPanelElement).when(gridPanel).getElement();
        doReturn(Optional.of(editor)).when(editorDefinition).getEditor(any(GridCellTuple.class),
                                                                       any(HasExpression.class),
                                                                       any(Optional.class),
                                                                       any(Optional.class),
                                                                       anyBoolean());
        doReturn(new BaseGridData()).when(editor).getModel();
        doReturn(Optional.empty()).when(editor).getEditorControls();

        this.view = spy(new ExpressionEditorViewImpl(returnToDRG,
                                                     expressionEditorControls,
                                                     document,
                                                     gridPanel,
                                                     gridLayer,
                                                     mousePanMediator,
                                                     editorSelectedEvent,
                                                     cellEditorControls,
                                                     translationService,
                                                     listSelector,
                                                     sessionManager,
                                                     sessionCommandManager,
                                                     expressionEditorDefinitionsSupplier));

        final ExpressionEditorDefinitions expressionEditorDefinitions = new ExpressionEditorDefinitions();
        expressionEditorDefinitions.add(undefinedExpressionEditorDefinition);

        doReturn(expressionEditorDefinitions).when(expressionEditorDefinitionsSupplier).get();
        doReturn(Optional.empty()).when(undefinedExpressionEditorDefinition).getModelClass();
        doReturn(Optional.empty()).when(undefinedExpressionEditor).getEditorControls();
        doReturn(new BaseGridData()).when(undefinedExpressionEditor).getModel();
        doReturn(Optional.of(undefinedExpressionEditor)).when(undefinedExpressionEditorDefinition).getEditor(any(GridCellTuple.class),
                                                                                                             any(HasExpression.class),
                                                                                                             any(Optional.class),
                                                                                                             any(Optional.class),
                                                                                                             anyBoolean());

        doAnswer((i) -> i.getArguments()[1]).when(translationService).format(anyString(), anyObject());
    }

    @Test
    public void testSetupGridPanel() {
        verify(viewport).setTransform(transformArgumentCaptor.capture());
        final Transform transform = transformArgumentCaptor.getValue();

        assertEquals(ExpressionEditorViewImpl.VP_SCALE,
                     transform.getScaleX(),
                     0.0);
        assertEquals(ExpressionEditorViewImpl.VP_SCALE,
                     transform.getScaleY(),
                     0.0);

        verify(gridPanel).add(gridLayer);
    }

    @Test
    public void testSetupGridWidget() {
        verify(gridLayer).removeAll();
        verify(gridLayer).add(expressionContainerArgumentCaptor.capture());

        final GridWidget expressionContainer = expressionContainerArgumentCaptor.getValue();

        verify(gridLayer).select(eq(expressionContainer));
        verify(gridLayer).enterPinnedMode(eq(expressionContainer),
                                          any(Command.class));
    }

    @Test
    public void testSetupGridWidgetPanControl() {
        verify(mousePanMediator).setTransformMediator(transformMediatorArgumentCaptor.capture());

        final TransformMediator transformMediator = transformMediatorArgumentCaptor.getValue();

        verify(mousePanMediator).setBatchDraw(true);
        verify(gridLayer).setDefaultTransformMediator(eq(transformMediator));
        verify(viewportMediators).push(eq(mousePanMediator));
    }

    @Test
    public void testOnResize() {
        view.onResize();

        verify(gridPanel).onResize();
    }

    @Test
    public void testSetEditorDoesUpdateReturnToDRGTextWhenHasNameIsNotEmpty() {
        final String NAME = "NAME";
        final Name name = new Name(NAME);
        final HasName hasNameMock = mock(HasName.class);
        doReturn(name).when(hasNameMock).getName();
        final Optional<HasName> hasName = Optional.of(hasNameMock);

        view.setExpression(hasName,
                           hasExpression);

        verify(returnToDRG).setTextContent(eq(NAME));
    }

    @Test
    public void testSetEditorDoesNotUpdateReturnToDRGTextWhenHasNameIsEmpty() {
        final Optional<HasName> hasName = Optional.empty();

        view.setExpression(hasName,
                           hasExpression);

        verify(returnToDRG, never()).setTextContent(any(String.class));
    }

    @Test
    public void testOnExpressionEditorSelected() throws Exception {
        final Optional<BaseExpressionGrid> expressionGridEditor = Optional.of(editor);
        final HTMLElement selectedEditorControlsHTML = mock(HTMLElement.class);
        final IsElement selectedEditorControlsElement = mock(IsElement.class);
        final Optional<IsElement> selectedEditorControls = Optional.of(selectedEditorControlsElement);

        doReturn(selectedEditorControlsHTML).when(selectedEditorControlsElement).getElement();
        doReturn(selectedEditorControls).when(editor).getEditorControls();

        view.onExpressionEditorSelected(expressionGridEditor);
        verify(expressionEditorControls).appendChild(selectedEditorControlsHTML);
    }
}
