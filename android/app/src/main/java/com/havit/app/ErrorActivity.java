package com.havit.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.havit.app.databinding.ActivityErrorBinding;

import java.util.Objects;

public class ErrorActivity extends AppCompatActivity {

    /**
     * Sets up the layout for the activity, hides the action bar,
     * and sets up a button to navigate to the LoginActivity when clicked.
     * @param savedInstanceState a Bundle containing the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Objects.requireNonNull(getSupportActionBar()).hide();

        ActivityErrorBinding binding = ActivityErrorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Button button = binding.tryAgainButton;
        button.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }
}