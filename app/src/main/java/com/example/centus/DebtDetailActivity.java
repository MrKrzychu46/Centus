package com.example.centus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class DebtDetailActivity extends AppCompatActivity {

    private AppDatabase db; // Obiekt bazy danych
    private Debt currentDebt;

    // Widoki
    private TextView debtTitleView, debtAmountView, debtDescriptionView, debtUserView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debt_detail_activity);

        // Inicjalizacja bazy danych
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "debt-database").build();

        // Inicjalizacja widoków
        debtTitleView = findViewById(R.id.debtTitle);
        debtAmountView = findViewById(R.id.debtAmount);
        debtDescriptionView = findViewById(R.id.debtDescription);
        debtUserView = findViewById(R.id.debtUser);

        // Pobranie ID długu z Intenta
        int debtId = getIntent().getIntExtra("debtId", -1);
        if (debtId == -1) {
            Toast.makeText(this, "Nie znaleziono szczegółów długu", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Pobranie danych długu z bazy
        new Thread(() -> {
            currentDebt = db.debtDao().getDebtById(debtId);
            if (currentDebt != null) {
                runOnUiThread(() -> {
                    // Wyświetlenie danych długu
                    debtTitleView.setText(currentDebt.name);
                    debtAmountView.setText(String.format("%.2f zł", currentDebt.amount));
                    debtDescriptionView.setText(currentDebt.additionalInfo.isEmpty() ? "Brak dodatkowych informacji" : currentDebt.additionalInfo);
                    debtUserView.setText(currentDebt.user);
                });
            } else {
                runOnUiThread(() -> {
                    Toast.makeText(DebtDetailActivity.this, "Nie znaleziono długu w bazie danych", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();

        // Obsługa przycisku edycji długu
        Button editDebtButton = findViewById(R.id.editDebtButton);
        editDebtButton.setOnClickListener(v -> {
            if (currentDebt == null) {
                Toast.makeText(this, "Brak danych do edycji", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(DebtDetailActivity.this, EditDebtActivity.class);
            intent.putExtra("debtId", currentDebt.id);
            startActivity(intent);
        });

        // Obsługa przycisku usuwania długu
        Button deleteDebtButton = findViewById(R.id.deleteDebtButton);
        deleteDebtButton.setOnClickListener(v -> {
            if (currentDebt == null) {
                Toast.makeText(this, "Brak danych do usunięcia", Toast.LENGTH_SHORT).show();
                return;
            }

            new Thread(() -> {
                db.debtDao().delete(currentDebt);
                runOnUiThread(() -> {
                    Toast.makeText(DebtDetailActivity.this, "Dług został usunięty", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }).start();
        });

        // Obsługa przycisków nawigacyjnych
        ImageButton notificationsButton = findViewById(R.id.notificationButton);
        notificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(DebtDetailActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        ImageButton mainButton = findViewById(R.id.appLogo);
        mainButton.setOnClickListener(v -> {
            Intent intent = new Intent(DebtDetailActivity.this, MainActivity.class);
            startActivity(intent);
        });

        ImageButton addingDebtsButton = findViewById(R.id.addingDebtsButton);
        addingDebtsButton.setOnClickListener(v -> {
            Intent intent = new Intent(DebtDetailActivity.this, AddDebtActivity.class);
            startActivity(intent);
        });
    }
}
