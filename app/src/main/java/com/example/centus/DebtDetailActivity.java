package com.example.centus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class DebtDetailActivity extends AppCompatActivity {

    // Deklaracja listy długów
    private List<AddDebtActivity.Debt> debtList = new ArrayList<>();
    private String currentDebtName; // Zmienna do przechowywania nazwy bieżącego długu

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debt_detail_activity);

        loadDebtsFromFile(); // Ładowanie długów z pliku

        // Pobranie szczegółów długu z Intenta
        String debtAmount = getIntent().getStringExtra("debtAmount");
        String debtTitle = getIntent().getStringExtra("debtTitle");
        String debtDescription = getIntent().getStringExtra("debtDescription");
        String debtUser = getIntent().getStringExtra("debtUser"); // Odbieramy użytkownika

        currentDebtName = debtTitle; // Zapisujemy nazwę dług

        // Ustawienie wartości tekstowych
        TextView debtAmountView = findViewById(R.id.debtAmount);
        TextView debtTitleView = findViewById(R.id.debtTitle);
        TextView debtDescriptionView = findViewById(R.id.debtDescription);
        TextView debtUserView = findViewById(R.id.debtUser); // Dodajemy pole użytkownika

        debtAmountView.setText(debtAmount);
        debtTitleView.setText(debtTitle);
        debtDescriptionView.setText(debtDescription);
        debtUserView.setText(debtUser); // Wyświetlamy użytkownika

        // Obsługa przycisku edycji długu
        Button editDebtButton = findViewById(R.id.editDebtButton);
        editDebtButton.setOnClickListener(v -> {
            Intent intent = new Intent(DebtDetailActivity.this, EditDebtActivity.class);
            startActivity(intent);
        });

        // Obsługa przycisku usuwania długu
        Button deleteDebtButton = findViewById(R.id.deleteDebtButton);
        deleteDebtButton.setOnClickListener(v -> {
            removeDebt(); // Usuwamy dług
            Toast.makeText(DebtDetailActivity.this, "Dług został usunięty", Toast.LENGTH_SHORT).show();
            finish(); // Zakończ aktywność
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

    // Metoda do usuwania długu z listy i zapisania zmian w pliku
    private void removeDebt() {
        for (int i = 0; i < debtList.size(); i++) {
            if (debtList.get(i).name.equals(currentDebtName)) {
                debtList.remove(i); // Usuwamy dług z listy
                saveDebtsToFile(); // Zapisujemy po usunięciu
                break;
            }
        }
    }

    // Zapis długów do pliku
    private void saveDebtsToFile() {
        try (FileOutputStream fos = openFileOutput("debts.txt", MODE_PRIVATE);
             OutputStreamWriter writer = new OutputStreamWriter(fos)) {

            for (AddDebtActivity.Debt debt : debtList) {
                writer.write(debt.name + ";" + debt.amount + ";" + debt.additionalInfo + ";" + debt.user + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd zapisu do pliku", Toast.LENGTH_SHORT).show();
        }
    }

    // Metoda do ładowania długów z pliku (nowy format CSV)
    private void loadDebtsFromFile() {
        debtList.clear(); // Czyścimy listę przed załadowaniem danych
        try (FileInputStream fis = openFileInput("debts.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // Rozdzielamy dane na podstawie średnika
                String[] parts = line.split(";");
                if (parts.length == 4) {
                    String name = parts[0];
                    double amount = Double.parseDouble(parts[1]);
                    String additionalInfo = parts[2];
                    String user = parts[3];

                    // Tworzymy nowy obiekt długu i dodajemy go do listy
                    AddDebtActivity.Debt debt = new AddDebtActivity.Debt(name, amount, additionalInfo, user);
                    debtList.add(debt);

                    // Logowanie dla debugowania
                    Log.d("DebtDetailActivity", "Załadowano dług: " + debt.name + " - " + debt.amount);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd odczytu z pliku", Toast.LENGTH_SHORT).show();
        }
    }
}