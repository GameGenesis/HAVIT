package com.havit.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.havit.app.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private String[] userName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextInputEditText nameTextField = binding.nameTextField;
        TextInputEditText usernameTextField = binding.usernameTextField;
        TextInputEditText passwordTextField = binding.passwordTextField;

        String name = String.valueOf(usernameTextField.getText());
        String username = String.valueOf(usernameTextField.getText());
        String password = String.valueOf(usernameTextField.getText());

        Button button = binding.submitButton;

        button.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
}
