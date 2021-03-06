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

package org.kie.workbench.common.forms.jbpm.client.resources.i18n;

import org.jboss.errai.ui.shared.api.annotations.TranslationKey;

public interface Constants {

    @TranslationKey(defaultValue = "Business Process")
    public static final String Process = "JBPMFormModelCreationPresenter.Process";

    @TranslationKey(defaultValue = "Start Process Form")
    public static final String JBPMFormModelCreationViewImplStartProcessForm = "JBPMFormModelCreationViewImpl.StartProcessForm";

    @TranslationKey(defaultValue = "Form for Task {0} ({1})")
    public static final String JBPMFormModelCreationViewImplTaskName = "JBPMFormModelCreationViewImpl.TaskName";

    @TranslationKey(defaultValue = "There's no process or task selected")
    public static final String InvalidFormModel = "JBPMFormModelCreationPresenter.InvalidFormModel";
}
