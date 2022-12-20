package com.havit.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.havit.app.databinding.ActivityLoginBinding;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Editable name = binding.nameTextField.getText();
        Editable username = binding.usernameTextField.getText();
        Editable password = binding.passwordTextField.getText();

        Button button = binding.submitButton;

        button.setOnClickListener(v -> {

            String nameString = String.valueOf(name);
            String userNameString = String.valueOf(username);
            String passwordString = String.valueOf(password);

            // Check credentials
            boolean isAuthenticated = authenticate(nameString, userNameString, passwordString);

            if (isAuthenticated) {
                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getSupportActionBar()).hide();
    }

    @Override
    public void onStop() {
        super.onStop();
        Objects.requireNonNull(getSupportActionBar()).show();
    }

    private boolean authenticate(String name, String username, String password) {
        return name.equals("testing");
    }

    public void ShowHidePass(View view) {
    }
}
