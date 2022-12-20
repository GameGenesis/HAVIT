package com.havit.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.havit.app.databinding.ActivityLoginBinding;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Button button = binding.submitButton;

        button.setOnClickListener(v -> {
            TextInputLayout nameTextLayout = binding.nameTextLayout;
            TextInputLayout userNameTextLayout = binding.usernameTextLayout;
            TextInputLayout passwordTextLayout = binding.passwordTextLayout;

            EditText nameEditText = nameTextLayout.getEditText();
            EditText userNameEditText = userNameTextLayout.getEditText();
            EditText passwordEditText = passwordTextLayout.getEditText();

            assert nameEditText != null;
            String nameStr = nameEditText.getText().toString();

            assert userNameEditText != null;
            String usernameStr = userNameEditText.getText().toString();

            assert passwordEditText != null;
            String passwordStr = userNameEditText.getText().toString();

            boolean isAuthenticated = authenticate(nameStr, usernameStr, passwordStr);

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
}
