package com.example.spamblocker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telecom.Call;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.security.Permission;
import java.security.Permissions;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SpamBlockerDB db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions(new String[] {Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.ANSWER_PHONE_CALLS}, 20);
        db = Room.databaseBuilder(getApplicationContext(), SpamBlockerDB.class, "spamblocker.db").createFromAsset("databases/spamblocker.db").allowMainThreadQueries().build();
        List<FilteredCalls> calls = db.callsDao().getAll();
        setupCallList(calls);
        setupNuke();
       //id.setText(test);
    }

    private void setupCallList(List<FilteredCalls> calls) {
        for (final FilteredCalls item : calls) {
            final RelativeLayout mainCallLayout = new RelativeLayout(this);
            final TextView callInfo = new TextView(this);
            final TextView callTime = new TextView(this);
            final Switch btnSetWhiteList = new Switch(this);
            callInfo.setText(item.number);
            callTime.setText(item.calltime);
            btnSetWhiteList.setId(View.generateViewId());
            setCallListLayoutAndListeners(mainCallLayout, callInfo, callTime, btnSetWhiteList);
            setupSwitchToggle(btnSetWhiteList, item.recID);
        }
    }
    private void setupNuke() {
        Button nuke = (Button) findViewById(R.id.nuke);
        nuke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nukeDB();
            }
        });
    }
    private void setupNumberTextView(TextView callInfo) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)callInfo.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_START);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        callInfo.setLayoutParams(params);
    }

    private void setupCallTimeTextView(TextView callTime) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)callTime.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_START);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        callTime.setLayoutParams(params);
    }
    private void setupWhitelistSwitchLayout(Switch s) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)s.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        s.setLayoutParams(params);
    }
    private void setCallListLayoutAndListeners(RelativeLayout mainCallLayout, TextView callInfo, TextView callTime, Switch btnSetWhiteList) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 200);
        params.setMargins(0, 10, 0, 10);
        LinearLayout linL = (LinearLayout) findViewById(R.id.LinearLayout_List);
        linL.addView(mainCallLayout);

        mainCallLayout.setBackgroundColor(Color.parseColor("#E0DEDE"));
        mainCallLayout.setLayoutParams(params);
        callInfo.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_VERTICAL);
        callInfo.setTextColor(Color.parseColor("#000000"));
        mainCallLayout.addView(callTime);
        mainCallLayout.addView(callInfo);
        mainCallLayout.addView(btnSetWhiteList);
        setupNumberTextView(callInfo);
        setupCallTimeTextView(callTime);
        setupWhitelistSwitchLayout(btnSetWhiteList);
    }

    private void setupSwitchToggle(Switch btnSetWhiteList, final int id) {
        btnSetWhiteList.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    db.callsDao().updateTest(1, id);
                }
                else {
                    db.callsDao().updateTest(0, id);
                }
            }
        });
    }

    private void nukeDB() {
        db.callsDao().nukeDB();
        finish();
        startActivity(getIntent());
    }
}