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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ChangeBalanceActivity extends AppCompatActivity {
    private Button backButton;
    private Button submitButton;
    private EditText enteredBalance;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_balance);
        backButton = findViewById(R.id.balanceChangeBackButton);
        submitButton = findViewById(R.id.balanceChangeSubmitButton);
        enteredBalance = findViewById(R.id.balanceChangeTextView);
        Bundle extras = getIntent().getExtras();
        String passedGroupId = extras.getString("passedGroupIdToChangeBalance");
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ChangeBalanceActivity.this, GroupActivity.class);
                i.putExtra("passedGroupId", passedGroupId);
                startActivity(i);
                finish();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredBalanceText = enteredBalance.getText().toString();

                if (TextUtils.isEmpty(enteredBalanceText)) {
                    Toast.makeText(ChangeBalanceActivity.this, "Balance must be entered.", Toast.LENGTH_SHORT).show();
                } else {
                    changeBalance(enteredBalanceText, passedGroupId);
                    Toast.makeText(ChangeBalanceActivity.this, "Balance has been changed succesfully.", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(ChangeBalanceActivity.this, GroupActivity.class);
                    i.putExtra("passedGroupId", passedGroupId);
                    startActivity(i);
                    finish();

                }

            }


        });
    }
    private void changeBalance(String enteredBalanceText, String groupId) {
        Map<String, Object> userData = new HashMap<>();
        String[] balanceArray = enteredBalanceText.split("\\.");
        int finalValue = 0;
        if (balanceArray.length == 2) {
            if(balanceArray[1].length() == 2) {
                finalValue += Integer.parseInt(balanceArray[0]) * 100 + Integer.parseInt(balanceArray[1]);
            } else {
                finalValue += Integer.parseInt(balanceArray[0]) * 100 + Integer.parseInt(balanceArray[1]) * 10;
            }
        } else {
            finalValue += Integer.parseInt(balanceArray[0]) * 100;
        }
        userData.put("userBalance",finalValue) ;
        db.collection("Groups").document(groupId).collection("groupUsers").document(auth.getUid()).update(userData);
    }
}