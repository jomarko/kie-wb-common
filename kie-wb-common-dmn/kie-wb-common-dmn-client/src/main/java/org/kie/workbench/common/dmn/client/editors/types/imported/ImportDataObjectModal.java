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

package org.kie.workbench.common.dmn.client.editors.types.imported;

import java.util.List;
import java.util.function.Consumer;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.kie.workbench.common.dmn.api.editors.types.DataObject;
import org.kie.workbench.common.dmn.client.api.dataobjects.DMNDataObjectsClient;
import org.uberfire.ext.editor.commons.client.file.popups.elemental2.Elemental2Modal;

@Dependent
public class ImportDataObjectModal extends Elemental2Modal<ImportDataObjectModal.View> {

    private final DMNDataObjectsClient client;

    @Inject
    public ImportDataObjectModal(final View view,
                                 final DMNDataObjectsClient client) {
        super(view);
        this.client = client;
    }

    @Override
    public void show() {
        client.loadDataObjects(getConsumer());
    }

    Consumer<List<DataObject>> getConsumer() {
        return objects -> {
            getView().clear();
            if (!objects.isEmpty()) {
                getView().addItems(objects);
            } else {
                getView().clear();
            }
            superShow();
        };
    }

    void superShow() {
        super.show();
    }

    public interface View extends Elemental2Modal.View<ImportDataObjectModal> {

        void addItems(final List<DataObject> dataObjects);

        void clear();
    }
}
