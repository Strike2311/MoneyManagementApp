package com.kacpkuch.moneymanagementapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private Button sendEmailButton;
    private Button backButton;
    private EditText enteredEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        sendEmailButton = findViewById(R.id.submitButton);
        backButton = findViewById(R.id.forgotPasswordBackButton);
        enteredEmail = findViewById(R.id.forgotPasswordEmailText);


        sendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String enteredEmailText = enteredEmail.getText().toString();

                if (TextUtils.isEmpty(enteredEmailText)) {
                    Toast.makeText(ForgotPasswordActivity.this, "E-mail must be entered.", Toast.LENGTH_SHORT).show();
                } else {
                    auth.sendPasswordResetEmail(enteredEmailText).addOnSuccessListener(new OnSuccessListener() {
                        @Override
                        public void onSuccess(Object o) {
                            Toast.makeText(ForgotPasswordActivity.this, "E-mail has been sent.", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ForgotPasswordActivity.this, "Couldn't send an e-mail.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ForgotPasswordActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}