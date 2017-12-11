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

package org.kie.workbench.common.dmn.client.commands.util;

import java.util.Optional;
import java.util.stream.IntStream;

import org.kie.workbench.common.dmn.client.widgets.grid.model.DMNGridData;
import org.uberfire.ext.wires.core.grids.client.model.impl.BaseGridCellValue;

public class CommandUtils {

    public static <T> void updateRowNumbers(final DMNGridData uiModel,
                                            final Class<T> rowNumberColumnClass,
                                            final IntStream rangeOfRowsToUpdate) {
        final Optional<T> rowNumberColumn = uiModel
                .getColumns()
                .stream()
                .filter(c -> rowNumberColumnClass.isInstance(c))
                .map(c -> rowNumberColumnClass.cast(c))
                .findFirst();

        rowNumberColumn.ifPresent(c -> {
            final int columnIndex = uiModel.getColumns().indexOf(c);
            rangeOfRowsToUpdate.forEach(
                    rowIndex -> uiModel.setCell(rowIndex,
                                                columnIndex,
                                                new BaseGridCellValue<>(rowIndex + 1))

            );
        });
    }
}
