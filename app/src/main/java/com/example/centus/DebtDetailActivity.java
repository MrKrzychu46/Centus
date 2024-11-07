package com.example.centus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DebtDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debt_detail_activity);

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
        notificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DebtDetailActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });

        ImageButton mainButton = findViewById(R.id.appLogo);
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DebtDetailActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ImageButton addingDebtsButton = findViewById(R.id.addingDebtsButton);
        addingDebtsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DebtDetailActivity.this, AddDebtActivity.class);
                startActivity(intent);
            }
        });
    }
}
