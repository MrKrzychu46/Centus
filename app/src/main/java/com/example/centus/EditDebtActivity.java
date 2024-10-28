package com.example.centus;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class EditDebtActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_debt);

        Button saveDebtButton = findViewById(R.id.saveDebtButton);

        saveDebtButton.setOnClickListener(v -> {
            // Logika zapisu danych długu (do zaimplementowania)
            finish(); // Cofnięcie po zapisaniu zmian
        });
    }
}
