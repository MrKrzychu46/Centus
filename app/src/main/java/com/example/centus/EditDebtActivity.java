package com.example.centus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

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

        ImageButton notificationsButton = findViewById(R.id.notificationButton);
        notificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditDebtActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });

        ImageButton mainButton = findViewById(R.id.appLogo);
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditDebtActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ImageButton addingDebtsButton = findViewById(R.id.addingDebtsButton);
        addingDebtsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditDebtActivity.this, AddDebtActivity.class);
                startActivity(intent);
            }
        });


    }
}
