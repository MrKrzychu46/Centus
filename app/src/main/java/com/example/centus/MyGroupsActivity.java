package com.example.centus;

import android.app.Activity;
import android.os.Bundle; //DODANE
import androidx.appcompat.app.AppCompatActivity; //DODANE
import android.content.Intent; //DODANE2
import android.view.View; //DODANE2
import android.widget.ArrayAdapter;
import android.widget.Button; //DODANE2
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;

public class MyGroupsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mygroups);

        //DODANE2


        ImageButton notificationsButton = findViewById(R.id.notificationButton);
        notificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyGroupsActivity.this, NotificationActivity.class);
                startActivity(intent);
            }
        });

        ImageButton mainButton = findViewById(R.id.appLogo);
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyGroupsActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ImageButton addingDebtsButton = findViewById(R.id.addingDebtsButton);
        addingDebtsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MyGroupsActivity.this, AddDebtActivity.class);
                startActivity(intent);
            }
        });

        Button profileButton = findViewById(R.id.profileButton);

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyGroupsActivity.this, MyProfileActivity.class);
                startActivity(intent);
            }
        });

        Button settingsButton = findViewById(R.id.settingsButton);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyGroupsActivity.this, OptionsActivity.class);
                startActivity(intent);
            }
        });
        ListView groupsDebtsListView = findViewById(R.id.groupsDebtsListView);

        // Przykładowa lista danych, które będą wyświetlane
        ArrayList<String> groupDebts = new ArrayList<>();
        groupDebts.add("Grupa 1 - Dług: 100 zł");
        groupDebts.add("Grupa 2 - Dług: 200 zł");
        groupDebts.add("Grupa 3 - Dług: 150 zł");

        // Tworzymy adapter do ListView z niestandardowym układem
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                R.layout.list_item, // Używamy własnego układu
                R.id.itemText,      // ID TextView w list_item.xml
                groupDebts          // Lista danych
        );

        // Ustawiamy adapter dla ListView
        groupsDebtsListView.setAdapter(adapter);

    }

}