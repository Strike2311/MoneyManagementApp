package com.kacpkuch.moneymanagementapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import android.content.Intent;
import android.widget.Toast;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class AddGroupActivity extends AppCompatActivity {
    private Button backButton;
    private Button createGroupButton;
    private Button joinGroupButton;
    private EditText groupNameText;
    private EditText groupCodeText;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        backButton = findViewById(R.id.addGroupBackButton);
        createGroupButton = findViewById(R.id.createGroupButton);
        joinGroupButton = findViewById(R.id.joinGroupButton);
        groupNameText = findViewById(R.id.groupNameTextView);
        groupCodeText = findViewById(R.id.groupCodeTextView);
        auth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_add_group);
        super.onCreate(savedInstanceState);

        /*
        backButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddGroupActivity.this, MainActivity.class));
            }
        });
        */

        createGroupButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                String enteredGroupName = groupNameText.getText().toString();

                if (TextUtils.isEmpty(enteredGroupName)) {
                    Toast.makeText(AddGroupActivity.this, "Group name must be entered.", Toast.LENGTH_SHORT).show();
                } else {
                    createNewGroup(enteredGroupName, auth.getUid());
                    Toast.makeText(AddGroupActivity.this, "Group created succesfully.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AddGroupActivity.this, MainActivity.class));
                }
            }
        });

        joinGroupButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddGroupActivity.this, MainActivity.class));
            }
        });
    }

    private void createNewGroup(String groupName, String uid) {
        String inviteCode = generateInviteCode();
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("description", groupName);
        groupData.put("inviteCode", inviteCode);
        groupData.put("email", Arrays.asList(uid));
        db.collection("Groups").document().set(groupData);

    }
    private String generateInviteCode() {
        byte[] array = new byte[7]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.forName("UTF-8"));
        return generatedString;
    }

}