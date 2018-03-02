/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.workbench.common.dmn.client.editors.expressions.types.context;

import java.util.Optional;
import java.util.function.Supplier;

import org.kie.workbench.common.dmn.api.definition.v1_1.Context;
import org.kie.workbench.common.dmn.api.definition.v1_1.ContextEntry;
import org.kie.workbench.common.dmn.api.definition.v1_1.Expression;
import org.kie.workbench.common.dmn.api.definition.v1_1.InformationItem;
import org.kie.workbench.common.dmn.api.property.dmn.Name;
import org.kie.workbench.common.dmn.client.editors.expressions.types.ExpressionEditorDefinition;
import org.kie.workbench.common.dmn.client.editors.expressions.types.ExpressionEditorDefinitions;
import org.kie.workbench.common.dmn.client.widgets.grid.BaseExpressionGrid;
import org.kie.workbench.common.dmn.client.widgets.grid.controls.list.ListSelector;
import org.kie.workbench.common.dmn.client.widgets.grid.model.BaseUIModelMapper;
import org.kie.workbench.common.dmn.client.widgets.grid.model.DMNGridCell;
import org.kie.workbench.common.dmn.client.widgets.grid.model.GridCellTuple;
import org.uberfire.ext.wires.core.grids.client.model.GridCellValue;
import org.uberfire.ext.wires.core.grids.client.model.GridData;
import org.uberfire.ext.wires.core.grids.client.model.impl.BaseGridCellValue;
import org.uberfire.ext.wires.core.grids.client.widget.grid.GridWidget;
import org.uberfire.ext.wires.core.grids.client.widget.grid.selections.impl.RowSelectionStrategy;

public class ContextUIModelMapper extends BaseUIModelMapper<Context> {

    public static final String DEFAULT_ROW_CAPTION = "default";

    private final GridWidget gridWidget;

    private final Supplier<ExpressionEditorDefinitions> expressionEditorDefinitionsSupplier;

    private final ListSelector listSelector;

    public ContextUIModelMapper(final GridWidget gridWidget,
                                final Supplier<GridData> uiModel,
                                final Supplier<Optional<Context>> dmnModel,
                                final Supplier<ExpressionEditorDefinitions> expressionEditorDefinitionsSupplier,
                                final ListSelector listSelector) {
        super(uiModel,
              dmnModel);
        this.gridWidget = gridWidget;
        this.expressionEditorDefinitionsSupplier = expressionEditorDefinitionsSupplier;
        this.listSelector = listSelector;
    }

    @Override
    public void fromDMNModel(final int rowIndex,
                             final int columnIndex) {
        dmnModel.get().ifPresent(context -> {
            final boolean isLastRow = isLastRow(rowIndex);
            final ContextUIModelMapperHelper.ContextSection section = ContextUIModelMapperHelper.getSection(columnIndex);
            switch (section) {
                case ROW_INDEX:
                    if (!isLastRow) {
                        uiModel.get().setCell(rowIndex,
                                              columnIndex,
                                              () -> new ContextGridCell<>(new BaseGridCellValue<>(rowIndex + 1),
                                                                          listSelector));
                    } else {
                        uiModel.get().setCell(rowIndex,
                                              columnIndex,
                                              () -> new DMNGridCell<>(new BaseGridCellValue<>((Integer) null)));
                    }
                    uiModel.get().getCell(rowIndex,
                                          columnIndex).setSelectionStrategy(RowSelectionStrategy.INSTANCE);
                    break;
                case NAME:
                    final InformationItem variable = context.getContextEntry().get(rowIndex).getVariable();
                    final String name = variable == null ? DEFAULT_ROW_CAPTION : variable.getName().getValue();
                    if (!isLastRow) {
                        uiModel.get().setCell(rowIndex,
                                              columnIndex,
                                              () -> new ContextGridCell<>(new BaseGridCellValue<>(name),
                                                                          listSelector));
                    } else {
                        uiModel.get().setCell(rowIndex,
                                              columnIndex,
                                              () -> new DMNGridCell<>(new BaseGridCellValue<>(name)));
                    }
                    break;
                case EXPRESSION:
                    final ContextEntry ce = context.getContextEntry().get(rowIndex);
                    final Optional<Expression> expression = Optional.ofNullable(ce.getExpression());

                    final Optional<ExpressionEditorDefinition<Expression>> expressionEditorDefinition = expressionEditorDefinitionsSupplier.get().getExpressionEditorDefinition(expression);
                    expressionEditorDefinition.ifPresent(ed -> {
                        final Optional<BaseExpressionGrid> editor = ed.getEditor(new GridCellTuple(rowIndex,
                                                                                                   columnIndex,
                                                                                                   gridWidget),
                                                                                 ce,
                                                                                 expression,
                                                                                 Optional.ofNullable(ce.getVariable()),
                                                                                 true);
                        if (!isLastRow) {
                            uiModel.get().setCell(rowIndex,
                                                  columnIndex,
                                                  () -> new ContextGridCell<>(new ExpressionCellValue(editor),
                                                                              listSelector));
                        } else {
                            uiModel.get().setCell(rowIndex,
                                                  columnIndex,
                                                  () -> new DMNGridCell<>(new ExpressionCellValue(editor)));
                        }
                    });
            }
        });
    }

    protected boolean isLastRow(final int rowIndex) {
        if (dmnModel.get().isPresent()) {
            final Context context = dmnModel.get().get();
            return rowIndex == context.getContextEntry().size() - 1;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void toDMNModel(final int rowIndex,
                           final int columnIndex,
                           final Supplier<Optional<GridCellValue<?>>> cell) {
        dmnModel.get().ifPresent(context -> {
            final ContextUIModelMapperHelper.ContextSection section = ContextUIModelMapperHelper.getSection(columnIndex);
            switch (section) {
                case ROW_INDEX:
                    break;
                case NAME:
                    context.getContextEntry()
                            .get(rowIndex)
                            .getVariable()
                            .setName(new Name(cell.get().orElse(new BaseGridCellValue<>("")).getValue().toString()));
                    break;
                case EXPRESSION:
                    cell.get().ifPresent(v -> {
                        final ExpressionCellValue ecv = (ExpressionCellValue) v;
                        ecv.getValue().ifPresent(beg -> {
                            beg.getExpression().ifPresent(e -> context.getContextEntry()
                                    .get(rowIndex)
                                    .setExpression((Expression) e));
                        });
                    });
            }
        });
    }
}
