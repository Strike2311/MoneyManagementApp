package com.kacpkuch.moneymanagementapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
public class CreateGroupActivity extends AppCompatActivity {
    private Boolean isCodeUsed;

    private Button backButton;
    private Button createGroupButton;
    private Button joinGroupButton;
    private EditText groupNameText;
    private EditText groupCodeText;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        backButton = findViewById(R.id.createGroupBackButton);
        createGroupButton = findViewById(R.id.createGroupCreateGroupButton);
        joinGroupButton = findViewById(R.id.createGroupJoinGroupButton);
        groupNameText = findViewById(R.id.createGroupGroupNameTextView);
        groupCodeText = findViewById(R.id.createGroupGroupInviteCodeTextView);
        auth = FirebaseAuth.getInstance();


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CreateGroupActivity.this, MainActivity.class));
            }
        });

        createGroupButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String enteredGroupName = groupNameText.getText().toString();

                if (TextUtils.isEmpty(enteredGroupName)) {
                    Toast.makeText(CreateGroupActivity.this, "Group name must be entered.", Toast.LENGTH_SHORT).show();
                } else {
                    createNewGroup(enteredGroupName, auth.getUid());
                    Toast.makeText(CreateGroupActivity.this, "Group created succesfully.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CreateGroupActivity.this, MainActivity.class));
                }
            }
        });

        joinGroupButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String enteredInviteCode = groupCodeText.getText().toString();

                if (TextUtils.isEmpty(enteredInviteCode)) {
                    Toast.makeText(CreateGroupActivity.this, "Group code must be entered.", Toast.LENGTH_SHORT).show();
                } else {
                    joinGroup(enteredInviteCode, auth.getUid());
                    Toast.makeText(CreateGroupActivity.this, "Group joined succesfully.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(CreateGroupActivity.this, MainActivity.class));
                }

                startActivity(new Intent(CreateGroupActivity.this, MainActivity.class));
            }
        });
    }

    private void joinGroup(String groupCode, String uid) {
        db.collection("Groups").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (document.getData().get("inviteCode").equals(groupCode)) {
                            db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task1) {
                                    if (task1.isSuccessful()) {
                                        for (QueryDocumentSnapshot document1 : task1.getResult()) {
                                            if (document1.getData().get("uid").equals(auth.getUid())) {
                                                Map<String, Object> userData = new HashMap<>();
                                                userData.put("userId", auth.getUid());
                                                userData.put("userBalance", 0);
                                                userData.put("userName", document1.get("name"));
                                                String documentId = document.getId();
                                                DocumentReference documentRef = db.collection("Groups").document(documentId);
                                                documentRef.update("userIdList", FieldValue.arrayUnion(auth.getUid()));
                                                documentRef.collection("groupUsers").document(uid).set(userData);
                                            }
                                        }
                                    } else {
                                        Log.d("TAG", "Error getting documents: ", task.getException());
                                    }
                                }
                            });
                        }
                    }
                } else {
                    Log.d("TAG", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void createNewGroup(String groupName, String uid) {
        String inviteCode = generateInviteCode();
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("inviteCode", inviteCode);
        groupData.put("description", groupName);
        groupData.put("userIdList", Arrays.asList(auth.getUid()));
        String groupId = db.collection("Groups").document().getId();
        db.collection("Groups").document(groupId).set(groupData);

        db.collection("Users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (document.getData().get("uid").equals(auth.getUid())) {
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("userId", auth.getUid());
                            userData.put("userBalance", 0);
                            userData.put("userName", document.getData().get("name"));
                            Log.d("FINAL", "Final inviteCode generated: " + inviteCode);
                            db.collection("Groups").document(groupId).collection("groupUsers").document(uid).set(userData);
                        }
                    }
                } else {
                    Log.d("TAG", "Error getting documents: ", task.getException());
                }
            }
        });


    }

    private String generateInviteCode() {
        String characters = "ABCDEFGHIJKLMNOPRSTUWXYZ1234567890";
        String generatedString = "";
        int digits = 8;
        Random rand = new Random();
        char[] text = new char[digits];

        for (int i = 0; i < digits; i++) {
            text[i] = characters.charAt(rand.nextInt(characters.length()));
        }

        for (int i = 0; i < digits; i++) {
            generatedString += text[i];
        }

        return generatedString;
    }


}


