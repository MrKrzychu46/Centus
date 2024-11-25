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

    private List<AddDebtActivity.Debt> debtList = new ArrayList<>();
    private String currentDebtName;

    // Deklaracja widoków jako pola klasy
    private TextView debtTitleView, debtAmountView, debtDescriptionView, debtUserView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debt_detail_activity);

        // Inicjalizacja widoków
        debtTitleView = findViewById(R.id.debtTitle);
        debtAmountView = findViewById(R.id.debtAmount);
        debtDescriptionView = findViewById(R.id.debtDescription);
        debtUserView = findViewById(R.id.debtUser);

        loadDebtsFromFile();

        // Pobranie szczegółów długu z Intenta
        String debtAmount = getIntent().getStringExtra("debtAmount");
        String debtTitle = getIntent().getStringExtra("debtTitle");
        String debtDescription = getIntent().getStringExtra("debtDescription");
        String debtUser = getIntent().getStringExtra("debtUser");

        currentDebtName = debtTitle;

        // Ustawienie wartości tekstowych
        debtAmountView.setText(debtAmount);
        debtTitleView.setText(debtTitle);
        debtDescriptionView.setText(debtDescription);
        debtUserView.setText(debtUser);

        // Obsługa przycisku edycji długu
        Button editDebtButton = findViewById(R.id.editDebtButton);
        editDebtButton.setOnClickListener(v -> {
            Intent intent = new Intent(DebtDetailActivity.this, EditDebtActivity.class);
            intent.putExtra("debtName", debtTitle); // Tytuł długu

            try {
                if (debtAmount == null || debtAmount.isEmpty()) {
                    Toast.makeText(this, "Kwota długu jest pusta", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Usuwamy niepotrzebne znaki, zamieniamy przecinki na kropki
                String sanitizedAmountText = debtAmount.replaceAll("[^0-9.,]", "").replace(",", ".");
                double amount = Double.parseDouble(sanitizedAmountText);

                if (amount <= 0 || amount > 1_000_000) {
                    Toast.makeText(this, "Kwota musi być dodatnia i nie większa niż 1,000,000", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Dodajemy poprawnie sparsowaną kwotę do Intenta
                intent.putExtra("debtAmount", amount);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Nieprawidłowy format kwoty", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }

            intent.putExtra("debtInfo", debtDescription); // Opis długu
            intent.putExtra("debtUser", debtUser);        // Nazwa użytkownika długu
            startActivityForResult(intent, 1);            // Start aktywności edycji
        });


        // Obsługa przycisku usuwania długu
        Button deleteDebtButton = findViewById(R.id.deleteDebtButton);
        deleteDebtButton.setOnClickListener(v -> {
            removeDebt();
            Toast.makeText(DebtDetailActivity.this, "Dług został usunięty", Toast.LENGTH_SHORT).show();
            finish();
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

    private void removeDebt() {
        for (int i = 0; i < debtList.size(); i++) {
            if (debtList.get(i).name.equals(currentDebtName)) {
                debtList.remove(i);
                saveDebtsToFile();
                break;
            }
        }

        // Czyścimy pola tekstowe
        debtTitleView.setText("");
        debtAmountView.setText("");
        debtDescriptionView.setText("");
        debtUserView.setText("");
    }

    private void saveDebtsToFile() {
        try (FileOutputStream fos = openFileOutput("debts.txt", MODE_PRIVATE);
             OutputStreamWriter writer = new OutputStreamWriter(fos)) {

            for (AddDebtActivity.Debt debt : debtList) {
                writer.write(debt.name + ";" + debt.amount + ";" + debt.additionalInfo + ";" + debt.user + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd zapisu do pliku: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadDebtsFromFile() {
        debtList.clear();
        try (FileInputStream fis = openFileInput("debts.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(";");
                if (parts.length == 4) {
                    String name = parts[0];
                    double amount = Double.parseDouble(parts[1]);
                    String additionalInfo = parts[2];
                    String user = parts[3];

                    AddDebtActivity.Debt debt = new AddDebtActivity.Debt(name, amount, additionalInfo, user);
                    debtList.add(debt);

                    Log.d("DebtDetailActivity", "Załadowano dług: " + debt.name + " - " + debt.amount);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd odczytu z pliku", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(this, "Błąd formatu danych w pliku", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String updatedName = data.getStringExtra("updatedDebtName");
            double updatedAmount = data.getDoubleExtra("updatedDebtAmount", -1);
            String updatedInfo = data.getStringExtra("updatedDebtInfo");
            String updatedUser = data.getStringExtra("updatedDebtUser");

            if (updatedName == null || updatedInfo == null || updatedUser == null || updatedAmount < 0) {
                Toast.makeText(this, "Nieprawidłowe dane zaktualizowanego długu", Toast.LENGTH_SHORT).show();
                return;
            }

            for (AddDebtActivity.Debt debt : debtList) {
                if (debt.name.equals(currentDebtName)) {
                    debt.name = updatedName;
                    debt.amount = updatedAmount;
                    debt.additionalInfo = updatedInfo;
                    debt.user = updatedUser;
                    break;
                }
            }

            saveDebtsToFile();

            debtTitleView.setText(updatedName);
            debtAmountView.setText(String.valueOf(updatedAmount));
            debtDescriptionView.setText(updatedInfo);
            debtUserView.setText(updatedUser);

            Toast.makeText(this, "Dług został zaktualizowany", Toast.LENGTH_SHORT).show();
        }
    }
}
