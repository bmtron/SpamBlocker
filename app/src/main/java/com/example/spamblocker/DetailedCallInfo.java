package com.example.spamblocker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class DetailedCallInfo extends AppCompatActivity {
    private SpamBlockerDB db;
    private String number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_call_info);
        Intent intent = getIntent();
        number = intent.getStringExtra("number");
        db = Room.databaseBuilder(getApplicationContext(), SpamBlockerDB.class, "spamblocker.db").createFromAsset("databases/spamblocker.db").allowMainThreadQueries().build();
        getAndDisplayCallData(number);
        Button mainScreen = (Button) findViewById(R.id.backToMain);
        setupBackButton(mainScreen);
    }

    private void setupBackButton(Button back) {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent main = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(main);
                finish();
            }
        });
    }

    private void getAndDisplayCallData(String number) {
        FilteredCalls data = db.callsDao().getCallByNumber(number);
        TextView numberText = (TextView) findViewById(R.id.phoneNumber);
        TextView callTimeText = (TextView) findViewById(R.id.lastCallDate);
        TextView callCountText = (TextView) findViewById(R.id.callCountText);
        numberText.setText(data.number);
        callTimeText.setText(data.calltime);
        callCountText.setText(data.callcount.toString());
    }
}