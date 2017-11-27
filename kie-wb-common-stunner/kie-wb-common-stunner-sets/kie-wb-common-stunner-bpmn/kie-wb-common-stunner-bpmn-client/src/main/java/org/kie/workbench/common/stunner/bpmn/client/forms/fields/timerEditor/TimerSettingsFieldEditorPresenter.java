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

package org.kie.workbench.common.stunner.bpmn.client.forms.fields.timerEditor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.kie.workbench.common.stunner.bpmn.definition.property.event.timer.TimerSettingsValue;
import org.uberfire.client.mvp.UberElement;
import org.uberfire.commons.data.Pair;

public class TimerSettingsFieldEditorPresenter {

    protected enum DISPLAY_MODE {
        DURATION_TIMER,
        MULTIPLE_TIMER,
        DATE_TIMER
    }

    protected enum TIME_CYCLE_LANGUAGE {
        ISO("ISO",
            "none"),
        CRON("Cron",
             "cron");

        private final String text;

        private final String value;

        TIME_CYCLE_LANGUAGE(String text,
                            String value) {
            this.text = text;
            this.value = value;
        }

        public String text() {
            return text;
        }

        public String value() {
            return value;
        }
    }

    final static List<Pair<String, String>> timeCycleOptions = new ArrayList<Pair<String, String>>() {
        {
            add(new Pair<>(TIME_CYCLE_LANGUAGE.ISO.text(),
                           TIME_CYCLE_LANGUAGE.ISO.value()));
            add(new Pair<>(TIME_CYCLE_LANGUAGE.CRON.text(),
                           TIME_CYCLE_LANGUAGE.CRON.value()));
        }
    };

    public interface View extends UberElement<TimerSettingsFieldEditorPresenter> {

        void setMultipleTimerChecked(boolean value);

        void setDurationTimerChecked(boolean value);

        void setDateTimerChecked(boolean value);

        void showMultipleTimerParams(boolean show);

        void showDurationTimerParams(boolean show);

        void showDateTimerParams(boolean show);

        void showTimeDate(boolean show);

        void showTimeDateTimePicker(boolean show);

        void setTimeDateTimePickerValue(String value);

        void setTimeDateTimePickerValue(Date value);

        void setTimeDuration(String timeDuration);

        String getTimeDuration();

        void setTimeDate(String timeDate);

        String getTimeDate();

        Date getTimeDateTimePickerValue();

        void setTimeCycle(String timeCycle);

        String getTimeCycle();

        void setTimeCycleLanguageOptions(List<Pair<String, String>> options,
                                         String selectedValue);

        void setTimeCycleLanguage(String timeCycleLanguage);

        String getTimeCycleLanguage();

        void clear();

        Date parseFromISO(final String value) throws IllegalArgumentException;

        String formatToISO(final Date value);
    }

    public interface ValueChangeHandler {

        void onValueChange(TimerSettingsValue oldValue,
                           TimerSettingsValue newValue);
    }

    private final View view;

    private TimerSettingsValue value;

    private List<ValueChangeHandler> changeHandlers = new ArrayList<>();

    @Inject
    public TimerSettingsFieldEditorPresenter(final View view) {
        this.view = view;
    }

    @PostConstruct
    public void init() {
        view.init(this);
        view.setTimeCycleLanguageOptions(timeCycleOptions,
                                         TIME_CYCLE_LANGUAGE.ISO.value());
        setDisplayMode(DISPLAY_MODE.DURATION_TIMER,
                       true);
        view.showTimeDateTimePicker(false);
    }

    public View getView() {
        return view;
    }

    public void setValue(TimerSettingsValue value) {
        view.clear();
        this.value = value;
        setDisplayMode(DISPLAY_MODE.DURATION_TIMER,
                       true);
        if (value != null) {
            if (value.getTimeDate() != null) {
                setDisplayMode(DISPLAY_MODE.DATE_TIMER,
                               true);
                view.setTimeDate(value.getTimeDate());
            } else if (value.getTimeDuration() != null) {
                setDisplayMode(DISPLAY_MODE.DURATION_TIMER,
                               true);
                view.setTimeDuration(value.getTimeDuration());
            } else if (value.getTimeCycleLanguage() != null) {
                setDisplayMode(DISPLAY_MODE.MULTIPLE_TIMER,
                               true);
                view.setTimeCycleLanguage(value.getTimeCycleLanguage());
                view.setTimeCycle(value.getTimeCycle());
            }
        }
    }

