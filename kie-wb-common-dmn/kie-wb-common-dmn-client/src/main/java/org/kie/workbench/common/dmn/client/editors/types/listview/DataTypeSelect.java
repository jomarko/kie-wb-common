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

package org.kie.workbench.common.dmn.client.editors.types.listview;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import elemental2.dom.HTMLElement;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.kie.workbench.common.dmn.client.editors.types.common.DataType;
import org.kie.workbench.common.dmn.client.editors.types.common.DataTypeManager;
import org.kie.workbench.common.dmn.client.editors.types.common.DataTypeUtils;
import org.uberfire.client.mvp.UberElemental;

import static org.kie.workbench.common.dmn.api.property.dmn.types.BuiltInType.BOOLEAN;

@Dependent
public class DataTypeSelect {

    private final View view;

    private final DataTypeUtils dataTypeUtils;

    private final DataTypeManager dataTypeManager;

    final static String STRUCTURE = "Structure";

    private DataType dataType;

    private DataTypeListItem listItem;

    private List<DataType> subDataTypes;

    @Inject
    public DataTypeSelect(final View view,
                          final DataTypeUtils dataTypeUtils,
                          final DataTypeManager dataTypeManager) {
        this.view = view;
        this.dataTypeUtils = dataTypeUtils;
        this.dataTypeManager = dataTypeManager;
    }

    @PostConstruct
    void setup() {
        view.init(this);
    }

    public HTMLElement getElement() {
        return view.getElement();
    }

    public void init(final DataTypeListItem gridItem,
                     final DataType dataType) {
        this.listItem = gridItem;
        this.dataType = dataType;
        this.view.setDataType(dataType);
        this.subDataTypes = dataType.getSubDataTypes();
    }

    void refresh() {
        view.setupDropdown();
    }

    DataType getDataType() {
        return dataType;
    }

    void enableEditMode() {
        refresh();
        view.enableEditMode();
    }

    void disableEditMode() {
        view.disableEditMode();
    }

    List<DataType> getDefaultDataTypes() {
        return dataTypeUtils.defaultDataTypes();
    }

    List<DataType> getCustomDataTypes() {
        return dataTypeUtils
                .customDataTypes()
                .stream()
                .filter(dataType -> !Objects.equals(dataType.getName(), getDataType().getName()))
                .collect(Collectors.toList());
    }

    void refreshView(final String typeName) {
        subDataTypes = dataTypeManager.from(getDataType()).makeExternalDataTypes(typeName);
        listItem.refreshSubItems(subDataTypes);
        listItem.refreshConstraintComponent();
    }

    boolean isIndirectBooleanOrStructure() {
        final String currentValue = getValue();
        return isIndirectBooleanOrStructure(currentValue);
    }

    boolean isIndirectBooleanOrStructure(final String currentValue) {
        final List<DataType> customDataTypes = getCustomDataTypes();
        final Optional<DataType> customType = customDataTypes.stream()
                                                             .filter(d -> d.getName().equals(currentValue))
                                                             .findFirst();

        if (customType.isPresent()) {
            final String type = customType.get().getType();
            return (type.equals(BOOLEAN.getName()) || type.equals(STRUCTURE)) || isIndirectBooleanOrStructure(type);
        }

        return false;
    }

    public String getValue() {
        return view.getValue();
    }

    List<DataType> getSubDataTypes() {
        return subDataTypes;
    }

    public interface View extends UberElemental<DataTypeSelect>,
                                  IsElement {

        void setupDropdown();

        void enableEditMode();

        void disableEditMode();

        void setDataType(final DataType type);

        String getValue();
    }
}
