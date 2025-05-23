package com.example.centus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

public class DebtDetailActivity extends AppCompatActivity {

    private FirebaseHelper firebaseHelper;
    private String debtId;

    private TextView debtTitleView, debtAmountView, debtDescriptionView;
    private TextView creditorView, debtorView;
    private TextView debtDateView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debt_detail_activity);

        firebaseHelper = new FirebaseHelper();
        debtId = getIntent().getStringExtra("debtId");

        debtTitleView = findViewById(R.id.debtTitle);
        debtAmountView = findViewById(R.id.debtAmount);
        debtDescriptionView = findViewById(R.id.debtDescription);
        creditorView = findViewById(R.id.creditorName);
        debtorView = findViewById(R.id.debtorName);
        debtDateView = findViewById(R.id.debtDate);


        loadDebtDetailsFromFirestore(debtId);

        Button editDebtButton = findViewById(R.id.editDebtButton);
        editDebtButton.setOnClickListener(v -> {
            Intent intent = new Intent(DebtDetailActivity.this, EditDebtActivity.class);
            intent.putExtra("debtId", debtId);
            startActivityForResult(intent, 1);
        });

        Button deleteDebtButton = findViewById(R.id.deleteDebtButton);
        deleteDebtButton.setOnClickListener(v -> deleteDebt());

        ImageButton notificationsButton = findViewById(R.id.notificationButton);
        notificationsButton.setOnClickListener(v -> startActivity(new Intent(this, NotificationActivity.class)));

        ImageButton mainButton = findViewById(R.id.appLogo);
        mainButton.setOnClickListener(v -> {
            setResult(Activity.RESULT_OK);
            finish();
        });

        ImageButton addingDebtsButton = findViewById(R.id.addingDebtsButton);
        addingDebtsButton.setOnClickListener(v -> startActivity(new Intent(this, AddDebtActivity.class)));
    }

    private void loadDebtDetailsFromFirestore(String debtId) {
        firebaseHelper.getDebts().document(debtId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Nie znaleziono szczegółów długu", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String name = documentSnapshot.getString("name");
                    Double amount = documentSnapshot.getDouble("amount");
                    String info = documentSnapshot.getString("additional_info");
                    String creditorId = documentSnapshot.getString("creditor_id");
                    String debtorId = documentSnapshot.getString("debtor_id");
                    com.google.firebase.Timestamp createdAt = documentSnapshot.getTimestamp("created_at");

                    debtTitleView.setText(name != null ? name : "Brak");
                    debtAmountView.setText(amount != null ? amount + " zł" : "0.0 zł");
                    debtDescriptionView.setText(info != null ? info : "-");

                    // ⏰ Wyświetlenie daty wystawienia
                    TextView debtDateView = findViewById(R.id.debtDate);
                    if (createdAt != null) {
                        String formattedDate = android.text.format.DateFormat.format("dd.MM.yyyy HH:mm", createdAt.toDate()).toString();
                        debtDateView.setText("Data wystawienia: " + formattedDate);
                    } else {
                        debtDateView.setText("Data wystawienia: nieznana");
                    }

                    // 🔐 Zabezpieczenie przycisków tylko dla wierzyciela
                    String currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    Button editDebtButton = findViewById(R.id.editDebtButton);
                    Button deleteDebtButton = findViewById(R.id.deleteDebtButton);

                    boolean isCreditor = creditorId != null && creditorId.equals(currentUid);
                    editDebtButton.setEnabled(isCreditor);
                    deleteDebtButton.setEnabled(isCreditor);
                    editDebtButton.setVisibility(isCreditor ? View.VISIBLE : View.GONE);
                    deleteDebtButton.setVisibility(isCreditor ? View.VISIBLE : View.GONE);

                    // Dane wierzyciela
                    if (creditorId != null) {
                        firebaseHelper.fetchUserById(creditorId, new FirebaseHelper.OnUserFetchListener() {
                            @Override
                            public void onSuccess(Map<String, Object> userData) {
                                String n = (String) userData.get("name");
                                String s = (String) userData.get("surname");
                                creditorView.setText("Wierzyciel: " + (n != null ? n : "") + " " + (s != null ? s : ""));
                            }

                            @Override
                            public void onFailure(Exception e) {
                                creditorView.setText("Wierzyciel: nieznany");
                            }
                        });
                    }

                    // Dane dłużnika
                    if (debtorId != null) {
                        firebaseHelper.fetchUserById(debtorId, new FirebaseHelper.OnUserFetchListener() {
                            @Override
                            public void onSuccess(Map<String, Object> userData) {
                                String n = (String) userData.get("name");
                                String s = (String) userData.get("surname");
                                debtorView.setText("Dłużnik: " + (n != null ? n : "") + " " + (s != null ? s : ""));
                            }

                            @Override
                            public void onFailure(Exception e) {
                                debtorView.setText("Dłużnik: nieznany");
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("DebtDetailActivity", "Błąd ładowania długu", e);
                    Toast.makeText(this, "Błąd ładowania szczegółów długu", Toast.LENGTH_SHORT).show();
                });
    }




    private void deleteDebt() {
        firebaseHelper.getDebts().document(debtId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Dług usunięty", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w("DebtDetailActivity", "Błąd usuwania długu", e);
                    Toast.makeText(this, "Błąd podczas usuwania długu", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            loadDebtDetailsFromFirestore(debtId);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDebtDetailsFromFirestore(debtId);
    }
}
