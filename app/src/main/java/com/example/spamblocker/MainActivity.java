package com.example.spamblocker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.room.Room;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

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
        setupRefreshBtn();
        setupAddManualButton();
        createNotificationChannel();
    }

    private void setupCallList(List<FilteredCalls> calls) {
        for (final FilteredCalls item : calls) {
            final RelativeLayout mainCallLayout = new RelativeLayout(this);
            final TextView callInfo = new TextView(this);
            final TextView callTime = new TextView(this);
            final TextView callCount = new TextView(this);
            final Switch btnSetWhiteList = new Switch(this);
            callInfo.setText(item.number);
            callTime.setText(item.calltime);
            callCount.setText(item.callcount.toString());
            btnSetWhiteList.setId(View.generateViewId());
            setCallListLayoutAndListeners(mainCallLayout, callInfo, callTime, btnSetWhiteList, callCount, item.number);
            setupSwitchToggle(btnSetWhiteList, item.recID);
            if (item.whitelisted == 1) {
                btnSetWhiteList.setChecked(true);
            }
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

    private void setupCallCountLayout(TextView callCount) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)callCount.getLayoutParams();
        params.addRule(RelativeLayout.ALIGN_PARENT_END);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        callCount.setLayoutParams(params);
    }
    private void setCallListLayoutAndListeners(RelativeLayout mainCallLayout, TextView callInfo, TextView callTime, Switch btnSetWhiteList, TextView callCount, String number) {
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
        mainCallLayout.addView(callCount);
        setupNumberTextView(callInfo);
        setupCallTimeTextView(callTime);
        setupCallCountLayout(callCount);
        setupWhitelistSwitchLayout(btnSetWhiteList);
        setupCallTouch(mainCallLayout, number);
    }

    private void setupCallTouch(RelativeLayout mainCallLayout, final String number) {
        mainCallLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Intent toDetails = new Intent(getApplicationContext(), DetailedCallInfo.class);
                toDetails.putExtra("number", number);
                startActivity(toDetails);
                finish();
                return false;
            }
        });
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
    private void setupRefreshBtn() {
        Button btnRefresh = (Button) findViewById(R.id.refreshData);
        btnRefresh.setText("Refresh Data");
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                startActivity(getIntent());
            }
        });
    }

    private void setupAddManualButton() {
        Button btnAdd = (Button) findViewById(R.id.btnAddManually);
        btnAdd.setText("+");
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent goToManualAdd = new Intent(MainActivity.this, ManualAdd.class);
                startActivity(goToManualAdd);
                finish();
            }
        });
    }

    private void nukeDB() {
        db.callsDao().nukeDB();
        finish();
        startActivity(getIntent());
    }

    private void createNotificationChannel() {
        CharSequence name = "Blocked Call Notification";
        String description = "The blocked call notification";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = new NotificationChannel(Global.BLOCKED_CALL_CHANNEL_ID, name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

}