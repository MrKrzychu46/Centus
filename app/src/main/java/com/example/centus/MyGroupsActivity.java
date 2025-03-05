package com.example.centus;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MyGroupsActivity extends AppCompatActivity {

    private LinearLayout groupsLayout; // Layout, do którego dodajemy przyciski
    private Map<String, List<Map<String, Object>>> groupedDebts;
    private FirebaseHelper firebaseHelper;
    private static final String TAG = "MyGroupsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mygroups);

        firebaseHelper = new FirebaseHelper();
        groupsLayout = findViewById(R.id.groupsLayout);

        fetchAndDisplayGroupedDebts();
    }

    private void fetchAndDisplayGroupedDebts() {
        firebaseHelper.fetchGroupedDebts(new FirebaseHelper.OnDebtsGroupedListener() {
            @Override
            public void onSuccess(Map<String, List<Map<String, Object>>> groupedDebtsResult) {
                groupedDebts = groupedDebtsResult; // Przechowujemy dane grupowanych długów
                Log.d(TAG, "Pobrano grupy długów: " + groupedDebts.size());
                displayGroups(); // Wyświetlamy grupy
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(MyGroupsActivity.this, "Błąd podczas pobierania długów", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Błąd podczas pobierania grup długów", e);
            }
        });
    }


    private void displayGroups() {
        groupsLayout.removeAllViews();
        Log.d(TAG, "Rozpoczynam wyświetlanie grup. Liczba grup: " + groupedDebts.size());

        if (groupedDebts.isEmpty()) {
            Toast.makeText(MyGroupsActivity.this, "Brak grup długów", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Lista grup jest pusta");
        } else {
            for (String userId : groupedDebts.keySet()) {
                List<Map<String, Object>> debts = groupedDebts.get(userId);

                firebaseHelper.fetchUserById(userId, new FirebaseHelper.OnUserFetchListener() {
                    @Override
                    public void onSuccess(Map<String, Object> userData) {
                        String name = userData.get("name") != null ? userData.get("name").toString() : "Nieznany";
                        String surname = userData.get("surname") != null ? userData.get("surname").toString() : "użytkownik";
                        String fullName = name + " " + surname;

                        Log.d(TAG, "Dodawanie grupy dla użytkownika: " + fullName);

                        Button groupButton = new Button(MyGroupsActivity.this);
                        groupButton.setText("Grupa: " + fullName);
                        groupButton.setOnClickListener(v -> displayDebtsForGroup(fullName, debts));
                        groupsLayout.addView(groupButton);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e(TAG, "Nie udało się pobrać danych użytkownika dla ID: " + userId, e);
                    }
                });
            }
        }
    }


    private void displayDebtsForGroup(String userName, List<Map<String, Object>> debts) {
        groupsLayout.removeAllViews();

        if (debts.isEmpty()) {
            Toast.makeText(this, "Brak długów w grupie: " + userName, Toast.LENGTH_SHORT).show();
        } else {
            for (int i = 0; i < debts.size(); i++) {
                Map<String, Object> debt = debts.get(i);

                String debtName = (debt.get("name") != null) ? debt.get("name").toString() : "Nieznany dług";
                String debtId = (debt.get("debtId") != null) ? debt.get("debtId").toString() : null;

                if (Objects.isNull(debtId)) {
                    Log.e(TAG, "Dług bez ID: " + debtName);
                    continue;
                }

                Button debtButton = new Button(this);
                debtButton.setText("Dług " + (i + 1) + ": " + debtName);
                debtButton.setOnClickListener(v -> {
                    Intent intent = new Intent(MyGroupsActivity.this, DebtDetailActivity.class);
                    intent.putExtra("debtId", debtId); // Przekazujemy debtId
                    startActivity(intent);
                });

                groupsLayout.addView(debtButton);
            }

            Button backButton = new Button(this);
            backButton.setText("Powrót do grup");
            backButton.setOnClickListener(v -> displayGroups());
            groupsLayout.addView(backButton);
        }
    }}