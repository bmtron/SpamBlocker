package com.example.spamblocker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.service.controls.Control;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class ManualAdd extends AppCompatActivity {
    Button btnBackToMain;
    Button btnAdd;
    EditText phoneNumberText;
    PopupWindow popupWindow;
    private SpamBlockerDB db;
    ConstraintLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_add);
        db = Room.databaseBuilder(getApplicationContext(), SpamBlockerDB.class, "spamblocker.db").createFromAsset("databases/spamblocker.db").allowMainThreadQueries().build();
        setupButtons();
    }

    private void setupButtons() {
        setupBackToMainBtn();
        setupAddBtn();
    }

    private void setupBackToMainBtn() {
        btnBackToMain = (Button) findViewById(R.id.buttonBackToMain);
        btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backToMain = new Intent(ManualAdd.this, MainActivity.class);
                startActivity(backToMain);
                finish();
            }
        });
    }

    private void setupAddBtn() {
        btnAdd = (Button) findViewById(R.id.btnAddToDb);
        phoneNumberText = (EditText) findViewById(R.id.editTextPhoneNumber);
        mainLayout = (ConstraintLayout) findViewById(R.id.manualAddLayout);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNUmber = phoneNumberText.getText().toString();

                if (phoneNUmber == null || phoneNUmber.isEmpty()) {
                    popupWindow = new PopupWindow(getApplicationContext());

                    LinearLayout popupMainLayout = new LinearLayout(getApplicationContext());
                    TextView popupText = new TextView(getApplicationContext());
                    Button popupBtn = new Button(getApplicationContext());
                    popupWindow.showAtLocation(mainLayout, Gravity.BOTTOM, 10, 10);
                    popupWindow.update(50, 50, 300, 300);
                    popupBtn.setText("Ok");
                    popupBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            popupWindow.dismiss();
                        }
                    });

                    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                    popupText.setText("You must enter a phone number. Click ok to continue.");
                    mainLayout.addView(popupText, params);
                    popupWindow.setContentView(mainLayout);
                    setContentView(popupMainLayout);
                }
            }
        });

    }
}