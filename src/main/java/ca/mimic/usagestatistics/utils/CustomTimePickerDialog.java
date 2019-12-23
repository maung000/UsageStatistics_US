package ca.mimic.usagestatistics.utils;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CustomTimePickerDialog extends TimePickerDialog {

    private final static int TIME_PICKER_INTERVAL = 5;
    private TimePicker mTimePicker;
//    private final OnTimeSetListener mTimeSetListener;

    public CustomTimePickerDialog(Context context,
                                  int hourOfDay, int minute, boolean is24HourView) {
        super(context, TimePickerDialog.THEME_HOLO_LIGHT, null, hourOfDay,
                minute / TIME_PICKER_INTERVAL, is24HourView);
//        mTimeSetListener = listener;
        fixSpinner(context, hourOfDay, minute, is24HourView);

    }

    /* In Android 7.0 Nougat, spinner mode for the TimePicker in TimePickerDialog is incorrectly displayed as clock */
    private void fixSpinner(Context context, int hourOfDay, int minute, boolean is24HourView) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) { // fixes the bug in API 24
            try {
                // Get the theme's android:timePickerMode
                final int MODE_SPINNER = 2;
                Class<?> styleableClass = Class.forName("com.android.internal.R$styleable");
                Field timePickerStyleableField = styleableClass.getField("TimePicker");
                int[] timePickerStyleable = (int[]) timePickerStyleableField.get(null);
                final TypedArray a = context.obtainStyledAttributes(null, timePickerStyleable, android.R.attr.timePickerStyle, 0);
                Field timePickerModeStyleableField = styleableClass.getField("TimePicker_timePickerMode");
                int timePickerModeStyleable = timePickerModeStyleableField.getInt(null);
                final int mode = a.getInt(timePickerModeStyleable, MODE_SPINNER);
                a.recycle();
                if (mode == MODE_SPINNER) {
                    mTimePicker = (TimePicker) findField(TimePickerDialog.class, TimePicker.class, "mTimePicker").get(this);
                    Class<?> delegateClass = Class.forName("android.widget.TimePicker$TimePickerDelegate");
                    Field delegateField = findField(TimePicker.class, delegateClass, "mDelegate");
                    Object delegate = delegateField.get(mTimePicker);
                    Class<?> spinnerDelegateClass;
                    spinnerDelegateClass = Class.forName("android.widget.TimePickerSpinnerDelegate");
                    // In 7.0 Nougat for some reason the timePickerMode is ignored and the delegate is TimePickerClockDelegate
                    if (delegate.getClass() != spinnerDelegateClass) {
                        delegateField.set(mTimePicker, null); // throw out the TimePickerClockDelegate!
                        mTimePicker.removeAllViews(); // remove the TimePickerClockDelegate views
                        Constructor spinnerDelegateConstructor = spinnerDelegateClass.getConstructor(TimePicker.class, Context.class, AttributeSet.class, int.class, int.class);
                        spinnerDelegateConstructor.setAccessible(true);
                        // Instantiate a TimePickerSpinnerDelegate
                        delegate = spinnerDelegateConstructor.newInstance(mTimePicker, context, null, android.R.attr.timePickerStyle, 0);
                        delegateField.set(mTimePicker, delegate); // set the TimePicker.mDelegate to the spinner delegate
                        // Set up the TimePicker again, with the TimePickerSpinnerDelegate
                        mTimePicker.setIs24HourView(is24HourView);
                        mTimePicker.setCurrentHour(hourOfDay);
                        mTimePicker.setCurrentMinute(minute/TIME_PICKER_INTERVAL);
                        mTimePicker.setOnTimeChangedListener(this);
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Field findField(Class objectClass, Class fieldClass, String expectedName) {
        try {
            Field field = objectClass.getDeclaredField(expectedName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
        } // ignore
        // search for it if it wasn't found under the expected ivar name
        for (Field searchField : objectClass.getDeclaredFields()) {
            if (searchField.getType() == fieldClass) {
                searchField.setAccessible(true);
                return searchField;
            }
        }
        return null;
    }

    @Override
    public void updateTime(int hourOfDay, int minuteOfHour) {
        mTimePicker.setCurrentHour(hourOfDay);
        mTimePicker.setCurrentMinute(minuteOfHour / TIME_PICKER_INTERVAL);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
//                if (mTimeSetListener != null) {
//                    mTimeSetListener.onTimeSet(mTimePicker, mTimePicker.getCurrentHour(),
//                            mTimePicker.getCurrentMinute() * TIME_PICKER_INTERVAL);
//                }
                break;
            case BUTTON_NEGATIVE:
                cancel();
                break;
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            Class<?> classForid = Class.forName("com.android.internal.R$id");
            Field timePickerField = classForid.getField("timePicker");
            mTimePicker = (TimePicker) findViewById(timePickerField.getInt(null));
            Field field = classForid.getField("minute");

            NumberPicker minuteSpinner = (NumberPicker) mTimePicker
                    .findViewById(field.getInt(null));
            minuteSpinner.setMinValue(0);
            minuteSpinner.setMaxValue((60 / TIME_PICKER_INTERVAL) - 1);
            List<String> displayedValues = new ArrayList<>();
            for (int i = 0; i < 60; i += TIME_PICKER_INTERVAL) {
                displayedValues.add(String.format("%02d", i));
            }
            minuteSpinner.setDisplayedValues(displayedValues
                    .toArray(new String[0]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}