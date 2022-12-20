package com.havit.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.havit.app.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Button button = binding.submitButton;

        button.setOnClickListener(v -> {
            TextInputLayout textInputLayout = findViewById(R.id.nameField);
            EditText editText = textInputLayout.getEditText();
            String text = editText.getText().toString();
            Log.d("Name field", text);
            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
}
