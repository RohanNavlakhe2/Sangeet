package com.yog.sangeet.util;

import android.text.Editable;
import android.text.TextWatcher;

//when we use this annotation this forces that there is exactly one abstract method.
//If not it gives compile time error
@FunctionalInterface
public interface SangeetAfterTextChangedWatcher extends TextWatcher {
    @Override
    default void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){

    }

    @Override
    default void onTextChanged(CharSequence charSequence, int i, int i1, int i2){

    }

    @Override
    void afterTextChanged(Editable editable);
}
