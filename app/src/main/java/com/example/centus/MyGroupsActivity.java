package com.example.centus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class MyGroupsActivity extends AppCompatActivity {

    private LinearLayout groupsLayout;
    private Button createGroupButton, addGroupExpenseButton;
    private FirebaseFirestore db;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mygroups);

        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        groupsLayout = findViewById(R.id.groupsLayout);
        createGroupButton = findViewById(R.id.createGroupButton);

        findViewById(R.id.addingDebtsButton).setOnClickListener(v -> startActivity(new Intent(this, AddDebtActivity.class)));
        findViewById(R.id.appLogo).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.notificationButton).setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        findViewById(R.id.profileButton).setOnClickListener(v -> startActivity(new Intent(this, MyProfileActivity.class)));
        findViewById(R.id.groupsButton).setOnClickListener(v -> startActivity(new Intent(this, MyGroupsActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, OptionsActivity.class)));

        createGroupButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateGroupActivity.class);
            startActivity(intent);
        });

        loadUserGroups();
    }

    private void loadUserGroups() {
        db.collection("groups")
                .whereArrayContains("members", currentUid)
                .get()
                .addOnSuccessListener(query -> {
                    groupsLayout.removeAllViews();

                    if (query.isEmpty()) {
                        TextView info = new TextView(this);
                        info.setText("Nie należysz do żadnej grupy.");
                        info.setTextColor(getResources().getColor(android.R.color.white));
                        groupsLayout.addView(info);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : query) {
                        String groupId = doc.getId();
                        String groupName = doc.getString("name");

                        Button groupBtn = new Button(this);
                        groupBtn.setText("Grupa: " + groupName);
                        groupBtn.setOnClickListener(v -> {
                            Intent intent = new Intent(this, GroupDetailsActivity.class);
                            intent.putExtra("groupId", groupId);
                            intent.putExtra("groupName", groupName);
                            startActivity(intent);
                        });
                        groupsLayout.addView(groupBtn);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Błąd wczytywania grup", Toast.LENGTH_SHORT).show();
                    Log.e("MyGroupsActivity", "Błąd Firestore", e);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserGroups();
    }
}
