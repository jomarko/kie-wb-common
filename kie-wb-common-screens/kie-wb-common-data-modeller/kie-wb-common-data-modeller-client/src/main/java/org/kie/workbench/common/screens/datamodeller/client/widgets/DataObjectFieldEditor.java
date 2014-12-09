/**
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.screens.datamodeller.client.widgets;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.ListBox;
import com.github.gwtbootstrap.client.ui.TextArea;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import org.guvnor.common.services.project.model.Project;
import org.jboss.errai.common.client.api.Caller;
import org.jboss.errai.common.client.api.RemoteCallback;
import org.kie.workbench.common.screens.datamodeller.client.DataModelerContext;
import org.kie.workbench.common.screens.datamodeller.client.resources.i18n.Constants;
import org.kie.workbench.common.screens.datamodeller.client.util.AnnotationValueHandler;
import org.kie.workbench.common.screens.datamodeller.client.util.DataModelerUtils;
import org.kie.workbench.common.screens.datamodeller.client.validation.ValidatorService;
import org.kie.workbench.common.screens.datamodeller.events.DataModelerEvent;
import org.kie.workbench.common.screens.datamodeller.events.DataObjectChangeEvent;
import org.kie.workbench.common.screens.datamodeller.events.DataObjectFieldChangeEvent;
import org.kie.workbench.common.screens.datamodeller.events.DataObjectFieldDeletedEvent;
import org.kie.workbench.common.screens.datamodeller.events.DataObjectFieldSelectedEvent;
import org.kie.workbench.common.screens.datamodeller.events.DataObjectSelectedEvent;
import org.kie.workbench.common.screens.datamodeller.model.AnnotationDefinitionTO;
import org.kie.workbench.common.screens.datamodeller.model.AnnotationTO;
import org.kie.workbench.common.screens.datamodeller.model.DataModelTO;
import org.kie.workbench.common.screens.datamodeller.model.DataObjectTO;
import org.kie.workbench.common.screens.datamodeller.model.ObjectPropertyTO;
import org.kie.workbench.common.screens.datamodeller.service.DataModelerService;
import org.uberfire.backend.vfs.Path;
import org.uberfire.ext.editor.commons.client.validation.ValidatorCallback;
import org.uberfire.ext.widgets.common.client.common.popups.errors.ErrorPopup;

public class DataObjectFieldEditor extends Composite {

    interface DataObjectFieldEditorUIBinder
            extends UiBinder<Widget, DataObjectFieldEditor> {

    };

    private static DataObjectFieldEditorUIBinder uiBinder = GWT.create(DataObjectFieldEditorUIBinder.class);

    private static final String DEFAULT_LABEL_CLASS = "gwt-Label";

    private static final String TEXT_ERROR_CLASS = "text-error";

    @UiField
    Label nameLabel;

    @UiField
    TextBox name;

    @UiField
    TextBox label;

    @UiField
    TextArea description;

    @UiField
    ListBox typeSelector;

    @UiField
    CheckBox equalsSelector;

    @UiField
    Icon equalsHelpIcon;

    @UiField
    Label positionLabel;

    @UiField
    Icon positionHelpIcon;

    @UiField
    ListBox positionSelector;

    @Inject
    Event<DataModelerEvent> dataModelerEventEvent;

    @Inject
    private ValidatorService validatorService;

    private DataObjectTO dataObject;

    private ObjectPropertyTO objectField;

    private DataModelerContext context;

    private boolean readonly = true;

    @Inject
    private Caller<DataModelerService> modelerService;

    public DataObjectFieldEditor() {
        initWidget(uiBinder.createAndBindUi(this));

        typeSelector.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                typeChanged(event);
            }
        });

        positionSelector.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent event) {
                positionChanged(event);
            }
        });

        //positionHelpIcon.getElement().getStyle().setPaddingLeft(4, Style.Unit.PX);
        positionHelpIcon.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        //equalsHelpIcon.getElement().getStyle().setPaddingLeft(4, Style.Unit.PX);
        equalsHelpIcon.getElement().getStyle().setCursor(Style.Cursor.POINTER);

        setReadonly(true);
    }

    public DataObjectTO getDataObject() {
        return dataObject;
    }

    public void setDataObject(DataObjectTO dataObject) {
        this.dataObject = dataObject;
    }

    public ObjectPropertyTO getObjectField() {
        return objectField;
    }

    public void setObjectField(ObjectPropertyTO objectField) {
        this.objectField = objectField;
    }

    public DataModelerContext getContext() {
        return context;
    }

    public void setContext(DataModelerContext context) {
        this.context = context;
        initTypeList();
    }

    private DataModelTO getDataModel() {
        return getContext() != null ? getContext().getDataModel() : null;
    }

    private Project getProject() {
        return getContext() != null ? getContext().getCurrentProject() : null;
    }

    private void setReadonly(boolean readonly) {
        this.readonly = readonly;
        boolean value = !readonly;

        name.setEnabled(value);
        label.setEnabled(value);
        description.setEnabled(value);
        typeSelector.setEnabled(value);
        equalsSelector.setEnabled(value);
        positionSelector.setEnabled(value);
    }

    private boolean isReadonly() {
        return readonly;
    }

    // Event notifications

    private void notifyFieldChange(String memberName, Object oldValue, Object newValue) {
        DataObjectFieldChangeEvent changeEvent = new DataObjectFieldChangeEvent(DataModelerEvent.DATA_OBJECT_FIELD_EDITOR, getDataModel(), getDataObject(), getObjectField(), memberName, oldValue, newValue);
        // Notify helper directly
        getContext().getHelper().dataModelChanged(changeEvent);
        dataModelerEventEvent.fire(changeEvent);
    }

    // Event observers
    private void onDataObjectFieldSelected(@Observes DataObjectFieldSelectedEvent event) {
        if (event.isFrom(getDataModel())) {
            loadDataObjectField(event.getCurrentDataObject(), event.getCurrentField());
        }
    }

    private void onDataObjectFieldDeleted(@Observes DataObjectFieldDeletedEvent event) {
        // When all attributes from the current object have been deleted clean
        if (event.isFrom(getDataModel())) {
            if (getDataObject().getProperties().size() == 0) {
                clean();
                setReadonly(true);
            }
        }
    }

    private void onDataObjectChange(@Observes DataObjectChangeEvent event) {
        if (event.isFrom(getDataModel())) {
            if ("name".equals(event.getPropertyName()) ||
                    "packageName".equals(event.getPropertyName()) ||
                    "label".equals(event.getPropertyName())) {

                initTypeList();
            }
        }
    }

    private void onDataObjectSelected(@Observes DataObjectSelectedEvent event) {
        if (event.isFrom(getDataModel())) {
            clean();
            setDataObject(event.getCurrentDataObject());
            setObjectField(null);
        }
    }

    private void loadDataObjectField(DataObjectTO dataObject, ObjectPropertyTO objectField) {
        clean();
        setReadonly(true);
        if (dataObject != null && objectField != null) {
            setDataObject(dataObject);
            setObjectField(objectField);
            initTypeList();

            initPositions();

            name.setText(getObjectField().getName());

            AnnotationTO annotation = objectField.getAnnotation(AnnotationDefinitionTO.LABEL_ANNOTATION);
            if (annotation != null) {
                label.setText( (String) annotation.getValue(AnnotationDefinitionTO.VALUE_PARAM) );
            }

            annotation = objectField.getAnnotation(AnnotationDefinitionTO.DESCRIPTION_ANNOTATION);
            if (annotation != null) {
                description.setText( (String) annotation.getValue(AnnotationDefinitionTO.VALUE_PARAM));
            }

            annotation = objectField.getAnnotation(AnnotationDefinitionTO.KEY_ANNOTATION);
            if (annotation != null) {
                equalsSelector.setValue(Boolean.TRUE);
            }

            annotation = objectField.getAnnotation(AnnotationDefinitionTO.POSITION_ANNOTATION );
            if (annotation != null) {
                String position = (String) annotation.getValue(AnnotationDefinitionTO.VALUE_PARAM);
                positionSelector.setSelectedValue(position);
            }

            setReadonly( getContext() == null || getContext().isReadonly() );
        } else {
            initTypeList();
        }
    }

    // TODO listen to DataObjectFieldDeletedEvent?

    // Event handlers
    @UiHandler("name")
    void nameChanged(ValueChangeEvent<String> event) {
        if (getObjectField() == null) return;
        // Set widgets to errorpopup for styling purposes etc.
        nameLabel.setStyleName(DEFAULT_LABEL_CLASS);

        final String oldValue = getObjectField().getName();
        final String newValue = DataModelerUtils.unCapitalize(name.getValue());

        final String originalClassName = getContext() != null ? getContext().getEditorModelContent().getOriginalClassName() : null;
        final String fieldName = oldValue;

        if ( originalClassName != null ) {
            modelerService.call( new RemoteCallback<List<Path>>() {

                @Override public void callback( List<Path> paths ) {

                    if ( paths != null && paths.size() > 0 ) {
                        //If usages for this field were detected in project assets
                        //show the confirmation message to the user.

                        ShowUsagesPopup showUsagesPopup = ShowUsagesPopup.newUsagesPopupForRenaming(
                                Constants.INSTANCE.modelEditor_confirm_renaming_of_used_field( oldValue ),
                                paths,
                                new org.uberfire.mvp.Command() {
                                    @Override
                                    public void execute() {
                                        doFieldNameChange( oldValue, newValue );
                                    }
                                },
                                new org.uberfire.mvp.Command() {
                                    @Override public void execute() {
                                        //do nothing.
                                        name.setValue( oldValue );
                                    }
                                }
                        );

                        showUsagesPopup.setCloseVisible( false );
                        showUsagesPopup.show();

                    } else {
                        //no usages, just proceed with the deletion.
                        doFieldNameChange( oldValue, newValue );
                    }
                }
            } ).findFieldUsages( originalClassName, fieldName );
        } else {
            doFieldNameChange( oldValue, fieldName );
        }
    }

    private void doFieldNameChange( final String oldValue, final String newValue ) {

        final Command afterCloseCommand = new Command() {
            @Override
            public void execute() {
                nameLabel.setStyleName(TEXT_ERROR_CLASS);
                name.selectAll();
            }
        };

        // In case an invalid name (entered before), was corrected to the original value, don't do anything but reset the label style
        if (oldValue.equalsIgnoreCase(name.getValue())) {
            name.setText(oldValue);
            nameLabel.setStyleName(DEFAULT_LABEL_CLASS);
            return;
        }

        validatorService.isValidIdentifier(newValue, new ValidatorCallback() {
            @Override
            public void onFailure() {
                ErrorPopup.showMessage( Constants.INSTANCE.validation_error_invalid_object_attribute_identifier( newValue ), null, afterCloseCommand );
            }

            @Override
            public void onSuccess() {
                validatorService.isUniqueAttributeName(newValue, getDataObject(), new ValidatorCallback() {
                    @Override
                    public void onFailure() {
                        ErrorPopup.showMessage( Constants.INSTANCE.validation_error_object_attribute_already_exists(newValue), null, afterCloseCommand );
                    }

                    @Override
                    public void onSuccess() {
                        nameLabel.setStyleName(DEFAULT_LABEL_CLASS);
                        objectField.setName(newValue);
                        notifyFieldChange("name", oldValue, newValue);
                    }
                });
            }
        });
    }

    @UiHandler("label")
    void labelChanged(final ValueChangeEvent<String> event) {
        if (getObjectField() == null) return;

        String oldValue = null;
        final String _label = label.getValue();
        AnnotationTO annotation = getObjectField().getAnnotation(AnnotationDefinitionTO.LABEL_ANNOTATION);

        if (annotation != null) {
            oldValue = AnnotationValueHandler.getInstance().getStringValue(annotation, AnnotationDefinitionTO.VALUE_PARAM);
            if ( _label != null && !"".equals(_label) ) annotation.setValue(AnnotationDefinitionTO.VALUE_PARAM, _label);
            else getObjectField().removeAnnotation(annotation);
        } else {
            if ( _label != null && !"".equals(_label) ) {
                getObjectField().addAnnotation(getContext().getAnnotationDefinitions().get(AnnotationDefinitionTO.LABEL_ANNOTATION), AnnotationDefinitionTO.VALUE_PARAM, _label );
            }
        }
        // TODO replace 'label' literal with annotation definition constant
        notifyFieldChange("label", oldValue, _label);
    }

    @UiHandler("description")
    void descriptionChanged(final ValueChangeEvent<String> event) {
        if (getObjectField() == null) return;

        String oldValue = null;
        final String _description = description.getValue();
        AnnotationTO annotation = getObjectField().getAnnotation(AnnotationDefinitionTO.DESCRIPTION_ANNOTATION);

        if (annotation != null) {
            oldValue = AnnotationValueHandler.getInstance().getStringValue(annotation, AnnotationDefinitionTO.VALUE_PARAM);
            if ( _description != null && !"".equals(_description) ) annotation.setValue(AnnotationDefinitionTO.VALUE_PARAM, _description);
            else getObjectField().removeAnnotation(annotation);
        } else {
            if ( _description != null && !"".equals(_description) ) {
                getObjectField().addAnnotation(getContext().getAnnotationDefinitions().get(AnnotationDefinitionTO.DESCRIPTION_ANNOTATION), AnnotationDefinitionTO.VALUE_PARAM, _description );
            }
        }
        notifyFieldChange(AnnotationDefinitionTO.DESCRIPTION_ANNOTATION, oldValue, _description);
    }

    private void typeChanged(ChangeEvent event) {
        if (getObjectField() == null) return;

        String oldValue = getObjectField().getClassName();
        String type = typeSelector.getValue();
        if (DataModelerUtils.isMultipleType(type)) {
            type = DataModelerUtils.getCanonicalClassName(type);
            getObjectField().setMultiple(true);
            if (getObjectField().getBag() == null) {
                getObjectField().setBag( ObjectPropertyTO.DEFAULT_PROPERTY_BAG );
            }
        } else {
            getObjectField().setMultiple(false);
        }
        getObjectField().setClassName(type);

        if (getContext().getHelper().isBaseType(type)) {
            getObjectField().setBaseType(true);
        } else {
            // Un-reference former type reference and set the new one
            getObjectField().setBaseType(false);
            getContext().getHelper().dataObjectUnReferenced(oldValue, getDataObject().getClassName());
            getContext().getHelper().dataObjectReferenced(type, getDataObject().getClassName());
        }
        notifyFieldChange("className", oldValue, type);
    }

    @UiHandler("equalsSelector")
    void equalsChanged(final ClickEvent event) {
        if (getObjectField() == null) return;

        Boolean oldEquals = null;
        AnnotationTO annotation = getObjectField().getAnnotation(AnnotationDefinitionTO.KEY_ANNOTATION);
        if (annotation != null) {
            Object annotationValue = annotation.getValue(AnnotationDefinitionTO.VALUE_PARAM);
            oldEquals = annotationValue != null ? (Boolean) annotationValue : Boolean.FALSE;
        }
        final Boolean setEquals = equalsSelector.getValue();

        if (annotation != null && !setEquals) getObjectField().removeAnnotation(annotation);
        else if (annotation == null && setEquals) getObjectField().addAnnotation(new AnnotationTO(getContext().getAnnotationDefinitions().get(AnnotationDefinitionTO.KEY_ANNOTATION)));
        notifyFieldChange(AnnotationDefinitionTO.KEY_ANNOTATION, oldEquals, setEquals);
    }

    void positionChanged(ChangeEvent event) {
        if (getObjectField() == null) return;

        AnnotationTO annotation = getObjectField().getAnnotation(AnnotationDefinitionTO.POSITION_ANNOTATION );
        final String oldPosition = (annotation != null) ? annotation.getValue(AnnotationDefinitionTO.VALUE_PARAM).toString() : "-1";
        final String newPosition = positionSelector.getValue();

        DataModelerUtils.recalculatePositions(getDataObject(), Integer.parseInt(oldPosition, 10), Integer.parseInt(newPosition, 10));

        notifyFieldChange(AnnotationDefinitionTO.POSITION_ANNOTATION, oldPosition, newPosition);
    }

    private void initTypeList() {

        String currentFieldType = null;
        boolean currentFieldTypeMultiple = false;

        if (getDataModel() != null) {
            if (getDataObject() != null && getObjectField() != null) {
                currentFieldType = getObjectField().getClassName();
                currentFieldTypeMultiple = getObjectField().isMultiple();
            }
            DataModelerUtils.initTypeList( typeSelector, getContext().getHelper().getOrderedBaseTypes().values(), getDataModel().getDataObjects(), getDataModel().getExternalClasses(), currentFieldType, currentFieldTypeMultiple);
        } else {
            DataModelerUtils.initList( typeSelector, false );
        }
    }

    public void refreshTypeList( boolean keepSelection ) {
        String selectedValue = typeSelector.getValue();
        initTypeList();
        if ( keepSelection && selectedValue != null ) {
            typeSelector.setSelectedValue( selectedValue );
        }
    }

    private void setSelectedType() {
        String type = getObjectField() != null ?
                (getObjectField().getClassName() + (getObjectField().isMultiple() ? DataModelerUtils.MULTIPLE:"")) : null;
        typeSelector.setSelectedValue(type);
    }

    private void initPositions() {
        positionSelector.clear();
        List<ObjectPropertyTO> properties = null;
        if (getDataModel() != null && getDataObject() != null && (properties = getDataObject().getProperties()) != null && properties.size() > 0) {
            SortedMap<Integer, String> positions = new TreeMap<Integer, String>();
            String positionValue;
            Integer positionIntValue;
            for (ObjectPropertyTO propertyTO : properties) {
                positionValue = AnnotationValueHandler.getInstance().getStringValue(propertyTO, AnnotationDefinitionTO.POSITION_ANNOTATION, "value");
                if (positionValue != null) {
                    try {
                        positionIntValue = new Integer(positionValue);
                        positions.put(positionIntValue, positionValue);
                    } catch (NumberFormatException e) {
                    }
                }
            }
            for (Map.Entry<Integer, String> position : positions.entrySet()) {
                positionSelector.addItem(position.getValue(), position.getValue());
            }
        }
    }

    private void clean() {
        nameLabel.setStyleName(DEFAULT_LABEL_CLASS);
        name.setText(null);
        label.setText(null);
        description.setText(null);
        typeSelector.setSelectedValue(null);
        equalsSelector.setValue(Boolean.FALSE);
        positionLabel.setStyleName(DEFAULT_LABEL_CLASS);
        positionSelector.clear();
    }
}