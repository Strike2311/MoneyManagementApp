package com.kacpkuch.moneymanagementapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class CalculateTransactionsActivity extends AppCompatActivity {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Button backButton;
    private TextView transactionsTextView;
    private ListView transactionsList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculate_transactions);
        transactionsList = findViewById(R.id.transactionsListView);
        backButton = findViewById(R.id.transactionsBackButton);
        transactionsTextView = findViewById(R.id.transactionsTextView);

        Bundle extras = getIntent().getExtras();
        HashMap<String,Double> transactionsMap = (HashMap<String, Double>) extras.getSerializable("passedArray");
        String groupId = extras.getString("passedGroupId");
        getDebtsToUsers(groupId, transactionsMap);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(CalculateTransactionsActivity.this, GroupActivity.class);
                i.putExtra("passedGroupId", groupId);
                startActivity(i);
                finish();
            }
        });

    }

    private void getDebtsToUsers(String groupId, HashMap transactionsMap) {
        db.collection("Groups").document(groupId).collection("groupUsers").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<String> userName = new ArrayList<>();
                    ArrayList<String> userBalance = new ArrayList<>();
                    ArrayList<String> userId = new ArrayList<>();
                    int entriesCounter = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        if (transactionsMap.containsKey(document.getId())) {

                            String balance = transactionsMap.get(document.getId()).toString();
                            String lastTwo = balance.substring(balance.length() - 2);
                            String firstPart = balance.substring(0, balance.length() - 2);
                            balance = firstPart + "." + lastTwo;
                            userName.add(document.get("userName").toString());
                            userBalance.add(balance);
                            userId.add(document.getId());
                            entriesCounter++;
                        }
                    }
                    if (entriesCounter == 0) {
                        transactionsTextView.setText("You don't have any transactions to make");
                    }
                    Log.d("userName", "userName: " + userName);

                    CalculateTransactionsActivity.DebtListAdapter listAdapter = new CalculateTransactionsActivity.DebtListAdapter(CalculateTransactionsActivity.this, userName, userBalance, userId);

                    transactionsList.setAdapter(listAdapter);

                } else {
                    Log.d("TAG", "Error getting documents: ", task.getException());
                }
            }
        });
    }
    class DebtListAdapter extends ArrayAdapter<String> {

        Context context;
        ArrayList<String> userName = new ArrayList<>();
        ArrayList<String> userId = new ArrayList<>();
        ArrayList<String> userBalance = new ArrayList<>();

        DebtListAdapter (Context c, ArrayList userName, ArrayList userBalance, ArrayList userId) {
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
            myUserName.setText(userName.get(position));
            myUserId.setText(userId.get(position));
            myUserBalance.setText(userBalance.get(position));

            return row;
        }
    }
}