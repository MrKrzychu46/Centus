package com.example.centus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DebtDetailActivity extends AppCompatActivity {

    // Deklaracja listy długów
    private List<AddDebtActivity.Debt> debtList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debt_detail_activity);
        loadDebtsFromFile();  // Ładowanie długów z pliku

        // Zaktualizowanie widoków z danymi z pliku
        updateDebtTextViews();

        Button editDebtButton = findViewById(R.id.editDebtButton);
        editDebtButton.setOnClickListener(v -> {
            Intent intent = new Intent(DebtDetailActivity.this, EditDebtActivity.class);
            startActivity(intent);
        });

        // Pobranie szczegółów długu z Intenta
        String debtAmount = getIntent().getStringExtra("debtAmount");
        String debtTitle = getIntent().getStringExtra("debtTitle");
        String debtDescription = getIntent().getStringExtra("debtDescription");

        // Ustawienie wartości tekstowych
        TextView debtAmountView = findViewById(R.id.debtAmount);
        TextView debtTitleView = findViewById(R.id.debtTitle);
        TextView debtDescriptionView = findViewById(R.id.debtDescription);

        debtAmountView.setText(debtAmount);
        debtTitleView.setText(debtTitle);
        debtDescriptionView.setText(debtDescription);

        // DODANE 07.11.2024
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

    // Metoda do ładowania długów z pliku
    private void loadDebtsFromFile() {
        try {
            FileInputStream fis = openFileInput("debts.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String line;
            AddDebtActivity.Debt currentDebt = null;

            while ((line = reader.readLine()) != null) {
                if (line.contains(" - ")) { // Pierwsza linia zawiera nazwę i kwotę
                    String[] parts = line.split(" - ");
                    String name = parts[0];
                    double amount = Double.parseDouble(parts[1].replace(" zł", ""));
                    String additionalInfo = reader.readLine(); // Druga linia to dodatkowe informacje
                    currentDebt = new AddDebtActivity.Debt(name, amount, additionalInfo);
                    debtList.add(currentDebt);  // Teraz dodajesz dług do debtList

                    // Logowanie, aby sprawdzić, czy dane zostały poprawnie załadowane
                    Log.d("DebtDetailActivity", "Załadowano dług: " + currentDebt.name + " - " + currentDebt.amount);
                }
            }

            reader.close(); // Zamykamy plik
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd odczytu z pliku", Toast.LENGTH_SHORT).show();
        }
    }


    // Metoda do aktualizacji TextView na podstawie wczytanych długów
    @SuppressLint("SetTextI18n")
    private void updateDebtTextViews() {
        // Sprawdź, czy lista długów nie jest pusta
        if (debtList != null && !debtList.isEmpty()) {
            // Pobierz pierwszy dług z listy (możesz zmienić na inną logikę, jeśli chcesz pokazać długi na podstawie wybranego)
            AddDebtActivity.Debt debt = debtList.get(0);

            // Numer długu (np. 1, 2, 3 itd.)
            int debtIndex = debtList.indexOf(debt) + 1;

            // Zaktualizuj odpowiednie TextView
            TextView debtAmountView = findViewById(R.id.debtAmount);
            TextView debtTitleView = findViewById(R.id.debtTitle);
            TextView debtDescriptionView = findViewById(R.id.debtDescription);

            // Ustawienie wartości w TextView
            debtAmountView.setText(debtIndex + ". " + debt.amount + " zł");
            debtTitleView.setText(debtIndex + ". " + debt.name);
            debtDescriptionView.setText(debt.additionalInfo);
        }
    }

}
