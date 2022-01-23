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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private FloatingActionButton createGroupButton;
    private Button signOutButton;
    private ListView groupsList;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        createGroupButton = findViewById(R.id.newGroupButton);
        groupsList = findViewById(R.id.groupsList);
        signOutButton = findViewById(R.id.signOutButton);
        auth = FirebaseAuth.getInstance();
        getGroupsForUser();

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        });

        createGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CreateGroupActivity.class));
                finish();
            }
        });

    }

    private void getGroupsForUser() {
        db.collection("Groups").whereArrayContains("userIdList", auth.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<String> description = new ArrayList<>();
                    ArrayList<String> id = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        description.add(document.get("description").toString());
                        id.add(document.getId());
                    }

                    ListAdapter listAdapter = new ListAdapter(MainActivity.this, description, id);

                    groupsList.setAdapter(listAdapter);

                    groupsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent i = new Intent(MainActivity.this, GroupActivity.class);
                            i.putExtra("passedGroupId",(String) parent.getItemAtPosition(position));
                            startActivity(i);
                            finish();
                        }
                    });
                } else {
                }


            }
        });
    }

    class ListAdapter extends ArrayAdapter<String> {

        Context context;
        ArrayList<String> description = new ArrayList<>();
        ArrayList<String> id = new ArrayList<>();

        ListAdapter (Context c, ArrayList description, ArrayList id) {
            super(c, R.layout.list_view_row, R.id.currentGroupId, id);
            this.context = c;
            this.description = description;
            this.id = id;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View row = layoutInflater.inflate(R.layout.list_view_row, parent, false);

            TextView myDescription = row.findViewById(R.id.groupDescription);
            TextView myId = row.findViewById(R.id.currentGroupId);

            myDescription.setText(description.get(position));
            myId.setText(id.get(position));

            return row;
        }
    }
}