package com.example.brett.sunshine.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

import com.example.brett.sunshine.R;

public final class LocationEditTextPreference extends EditTextPreference {

    private static final int DEFAULT_MINIMUM_LOCATION_LENGTH = 3;

    private int minimumLength = DEFAULT_MINIMUM_LOCATION_LENGTH;

    public LocationEditTextPreference(Context context, AttributeSet attributes){
        super(context, attributes);

        TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(attributes, R.styleable.LocationEditTextPreference, 0,0);

        try{
            minimumLength = styledAttributes.getInteger(R.styleable.LocationEditTextPreference_minLength, DEFAULT_MINIMUM_LOCATION_LENGTH);
        } finally {
            styledAttributes.recycle();
        }
    }


    @Override
    protected void showDialog(Bundle state) {
        super.showDialog(state);


        EditText et = getEditText();
        et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }


            @Override
            public void afterTextChanged(Editable s) {

                Dialog d = getDialog();

                if(d instanceof AlertDialog){

                    AlertDialog alertDialog = (AlertDialog)d;

                    Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

                    if(s.length() < minimumLength){
                        positiveButton.setEnabled(false);
                    } else {
                        positiveButton.setEnabled(true);
                    }
                }
            }
        });

    }
}
