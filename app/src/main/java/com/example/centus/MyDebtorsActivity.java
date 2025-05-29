package com.example.centus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyDebtorsActivity extends AppCompatActivity {

    private LinearLayout groupsLayout;
    private FirebaseFirestore db;
    private FirebaseHelper firebaseHelper;
    private String currentUid;

    private static final String TAG = "MyGroupsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mydebetors);

        db = FirebaseFirestore.getInstance();
        firebaseHelper = new FirebaseHelper();
        groupsLayout = findViewById(R.id.groupsLayout);
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Nawigacja
        findViewById(R.id.notificationButton).setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));
        findViewById(R.id.appLogo).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.addingDebtsButton).setOnClickListener(v -> startActivity(new Intent(this, AddDebtActivity.class)));
        findViewById(R.id.profileButton).setOnClickListener(v -> startActivity(new Intent(this, MyProfileActivity.class)));
        findViewById(R.id.settingsButton).setOnClickListener(v -> startActivity(new Intent(this, OptionsActivity.class)));

        fetchAndDisplayDebtors();
    }

    private void fetchAndDisplayDebtors() {
        db.collection("debts")
                .whereEqualTo("creditor_id", currentUid)
                .get()
                .addOnSuccessListener(query -> {
                    Map<String, List<Map<String, Object>>> groupedDebts = new HashMap<>();

                    for (QueryDocumentSnapshot doc : query) {
                        String debtorId = doc.getString("debtor_id");
                        if (debtorId == null) continue;

                        groupedDebts.putIfAbsent(debtorId, new ArrayList<>());
                        Map<String, Object> debt = doc.getData();
                        debt.put("debtId", doc.getId());
                        groupedDebts.get(debtorId).add(debt);
                    }

                    if (groupedDebts.isEmpty()) {
                        Toast.makeText(this, "Nie masz jeszcze żadnych dłużników.", Toast.LENGTH_SHORT).show();
                    } else {
                        displayGroups(groupedDebts);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Błąd podczas pobierania długów", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Błąd podczas pobierania danych", e);
                });
    }

    private void displayGroups(Map<String, List<Map<String, Object>>> groupedDebts) {
        groupsLayout.removeAllViews();

        TextView title = new TextView(this);
        title.setText("🧑‍🤝‍🧑 Twoi dłużnicy:");
        title.setTextSize(18);
        groupsLayout.addView(title);

        for (String debtorId : groupedDebts.keySet()) {
            List<Map<String, Object>> debts = groupedDebts.get(debtorId);

            firebaseHelper.fetchUserById(debtorId, new FirebaseHelper.OnUserFetchListener() {
                @Override
                public void onSuccess(Map<String, Object> userData) {
                    String name = (String) userData.get("name");
                    String surname = (String) userData.get("surname");
                    String fullName = (name != null ? name : "") + " " + (surname != null ? surname : "");

                    double total = 0.0;
                    for (Map<String, Object> d : debts) {
                        Object amt = d.get("amount");
                        if (amt instanceof Number) total += ((Number) amt).doubleValue();
                    }

                    Button groupButton = new Button(MyDebtorsActivity.this);
                    groupButton.setText(fullName + " – " + debts.size() + " dług(i) – razem: " + total + " zł");
                    groupButton.setOnClickListener(v -> displayDebtsForGroup(fullName, debts));
                    groupsLayout.addView(groupButton);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "Błąd przy pobieraniu użytkownika", e);
                }
            });
        }
    }


    private void displayDebtsForGroup(String userName, List<Map<String, Object>> debts) {
        groupsLayout.removeAllViews();

        for (Map<String, Object> debt : debts) {
            String name = (String) debt.get("name");
            String debtId = (String) debt.get("debtId");

            Button debtButton = new Button(this);
            debtButton.setText(name != null ? name : "Dług");
            debtButton.setOnClickListener(v -> {
                Intent intent = new Intent(MyDebtorsActivity.this, DebtDetailActivity.class);
                intent.putExtra("debtId", debtId);
                startActivity(intent);
            });
            groupsLayout.addView(debtButton);
        }
    }
}
