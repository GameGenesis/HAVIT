package com.havit.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.havit.app.databinding.ActivityLoginBinding;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    // private FirebaseAuth mAuth;

    private boolean isAuthenticated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Button button = binding.submitButton;

        button.setOnClickListener(v -> {
            EditText nameEditText = binding.nameTextLayout.getEditText();
            EditText userNameEditText = binding.usernameTextLayout.getEditText();
            EditText passwordEditText = binding.passwordTextLayout.getEditText();

            String nameStr, usernameStr, passwordStr;

            if (nameEditText != null && userNameEditText != null && passwordEditText != null) {
                nameStr = nameEditText.getText().toString();
                usernameStr = userNameEditText.getText().toString();
                passwordStr = userNameEditText.getText().toString();

                isAuthenticated = authenticate(nameStr, usernameStr, passwordStr);
            }

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
