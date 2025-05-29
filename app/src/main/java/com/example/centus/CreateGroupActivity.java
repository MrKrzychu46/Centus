package com.example.centus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateGroupActivity extends AppCompatActivity {

    private EditText groupNameEditText;
    private EditText phoneInput;
    private Button addPhoneButton;
    private ListView phoneListView;
    private Button createGroupButton;

    private ArrayAdapter<String> adapter;
    private List<String> selectedUserUids = new ArrayList<>();
    private Map<String, String> phoneToUidMap = new HashMap<>();
    private Map<String, String> phoneToNameMap = new HashMap<>();
    private List<String> displayNames = new ArrayList<>();



    private FirebaseFirestore db;
    private String currentUserPhone;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        db = FirebaseFirestore.getInstance();

        groupNameEditText = findViewById(R.id.groupNameEditText);
        phoneInput = findViewById(R.id.phoneInput);
        addPhoneButton = findViewById(R.id.addPhoneButton);
        phoneListView = findViewById(R.id.phoneListView);
        createGroupButton = findViewById(R.id.createGroupButton);

        findViewById(R.id.addingDebtsButton).setOnClickListener(v -> startActivity(new Intent(this, AddDebtActivity.class)));
        findViewById(R.id.appLogo).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.notificationButton).setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        findViewById(R.id.profileButton).setOnClickListener(v -> startActivity(new Intent(this, MyProfileActivity.class)));
        findViewById(R.id.groupsButton).setOnClickListener(v -> startActivity(new Intent(this, MyGroupsActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, OptionsActivity.class)));

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, displayNames);
        phoneListView.setAdapter(adapter);

        loadPhoneToUidMap();

        addPhoneButton.setOnClickListener(v -> {
            String phone = phoneInput.getText().toString().trim();
            if (phone.isEmpty()) {
                Toast.makeText(this, "Wprowadź numer telefonu", Toast.LENGTH_SHORT).show();
                return;
            }
            if (phone.equals(currentUserPhone)) {
                Toast.makeText(this, "Nie możesz dodać siebie", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!phoneToUidMap.containsKey(phone)) {
                Toast.makeText(this, "Nie znaleziono użytkownika z tym numerem", Toast.LENGTH_SHORT).show();
                return;
            }
            String uid = phoneToUidMap.get(phone);
            String displayName = phoneToNameMap.get(phone);

            if (!selectedUserUids.contains(uid)) {
                selectedUserUids.add(uid);
                displayNames.add(displayName);
                adapter.notifyDataSetChanged();
                phoneInput.setText("");
            } else {
                Toast.makeText(this, "Ten użytkownik już został dodany", Toast.LENGTH_SHORT).show();
            }
        });

        phoneListView.setOnItemLongClickListener((parent, view, position, id) -> {
            selectedUserUids.remove(position);
            displayNames.remove(position);
            adapter.notifyDataSetChanged();
            return true;
        });

        createGroupButton.setOnClickListener(v -> createGroup());
    }

    private void loadPhoneToUidMap() {
        String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("users").get().addOnSuccessListener(query -> {
            for (QueryDocumentSnapshot doc : query) {
                String uid = doc.getId();
                String phone = doc.getString("phone");
                String name = doc.getString("name");
                String surname = doc.getString("surname");
                if (phone != null && name != null && surname != null) {
                    phoneToUidMap.put(phone, uid);
                    phoneToNameMap.put(phone, name + " " + surname);
                    if (uid.equals(currentUid)) {
                        currentUserPhone = phone;
                    }
                }
            }
        });
    }

    private void createGroup() {
        String groupName = groupNameEditText.getText().toString().trim();
        if (groupName.isEmpty()) {
            Toast.makeText(this, "Podaj nazwę grupy", Toast.LENGTH_SHORT).show();
            return;
        }

        String ownerUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (!selectedUserUids.contains(ownerUid)) {
            selectedUserUids.add(ownerUid);
        }

        Map<String, Object> group = new HashMap<>();
        group.put("name", groupName);
        group.put("owner_id", ownerUid);
        group.put("members", selectedUserUids);
        group.put("created_at", Timestamp.now());

        db.collection("groups")
                .add(group)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Grupa utworzona", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("CreateGroup", "Błąd tworzenia grupy", e);
                    Toast.makeText(this, "Błąd tworzenia grupy", Toast.LENGTH_SHORT).show();
                });
    }
}
