package com.example.centus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppDatabase db;
    private LinearLayout debtsLayout;
    private TextView totalDebtTextView;
    private View statusIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicjalizacja bazy danych
        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "debt-database").build();

        statusIndicator = findViewById(R.id.statusIndicator);
        debtsLayout = findViewById(R.id.debtsLayout);
        totalDebtTextView = findViewById(R.id.totalDebtTextView);

        // Wczytaj dane przy uruchomieniu
        loadDebtsFromDatabase();

        // Przycisk do nawigacji
        ImageButton addingDebtsButton = findViewById(R.id.addingDebtsButton);
        addingDebtsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddDebtActivity.class);
            startActivityForResult(intent, 1);
        });

        ImageButton notificationsButton = findViewById(R.id.notificationButton);
        notificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(intent);
        });

        Button profileButton = findViewById(R.id.profileButton);
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyProfileActivity.class);
            startActivity(intent);
        });

        Button groupsButton = findViewById(R.id.groupsButton);
        groupsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyGroupsActivity.class);
            startActivity(intent);
        });

        Button settingsButton = findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OptionsActivity.class);
            startActivity(intent);
        });
    }

    // Obsługa powrotu z AddDebtActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            loadDebtsFromDatabase(); // Ponowne załadowanie długów po dodaniu
        }
    }

    // Wczytanie długów z bazy danych Room
    private void loadDebtsFromDatabase() {
        new Thread(() -> {
            List<Debt> debts = db.debtDao().getAllDebts();
            runOnUiThread(() -> {
                displayDebts(debts);
                updateTotalDebt(debts);
            });
        }).start();
    }

    // Wyświetlanie długów w widoku
    private void displayDebts(List<Debt> debts) {
        debtsLayout.removeAllViews();
        Log.d("MainActivity", "Rozpoczynam wyświetlanie długów. Liczba długów: " + debts.size());

        if (debts.isEmpty()) {
            Toast.makeText(MainActivity.this, "Brak długów", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "Lista długów jest pusta");
        } else {
            for (int i = 0; i < debts.size(); i++) {
                Debt debt = debts.get(i);
                Log.d("MainActivity", "Wyświetlam dług: " + debt.name + " - " + debt.amount);

                Button debtButton = new Button(this);
                debtButton.setText("Dług " + (i + 1) + ": " + debt.name);

                final int index = i;
                debtButton.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, DebtDetailActivity.class);
                    intent.putExtra("debtTitle", debts.get(index).name);
                    intent.putExtra("debtAmount", debts.get(index).amount + " zł");
                    intent.putExtra("debtDescription", debts.get(index).additionalInfo);
                    intent.putExtra("debtUser", debts.get(index).user);
                    startActivity(intent);
                });

                debtsLayout.addView(debtButton);
            }
        }
    }

    // Aktualizacja sumy długów
    private void updateTotalDebt(List<Debt> debts) {
        double totalDebt = calculateTotalDebt(debts);
        totalDebtTextView.setText("Bilans długu: " + totalDebt + " zł");

        if (totalDebt > 0) {
            statusIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            statusIndicator.setVisibility(View.VISIBLE); // Ustaw widoczność na widoczną
        } else if (totalDebt < 0) {
            statusIndicator.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            statusIndicator.setVisibility(View.VISIBLE); // Ustaw widoczność na widoczną
        } else {
            statusIndicator.setVisibility(View.INVISIBLE); // Ukryj kwadracik
        }
    }

    // Obliczanie sumy długów
    private double calculateTotalDebt(List<Debt> debts) {
        double total = 0.0;
        for (Debt debt : debts) {
            total += debt.amount;
        }
        return total;
    }

    // Metoda wywoływana po powrocie do MainActivity
    @Override
    protected void onResume() {
        super.onResume();
        loadDebtsFromDatabase(); // Ponowne wczytywanie długów z bazy danych
    }
}
