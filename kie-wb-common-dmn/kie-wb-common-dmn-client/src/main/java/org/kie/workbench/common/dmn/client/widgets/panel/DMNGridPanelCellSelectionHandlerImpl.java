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

package org.kie.workbench.common.dmn.client.widgets.panel;

import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.kie.workbench.common.dmn.api.qualifiers.DMNEditor;
import org.kie.workbench.common.dmn.client.editors.expressions.types.literal.LiteralExpressionGrid;
import org.kie.workbench.common.dmn.client.editors.expressions.types.undefined.UndefinedExpressionGrid;
import org.kie.workbench.common.dmn.client.widgets.layer.DMNGridLayer;
import org.uberfire.ext.wires.core.grids.client.model.GridCell;
import org.uberfire.ext.wires.core.grids.client.model.GridColumn;
import org.uberfire.ext.wires.core.grids.client.model.GridData;
import org.uberfire.ext.wires.core.grids.client.model.GridData.SelectedCell;
import org.uberfire.ext.wires.core.grids.client.widget.grid.GridWidget;
import org.uberfire.ext.wires.core.grids.client.widget.grid.selections.CellSelectionStrategy;
import org.uberfire.ext.wires.core.grids.client.widget.grid.selections.impl.RangeSelectionStrategy;

@ApplicationScoped
public class DMNGridPanelCellSelectionHandlerImpl implements DMNGridPanelCellSelectionHandler {

    private DMNGridLayer gridLayer;

    public DMNGridPanelCellSelectionHandlerImpl() {
        //CDI proxy
    }

    @Inject
    public DMNGridPanelCellSelectionHandlerImpl(final @DMNEditor DMNGridLayer gridLayer) {
        this.gridLayer = gridLayer;
    }

    @Override
    public void selectCellIfRequired(final int uiRowIndex,
                                     final int uiColumnIndex,
                                     final GridWidget gridWidget,
                                     final boolean isShiftKeyDown,
                                     final boolean isControlKeyDown) {
        // If the right-click did not occur in an already selected cell, ensure the cell is selected
        GridWidget _gridWidget = gridWidget;
        int _uiRowIndex = uiRowIndex;
        int _uiColumnIndex = uiColumnIndex;

        // LiteralExpression and UndefinedExpression are not handled as grids in
        // their own right. In these circumstances use their parent GridWidget.
        if (_gridWidget instanceof LiteralExpressionGrid) {
            final LiteralExpressionGrid grid = (LiteralExpressionGrid) _gridWidget;
            _gridWidget = grid.getParentInformation().getGridWidget();
            _uiRowIndex = grid.getParentInformation().getRowIndex();
            _uiColumnIndex = grid.getParentInformation().getColumnIndex();
        } else if (_gridWidget instanceof UndefinedExpressionGrid) {
            final UndefinedExpressionGrid grid = (UndefinedExpressionGrid) _gridWidget;
            _gridWidget = grid.getParentInformation().getGridWidget();
            _uiRowIndex = grid.getParentInformation().getRowIndex();
            _uiColumnIndex = grid.getParentInformation().getColumnIndex();
        }

        final int rowIndex = _uiRowIndex;
        final GridData gridData = _gridWidget.getModel();
        final GridColumn<?> column = gridData.getColumns().get(_uiColumnIndex);
        final Stream<SelectedCell> modelColumnSelectedCells = gridData.getSelectedCells().stream().filter(sc -> sc.getColumnIndex() == column.getIndex());
        final boolean isContextMenuCellSelectedCell = modelColumnSelectedCells.map(SelectedCell::getRowIndex).anyMatch(ri -> ri == rowIndex);
        if (!isContextMenuCellSelectedCell) {
            selectCell(_uiRowIndex,
                       _uiColumnIndex,
                       _gridWidget,
                       isShiftKeyDown,
                       isControlKeyDown);
        }
    }

    private void selectCell(final int uiRowIndex,
                            final int uiColumnIndex,
                            final GridWidget gridWidget,
                            final boolean isShiftKeyDown,
                            final boolean isControlKeyDown) {
        // Lookup CellSelectionManager for cell
        final GridData gridModel = gridWidget.getModel();

        CellSelectionStrategy selectionStrategy;
        final GridCell<?> cell = gridModel.getCell(uiRowIndex,
                                                   uiColumnIndex);
        if (cell == null) {
            selectionStrategy = RangeSelectionStrategy.INSTANCE;
        } else {
            selectionStrategy = cell.getSelectionStrategy();
        }
        if (selectionStrategy == null) {
            return;
        }

        gridLayer.select(gridWidget);

        // Handle selection
        if (selectionStrategy.handleSelection(gridModel,
                                              uiRowIndex,
                                              uiColumnIndex,
                                              isShiftKeyDown,
                                              isControlKeyDown)) {
            gridLayer.batch();
        }
    }
}
