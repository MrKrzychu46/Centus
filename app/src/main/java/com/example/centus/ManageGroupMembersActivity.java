package com.example.centus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageGroupMembersActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private String groupId;
    private String groupName;
    private String currentUserUid;
    private ListView userListView;
    private Button addPhoneButton;
    private EditText phoneInput;
    private ArrayAdapter<String> adapter;
    private Map<String, String> phoneToUidMap = new HashMap<>();
    private Map<String, String> uidToNameMap = new HashMap<>();
    private List<String> currentMembers = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_members);

        db = FirebaseFirestore.getInstance();
        groupId = getIntent().getStringExtra("groupId");
        groupName = getIntent().getStringExtra("groupName");
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        TextView title = findViewById(R.id.manageMembersTitle);
        userListView = findViewById(R.id.userListView);
        phoneInput = findViewById(R.id.phoneInput);
        addPhoneButton = findViewById(R.id.addPhoneButton);
        Button saveMembersButton = findViewById(R.id.saveMembersButton); // dodane

        findViewById(R.id.addingDebtsButton).setOnClickListener(v -> startActivity(new Intent(this, AddDebtActivity.class)));
        findViewById(R.id.appLogo).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.notificationButton).setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        findViewById(R.id.profileButton).setOnClickListener(v -> startActivity(new Intent(this, MyProfileActivity.class)));
        findViewById(R.id.groupsButton).setOnClickListener(v -> startActivity(new Intent(this, MyGroupsActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, OptionsActivity.class)));

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        userListView.setAdapter(adapter);

        addPhoneButton.setOnClickListener(v -> addUserByPhone());

        saveMembersButton.setOnClickListener(v -> {
            saveUpdatedMembers();
            finish(); // wraca do poprzedniego widoku
        });

        userListView.setOnItemLongClickListener((parent, view, position, id) -> {
            String name = adapter.getItem(position);
            String uid = uidToNameMap.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(name))
                    .map(Map.Entry::getKey).findFirst().orElse(null);

            if (uid != null && uid.equals(currentUserUid)) {
                Toast.makeText(this, "Nie możesz usunąć siebie z grupy", Toast.LENGTH_SHORT).show();
                return true;
            }

            new AlertDialog.Builder(this)
                    .setTitle("Usuń członka")
                    .setMessage("Czy na pewno chcesz usunąć tego członka z grupy?")
                    .setPositiveButton("Usuń", (dialog, which) -> {
                        adapter.remove(name);
                        currentMembers.remove(uid);
                        deleteUserGroupDebtsAndRecalculate(uid);
                    })
                    .setNegativeButton("Anuluj", null)
                    .show();
            return true;
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadGroupMembers();
        loadAllUsers();
    }

    private void loadGroupMembers() {
        db.collection("groups").document(groupId).get().addOnSuccessListener(doc -> {
            List<String> members = (List<String>) doc.get("members");
            if (members != null) {
                currentMembers.clear();
                currentMembers.addAll(members);
                updateDisplayedMembers();
            }
        });
    }

    private void loadAllUsers() {
        db.collection("users").get().addOnSuccessListener(query -> {
            phoneToUidMap.clear();
            uidToNameMap.clear();
            for (QueryDocumentSnapshot doc : query) {
                String uid = doc.getId();
                String name = doc.getString("name");
                String surname = doc.getString("surname");
                String phone = doc.getString("phone");
                if (name != null && surname != null && phone != null) {
                    String fullName = name + " " + surname;
                    phoneToUidMap.put(phone, uid);
                    uidToNameMap.put(uid, fullName);
                }
            }
            updateDisplayedMembers();
        });
    }

    private void updateDisplayedMembers() {
        adapter.clear();
        for (String uid : currentMembers) {
            String name = uidToNameMap.get(uid);
            if (name != null) adapter.add(name);
        }
    }

    private void addUserByPhone() {
        String phone = phoneInput.getText().toString().trim();
        if (phone.isEmpty()) {
            Toast.makeText(this, "Wprowadź numer telefonu", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phoneToUidMap.containsKey(phone)) {
            Toast.makeText(this, "Nie znaleziono użytkownika", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = phoneToUidMap.get(phone);
        if (uid.equals(currentUserUid)) {
            Toast.makeText(this, "Nie możesz dodać siebie", Toast.LENGTH_SHORT).show();
            return;
        }
        if (currentMembers.contains(uid)) {
            Toast.makeText(this, "Użytkownik już należy do grupy", Toast.LENGTH_SHORT).show();
            return;
        }

        currentMembers.add(uid);
        phoneInput.setText("");
        recalculateAllGroupDebts();
    }

    private void deleteUserGroupDebtsAndRecalculate(String uid) {
        db.collection("group_expenses")
                .whereEqualTo("group_id", groupId)
                .get()
                .addOnSuccessListener(expenseSnapshot -> {
                    for (QueryDocumentSnapshot expenseDoc : expenseSnapshot) {
                        String expenseId = expenseDoc.getId();
                        double expenseAmount = expenseDoc.getDouble("amount");

                        db.collection("debts")
                                .whereEqualTo("group_expense_id", expenseId)
                                .get()
                                .addOnSuccessListener(debtSnapshot -> {
                                    List<DocumentSnapshot> remainingDebts = new ArrayList<>();

                                    for (DocumentSnapshot doc : debtSnapshot.getDocuments()) {
                                        String debtorId = doc.getString("debtor_id");
                                        if (debtorId != null && debtorId.equals(uid)) {
                                            db.collection("debts").document(doc.getId()).delete();
                                        } else {
                                            remainingDebts.add(doc);
                                        }
                                    }

                                    int newSize = currentMembers.size();
                                    if (newSize > 1 && !remainingDebts.isEmpty()) {
                                        BigDecimal newShare = BigDecimal.valueOf(expenseAmount)
                                                .divide(BigDecimal.valueOf(newSize), 2, RoundingMode.HALF_UP);

                                        for (DocumentSnapshot d : remainingDebts) {
                                            db.collection("debts")
                                                    .document(d.getId())
                                                    .update("amount", newShare.doubleValue());
                                        }
                                    }
                                });
                    }
                    saveUpdatedMembers();
                });
    }

    private void recalculateAllGroupDebts() {
        db.collection("group_expenses")
                .whereEqualTo("group_id", groupId)
                .get()
                .addOnSuccessListener(expenseSnapshot -> {
                    for (QueryDocumentSnapshot expenseDoc : expenseSnapshot) {
                        String expenseId = expenseDoc.getId();
                        double expenseAmount = expenseDoc.getDouble("amount");
                        String title = expenseDoc.getString("title");

                        int memberCount = currentMembers.size();
                        if (memberCount <= 1) return;

                        BigDecimal share = BigDecimal.valueOf(expenseAmount)
                                .divide(BigDecimal.valueOf(memberCount), 2, RoundingMode.HALF_UP);

                        db.collection("debts")
                                .whereEqualTo("group_expense_id", expenseId)
                                .get()
                                .addOnSuccessListener(debtSnapshot -> {
                                    List<String> existingDebtors = new ArrayList<>();

                                    for (DocumentSnapshot doc : debtSnapshot.getDocuments()) {
                                        String debtorId = doc.getString("debtor_id");
                                        if (debtorId != null) {
                                            existingDebtors.add(debtorId);
                                            db.collection("debts")
                                                    .document(doc.getId())
                                                    .update("amount", share.doubleValue());
                                        }
                                    }

                                    for (String memberUid : currentMembers) {
                                        if (!memberUid.equals(currentUserUid) && !existingDebtors.contains(memberUid)) {
                                            Map<String, Object> debt = new HashMap<>();
                                            debt.put("name", title + " (" + groupName + ")");
                                            debt.put("amount", share.doubleValue());
                                            debt.put("additional_info", "Wyd. grupowy");
                                            debt.put("creditor_id", currentUserUid);
                                            debt.put("debtor_id", memberUid);
                                            debt.put("group_expense_id", expenseId);
                                            debt.put("group_id", groupId);
                                            debt.put("created_at", com.google.firebase.Timestamp.now());
                                            db.collection("debts").add(debt);
                                        }
                                    }
                                });
                    }
                    saveUpdatedMembers();
                });
    }

    private void saveUpdatedMembers() {
        db.collection("groups").document(groupId)
                .update("members", currentMembers)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Zaktualizowano członków grupy", Toast.LENGTH_SHORT).show();
                    updateDisplayedMembers();
                })
                .addOnFailureListener(e -> {
                    Log.e("ManageMembers", "Błąd zapisu", e);
                    Toast.makeText(this, "Błąd aktualizacji członków", Toast.LENGTH_SHORT).show();
                });
    }
}