    public TimerSettingsValue getValue() {
        return value;
    }

    public void addChangeHandler(ValueChangeHandler changeHandler) {
        if (!changeHandlers.contains(changeHandler)) {
            changeHandlers.add(changeHandler);
        }
    }

    protected void onTimerDurationChange() {
        TimerSettingsValue oldValue = value;
        value = copy(oldValue,
                     true);
        value.setTimeDuration(view.getTimeDuration());
        value.setTimeDate(null);
        value.setTimeCycle(null);
        value.setTimeCycleLanguage(null);

        notifyChange(oldValue,
                     value);
    }

    protected void onTimeCycleChange() {
        onMultipleTimerValuesChange();
    }

    protected void onTimeCycleLanguageChange() {
        onMultipleTimerValuesChange();
    }

    protected void onMultipleTimerValuesChange() {
        TimerSettingsValue oldValue = value;
        value = copy(oldValue,
                     true);
        value.setTimeCycleLanguage(view.getTimeCycleLanguage());
        value.setTimeCycle(view.getTimeCycle());

        value.setTimeDuration(null);
        value.setTimeDate(null);

        notifyChange(oldValue,
                     value);
    }

    protected void onTimeDateChange() {
        TimerSettingsValue oldValue = value;
        value = copy(oldValue,
                     true);
        value.setTimeDate(view.getTimeDate());

        value.setTimeDuration(null);
        value.setTimeCycle(null);
        value.setTimeCycleLanguage(null);

        notifyChange(oldValue,
                     value);
    }

    private void setDisplayMode(DISPLAY_MODE mode,
                                boolean setRadioChecked) {
        view.showDurationTimerParams(false);
        view.showMultipleTimerParams(false);
        view.showDateTimerParams(false);

        switch (mode) {
            case DURATION_TIMER:
                view.showDurationTimerParams(true);
                if (setRadioChecked) {
                    view.setDurationTimerChecked(true);
                }
                break;
            case MULTIPLE_TIMER:
                view.showMultipleTimerParams(true);
                if (setRadioChecked) {
                    view.setMultipleTimerChecked(true);
                }
                break;
            case DATE_TIMER:
                view.showDateTimerParams(true);
                if (setRadioChecked) {
                    view.setDateTimerChecked(true);
                }
                break;
        }
    }

    protected void onMultipleTimerSelected() {
        setDisplayMode(DISPLAY_MODE.MULTIPLE_TIMER,
                       false);
        onMultipleTimerValuesChange();
    }

    protected void onDurationTimerSelected() {
        setDisplayMode(DISPLAY_MODE.DURATION_TIMER,
                       false);
        onTimerDurationChange();
    }

    protected void onDateTimerSelected() {
        setDisplayMode(DISPLAY_MODE.DATE_TIMER,
                       false);
        onTimeDateChange();
    }

    public void onShowTimeDateTimePicker() {
        view.showTimeDate(false);
        String value = view.getTimeDate();
        try {
            Date date = view.parseFromISO(value);
            view.setTimeDateTimePickerValue(date);
        } catch (IllegalArgumentException e) {
            view.setTimeDateTimePickerValue(value);
        }
        view.showTimeDateTimePicker(true);
    }

    public void onTimeDateTimePickerChange() {
        Date value = view.getTimeDateTimePickerValue();
        view.setTimeDate(view.formatToISO(value));
        onTimeDateChange();
    }

    public void onTimeDateTimePickerHidden() {
        view.showTimeDate(true);
        view.showTimeDateTimePicker(false);
    }

    private TimerSettingsValue copy(TimerSettingsValue source,
                                    boolean createIfSourceNull) {
        if (source == null) {
            return createIfSourceNull ? new TimerSettingsValue() : null;
        }
        TimerSettingsValue copy = new TimerSettingsValue();
        copy.setTimeDuration(source.getTimeDuration());
        copy.setTimeDate(source.getTimeDate());
        copy.setTimeCycle(source.getTimeCycle());
        copy.setTimeCycleLanguage(source.getTimeCycleLanguage());
        return copy;
    }

    private void notifyChange(TimerSettingsValue oldValue,
                              TimerSettingsValue newValue) {
        changeHandlers.forEach(changeHandler -> changeHandler.onValueChange(oldValue,
                                                                            newValue));
    }
}