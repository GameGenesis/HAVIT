package com.havit.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.havit.app.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {
    private String[] userName;
    private EditText test;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextInputEditText nameTextField = binding.nameTextField;
        TextInputEditText usernameTextField = binding.usernameTextField;
        TextInputEditText passwordTextField = binding.passwordTextField;

        test = (EditText) usernameTextField.getText();
        String username = String.valueOf(usernameTextField.getText());
        String password = String.valueOf(usernameTextField.getText());


        Button button = binding.submitButton;

        button.setOnClickListener(v -> {

            // Get entered email/username and password
            String testString = test.getText().toString();

            // Check credentials
            boolean isAuthenticated = authenticate(testString);

            if (isAuthenticated) {
                // Credentials are valid, proceed to next activity
//                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                startActivity(intent);
                Intent i = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(i);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                // Credentials are invalid, show error message
                Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            }



        });
    }

    // Method to authenticate email and password
    private boolean authenticate(String test) {
        // Replace this with your own authentication logic
        return test.equals("testing");
    }
}
