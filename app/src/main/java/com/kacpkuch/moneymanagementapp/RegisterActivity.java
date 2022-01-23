package com.kacpkuch.moneymanagementapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private EditText nickname;
    private Button registerButton;
    private Button backButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        email = findViewById(R.id.emailText);
        password = findViewById(R.id.passwordText);
        confirmPassword = findViewById(R.id.passwordText2);
        registerButton = findViewById(R.id.signUpButton);
        backButton = findViewById(R.id.backButton);
        nickname = findViewById(R.id.nicknameText);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredEmail = email.getText().toString();
                String enteredPassword = password.getText().toString();
                String enteredConfirmPassword = confirmPassword.getText().toString();
                String enteredNickname = nickname.getText().toString();

                if (TextUtils.isEmpty(enteredEmail) || TextUtils.isEmpty(enteredPassword) || TextUtils.isEmpty(enteredConfirmPassword) || TextUtils.isEmpty(enteredNickname)) {
                    Toast.makeText(RegisterActivity.this, "Credentials must be filled in.", Toast.LENGTH_SHORT).show();
                } else if (!enteredPassword.equals(enteredConfirmPassword)){
                    Toast.makeText(RegisterActivity.this, "Passwords don't match.", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(enteredEmail, enteredPassword, enteredNickname);
                }
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }

    private void registerUser(String email, String password, String nickname) {

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    addUserToDatabase( email,  auth.getUid(),  nickname);
                    Toast.makeText(RegisterActivity.this, "Succesfully registered new account.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                } else {
                    Toast.makeText(RegisterActivity.this, "Couldn't create an account.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addUserToDatabase(String email, String uid, String nickname) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name",nickname);
        userData.put("uid",uid);
        userData.put("email",email);
        db.collection("Users").document().set(userData);
    }
}