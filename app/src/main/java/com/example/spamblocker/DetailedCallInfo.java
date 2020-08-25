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
    private int recid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_call_info);
        Intent intent = getIntent();
        recid = (int) intent.getExtras().get("incoming_recid");
        db = Room.databaseBuilder(getApplicationContext(), SpamBlockerDB.class, "spamblocker.db").createFromAsset("databases/spamblocker.db").allowMainThreadQueries().build();
        getAndDisplayCallData(recid);
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

    private void getAndDisplayCallData(int recid) {
        FilteredCalls data = db.callsDao().getByRecID(recid);
        TextView temp = (TextView) findViewById(R.id.tempText);
        temp.setText(data.number);
    }
}