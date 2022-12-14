package com.havit.app.ui.login;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.havit.app.R;

public class LoginViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public LoginViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is login page");
    }

    public LiveData<String> getText() {
        return mText;
    }

//    EditText editText = (EditText) findViewById(R.id.textView);
//    View.OnFocusChangeListener ofcListener = new MyFocusChangeListener();
//    editText.setOnFocusChangeListener(ofcListener);
//
//    private class MyFocusChangeListener implements View.OnFocusChangeListener {
//
//        public void onFocusChange(View v, boolean hasFocus){
//
//            if(v.getId() == R.id.textView && !hasFocus) {
//
//                InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
//
//            }
//        }
//    }

}