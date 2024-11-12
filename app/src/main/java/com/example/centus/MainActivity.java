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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ArrayList<AddDebtActivity.Debt> debtList = new ArrayList<>();
    private LinearLayout debtsLayout;
    private TextView totalDebtTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        debtsLayout = findViewById(R.id.debtsLayout);
        totalDebtTextView = findViewById(R.id.totalDebtTextView);

        // Wczytujemy długi z pliku przy starcie aplikacji
        loadDebtsFromFile();
        displayDebts();
        updateTotalDebt();

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
            String name = data.getStringExtra("debtName");
            double amount = data.getDoubleExtra("debtAmount", 0.0);
            String additionalInfo = data.getStringExtra("debtInfo");
            String user = data.getStringExtra("debtUser");

            // Dodajemy nowy dług do listy
            debtList.add(new AddDebtActivity.Debt(name, amount, additionalInfo, user));
            saveDebtsToFile(); // Zapisujemy dane do pliku
            displayDebts();
            updateTotalDebt();
        }
    }

    // Wyświetlanie długów w widoku
    private void displayDebts() {
        debtsLayout.removeAllViews();
        Log.d("MainActivity", "Rozpoczynam wyświetlanie długów. Liczba długów: " + debtList.size());

        if (debtList.isEmpty()) {
            Toast.makeText(MainActivity.this, "Brak długów", Toast.LENGTH_SHORT).show();
            Log.d("MainActivity", "Lista długów jest pusta");
        } else {
            for (int i = 0; i < debtList.size(); i++) {
                AddDebtActivity.Debt debt = debtList.get(i);
                Log.d("MainActivity", "Wyświetlam dług: " + debt.name + " - " + debt.amount);

                Button debtButton = new Button(this);
                debtButton.setText("Dług " + (i + 1) + ": " + debt.name);

                final int index = i;
                debtButton.setOnClickListener(v -> {
                    Intent intent = new Intent(MainActivity.this, DebtDetailActivity.class);
                    intent.putExtra("debtTitle", debtList.get(index).name);
                    intent.putExtra("debtAmount", debtList.get(index).amount + " zł");
                    intent.putExtra("debtDescription", debtList.get(index).additionalInfo);
                    intent.putExtra("debtUser", debtList.get(index).user);
                    startActivity(intent);
                });

                debtsLayout.addView(debtButton);
            }
        }
    }


    // Aktualizacja sumy długów
    private void updateTotalDebt() {
        double totalDebt = calculateTotalDebt();
        totalDebtTextView.setText("Bilans długu: " + totalDebt + " zł");
    }

    // Obliczanie sumy długów
    private double calculateTotalDebt() {
        double total = 0.0;
        for (AddDebtActivity.Debt debt : debtList) {
            total += debt.amount;
        }
        return total;
    }

    // Zapisywanie długów do pliku w formacie CSV
    private void saveDebtsToFile() {
        try (FileOutputStream fos = openFileOutput("debts.txt", MODE_PRIVATE)) {
            StringBuilder data = new StringBuilder();

            for (AddDebtActivity.Debt debt : debtList) {
                data.append(debt.name).append(";")
                        .append(debt.amount).append(";")
                        .append(debt.additionalInfo).append(";")
                        .append(debt.user).append("\n");
            }

            fos.write(data.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd zapisu do pliku", Toast.LENGTH_SHORT).show();
        }
    }

    // Wczytywanie długów z pliku CSV
    private void loadDebtsFromFile() {
        debtList.clear();
        try (FileInputStream fis = openFileInput("debts.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 4) {
                    String name = parts[0];
                    double amount = Double.parseDouble(parts[1]);
                    String additionalInfo = parts[2];
                    String user = parts[3];
                    debtList.add(new AddDebtActivity.Debt(name, amount, additionalInfo, user));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd odczytu z pliku", Toast.LENGTH_SHORT).show();
        }
    }
}
