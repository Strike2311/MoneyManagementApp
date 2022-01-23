package com.kacpkuch.moneymanagementapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.core.view.Change;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.google.protobuf.MapEntryLite;

import org.w3c.dom.Text;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GroupActivity extends AppCompatActivity {
    private TextView groupIdText;
    private TextView inviteCodeText;
    private TextView descriptionText;


    private ListView groupUsersView;
    private Button backButton;
    private Button changeBalanceButton;
    private Button calculateTransactionsButton;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        backButton = findViewById(R.id.groupBackButton);
        inviteCodeText = findViewById(R.id.inviteCodeTextView);
        changeBalanceButton = findViewById(R.id.changeBalanceButton);
        calculateTransactionsButton = findViewById(R.id.calculateTransactionsButton);
        groupUsersView = findViewById(R.id.groupUsersListView);
        groupIdText = findViewById(R.id.currentGroupId);
        descriptionText = findViewById(R.id.groupDescriptionOnGroupViewText);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("passedGroupId");
            groupIdText.setText(value);
        }
        getUsersInGroup();
        getGroupInviteCodeAndDescription();
        calculateTransactionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateTransactions();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GroupActivity.this, MainActivity.class));
                finish();
            }
        });
        changeBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GroupActivity.this, ChangeBalanceActivity.class);
                i.putExtra("passedGroupIdToChangeBalance", groupIdText.getText());
                startActivity(i);
                finish();
            }
        });
    }

    private void getGroupInviteCodeAndDescription() {
        String groupId = groupIdText.getText().toString();
        db.collection("Groups").document(groupId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                inviteCodeText.setText(task.getResult().get("inviteCode").toString());
                descriptionText.setText(task.getResult().get("description").toString());
            }
        });

    }
    private void getUsersInGroup() {
        String groupId = groupIdText.getText().toString();
        db.collection("Groups").document(groupId).collection("groupUsers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<String> userName = new ArrayList<>();
                    ArrayList<String> userBalance = new ArrayList<>();
                    ArrayList<String> userId = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        userName.add(document.get("userName").toString());
                        userBalance.add(document.get("userBalance").toString());
                        userId.add(document.getId());
                    }
                    Log.d("userName", "userName: " + userName);

                    GroupActivity.GroupListAdapter listAdapter = new GroupActivity.GroupListAdapter(GroupActivity.this, userName, userBalance, userId);

                    groupUsersView.setAdapter(listAdapter);

                } else {
                    Log.d("TAG", "Error getting documents: ", task.getException());
                }
            }
        });
    }
    private void calculateTransactions() {
        db.collection("Groups").document(groupIdText.getText().toString()).collection("groupUsers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    int overallSum = 0;
                    int originalOverallSum = 0;
                    int groupMembers = 0;
                    ArrayList<User> usersMap = new ArrayList<User>();
                    ArrayList<User> usersMapWithDifference = new ArrayList<User>();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        int currentUserBalance = Integer.parseInt(document.get("userBalance").toString());
                        overallSum += currentUserBalance;
                        groupMembers++;
                        usersMap.add(new User(document.get("userId").toString(), currentUserBalance));
                    }


                    Log.d("DIFFRENCE", "overallSum: " + overallSum);
                    originalOverallSum = overallSum;
                    while(overallSum % groupMembers != 0) {
                        overallSum += 1;
                        Log.d("DIFFRENCE", "overallSum: " + overallSum);
                    }
                    int difference = overallSum/groupMembers;

                    Log.d("DIFFRENCE", "DIFFRENCE: " + difference);
                    for(QueryDocumentSnapshot document : task.getResult()) {
                        int currentUserBalance = 0;
                        for(User u : usersMap) {
                            if((document.get("userId").toString()).equals(u.getId())) {
                                currentUserBalance = u.getBalance();
                            }
                        }
                        usersMapWithDifference.add(new User(document.get("userId").toString(), currentUserBalance - difference));
                    }
                    Log.d("arraylist", "before: " + usersMapWithDifference);

                    Collections.sort(usersMapWithDifference, new Comparator<User>() {
                        @Override
                        public int compare(User o1, User o2) {
                            return o1.compareTo(o2);
                        }
                    });
                    Log.d("arraylist", "after: " + usersMapWithDifference);

                    if (originalOverallSum < overallSum) {
                        Log.d("BEGGINING", "currentDifference and overallSum: " + originalOverallSum + " " + overallSum);
                        ArrayList<String> overpayKeys = new ArrayList<>();
                        ArrayList<String> debtKeys = new ArrayList<>();
                        int overpaySum = 0;
                        int debtSum = 0;
                        for(User currentEntry : usersMapWithDifference) {

                            int currentValue = currentEntry.getBalance();
                            if (currentValue > 0) {
                                overpaySum += currentValue;
                                overpayKeys.add(currentEntry.getId());
                            } else if (currentValue < 0) {
                                debtSum -= currentValue;
                                debtKeys.add(currentEntry.getId());
                            }
                        }
                        Log.d("After while", "debtSum: " + debtSum);
                        Log.d("After while", "overpaySum: " + overpaySum);


                        int fixValue = overallSum - originalOverallSum;
                        Log.d("After while", "originalOverallSum: " + originalOverallSum);
                        Log.d("After while", "overallSum: " + overallSum);

                        Log.d("After while", "fixValue: " + fixValue);

                        if (overpaySum > debtSum) {
                            Random r = new Random();
                            String selectedUser = debtKeys.get(r.nextInt(debtKeys.size()));
                            for(User u : usersMapWithDifference) {
                                if (u.getId().equals(selectedUser)) {
                                    int newBalance = u.getBalance() - fixValue;
                                    u.setBalance(newBalance);
                                }
                            }
                        } else if (overpaySum < debtSum){
                            Random r = new Random();
                            String selectedUser = overpayKeys.get(r.nextInt(overpayKeys.size()));
                            for(User u : usersMapWithDifference) {
                                if (u.getId().equals(selectedUser)) {
                                    int newBalance = u.getBalance() + fixValue;
                                    u.setBalance(newBalance);
                                }
                            }
                        }
                    }

                    HashMap<String,Integer> transactionsMap = getTransactionsMap(usersMapWithDifference);
                    Log.d("transactions", "transactionsMap: " + transactionsMap);

                    Iterator iter = transactionsMap.entrySet().iterator();
                    HashMap<String, Integer> currentUsersTransactions = new HashMap<>();
                    while (iter.hasNext()) {
                        Map.Entry currentEntry = (Map.Entry) iter.next();
                        String[] users = (currentEntry.getKey().toString()).split("-");
                        Log.d("transactions", "users: " + users.toString());
                        if ((users[0]).equals(auth.getUid())) {
                            currentUsersTransactions.put(users[1].toString(), Integer.parseInt(currentEntry.getValue().toString()));
                        }
                    }
                    Log.d("transactions", "Current users transactions: " + transactionsMap);
                    Intent i = new Intent(GroupActivity.this, CalculateTransactionsActivity.class);
                    i.putExtra("passedArray",currentUsersTransactions);
                    i.putExtra("passedGroupId", groupIdText.getText());
                    startActivity(i);
                    finish();

                }
            }
        });
    }

    private HashMap<String, Integer> getTransactionsMap(ArrayList<User> usersMapWithDifference) {
        Boolean finishFlag = false;
        HashMap<String, Integer> transactionsMap = new HashMap<>();
        while (!finishFlag) {

            int clearBalanceCounter = 0;
            Boolean overpayFound = false;
            Boolean debtFound = false;
            String overpayId = "";
            String debtId = "";
            Integer overpayBalance = 0;
            Integer debtBalance = 0;
            for(User currentEntry : usersMapWithDifference) {
                Integer balanceValue = currentEntry.getBalance();
                if(balanceValue < 0 && !debtFound) {
                    debtFound = true;
                    debtId = currentEntry.getId();
                    debtBalance = currentEntry.getBalance();
                    Log.d("Calculating", "Found debt: " + debtBalance);

                } else if (balanceValue > 0 && !overpayFound) {
                    overpayFound = true;
                    overpayId = currentEntry.getId();
                    overpayBalance = currentEntry.getBalance();
                    Log.d("Calculating", "Found overpay: " + overpayBalance);

                }
                else if(balanceValue == 0){
                    clearBalanceCounter++;
                }
            }

            if (clearBalanceCounter == usersMapWithDifference.size()) {
                finishFlag = true;
                Log.d("Calculating", "FINISHED CALCULATING: " + usersMapWithDifference);
                Log.d("Calculating", "FINISHED CALCULATING: " + usersMapWithDifference);


            } else if (debtFound && overpayFound) {
                Log.d("Calculating", "usersMapWithDifference: " + usersMapWithDifference);
                Log.d("Calculating", "transactionsMap: " + transactionsMap);


                if (Math.abs(debtBalance) - overpayBalance > 0) {
                    for(User currentEntry : usersMapWithDifference) {
                        if (currentEntry.getId().equals(overpayId)) {
                            currentEntry.setBalance(0);
                        } else if(currentEntry.getId().equals(debtId)) {
                            currentEntry.setBalance(debtBalance + overpayBalance);
                        }
                    }
                    transactionsMap.put(debtId + "-" + overpayId, overpayBalance);
                } else if (Math.abs(debtBalance) - overpayBalance < 0) {
                    for(User currentEntry : usersMapWithDifference) {
                        if (currentEntry.getId().equals(overpayId)) {
                            currentEntry.setBalance(overpayBalance + debtBalance);
                        } else if(currentEntry.getId().equals(debtId)) {
                            currentEntry.setBalance(0);
                        }
                    }
                    transactionsMap.put(debtId + "-" + overpayId, overpayBalance);

                } else {
                    for(User currentEntry : usersMapWithDifference) {
                        if (currentEntry.getId().equals(overpayId)) {
                            currentEntry.setBalance(0);
                        } else if(currentEntry.getId().equals(debtId)) {
                            currentEntry.setBalance(0);
                        }
                    }
                    transactionsMap.put(debtId + "-" + overpayId, overpayBalance);
                }
            }
        }
        return transactionsMap;
    }
    class GroupListAdapter extends ArrayAdapter<String> {

        Context context;
        ArrayList<String> userName = new ArrayList<>();
        ArrayList<String> userId = new ArrayList<>();
        ArrayList<String> userBalance = new ArrayList<>();

        GroupListAdapter (Context c, ArrayList userName, ArrayList userBalance, ArrayList userId) {
            super(c, R.layout.list_view_row, R.id.currentGroupId, userName);
            this.context = c;
            this.userName = userName;
            this.userId = userId;
            this.userBalance = userBalance;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.attrib_row, parent, false);

            TextView myUserName = row.findViewById(R.id.groupUserName);
            TextView myUserId = row.findViewById(R.id.groupUserId);
            TextView myUserBalance = row.findViewById(R.id.groupUserBalance);

            Log.d("DEBUG", "userName: "+ userName);
            Log.d("DEBUG", "userId: "+ userId.toString());
            Log.d("DEBUG", "userBalance: "+ userBalance);
            if (userId.get(position).equals(auth.getUid())) {
                myUserName.setTypeface(myUserName.getTypeface(), Typeface.BOLD);
            }
            String balance;
            if(userBalance.get(position).length() > 2) {
                balance = userBalance.get(position);
                String lastTwo = balance.substring(balance.length() - 2);
                String firstPart = balance.substring(0, balance.length() - 2);
                balance = firstPart + "." + lastTwo;
            }
            else {
                balance = userBalance.get(position);
            }
            myUserName.setText(userName.get(position));
            myUserId.setText(userId.get(position));
            myUserBalance.setText(balance);

            return row;
        }
    }

}