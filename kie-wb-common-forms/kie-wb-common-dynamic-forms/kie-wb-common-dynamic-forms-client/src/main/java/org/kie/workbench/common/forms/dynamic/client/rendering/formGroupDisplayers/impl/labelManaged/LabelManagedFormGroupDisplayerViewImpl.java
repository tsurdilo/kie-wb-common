/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.forms.dynamic.client.rendering.formGroupDisplayers.impl.labelManaged;

import javax.inject.Inject;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.kie.workbench.common.forms.model.FieldDefinition;

@Templated
public class LabelManagedFormGroupDisplayerViewImpl extends Composite implements LabelManagedFormGroupDisplayerView {

    @Inject
    @DataField
    protected FlowPanel fieldContainer;
    @DataField
    protected Element helpBlock = DOM.createDiv();

    public void render(Widget widget,
                       FieldDefinition field) {
        this.getElement().setId(generateFormGroupId(field));
        fieldContainer.add(widget);
        helpBlock.setId(generateHelpBlockId(field));
    }
}
