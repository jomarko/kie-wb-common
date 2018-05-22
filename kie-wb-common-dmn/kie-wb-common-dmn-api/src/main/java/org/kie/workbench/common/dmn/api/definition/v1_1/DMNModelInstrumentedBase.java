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
package org.kie.workbench.common.dmn.api.definition.v1_1;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.kie.workbench.common.dmn.api.definition.DMNDefinition;
import org.kie.workbench.common.dmn.api.property.dmn.QName;
import org.kie.workbench.common.stunner.core.definition.builder.Builder;

public abstract class DMNModelInstrumentedBase implements DMNDefinition {

    public static final String PREFIX_FEEL = "feel";
    public static final String PREFIX_DMN = "dmn";
    public static final String PREFIX_KIE = "kie";

    public static final String URI_FEEL = org.kie.dmn.model.v1_1.DMNModelInstrumentedBase.URI_FEEL;
    public static final String URI_DMN = org.kie.dmn.model.v1_1.DMNModelInstrumentedBase.URI_DMN;
    public static final String URI_KIE = org.kie.dmn.model.v1_1.DMNModelInstrumentedBase.URI_KIE;

    private Map<String, String> nsContext = new HashMap<>();
    private Map<QName, String> additionalAttributes = new HashMap<>();

    private DMNModelInstrumentedBase parent;

    @NonPortable
    protected static abstract class BaseNodeBuilder<T extends DMNModelInstrumentedBase> implements Builder<T> {

    }

    // -----------------------
    // DMN properties
    // -----------------------

    @Override
    public Map<String, String> getNsContext() {
        return nsContext;
    }

    public void setAdditionalAttributes(Map<QName, String> additionalAttributes) {
        this.additionalAttributes = additionalAttributes;
    }

    public Map<QName, String> getAdditionalAttributes() {
        return additionalAttributes;
    }

    public DMNModelInstrumentedBase getParent() {
        return parent;
    }

    public void setParent(final DMNModelInstrumentedBase parent) {
        this.parent = parent;
    }

    public Optional<String> getPrefixForNamespaceURI(final String namespaceURI) {
        if (this.nsContext != null && this.nsContext.containsValue(namespaceURI)) {
            return this.nsContext.entrySet().stream().filter(kv -> kv.getValue().equals(namespaceURI)).findFirst().map(Map.Entry::getKey);
        }
        if (this.parent != null) {
            return parent.getPrefixForNamespaceURI(namespaceURI);
        }
        return Optional.empty();
    }
}
