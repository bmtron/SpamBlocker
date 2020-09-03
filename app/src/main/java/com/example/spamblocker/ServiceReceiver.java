package com.example.spamblocker;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.room.Room;

public class ServiceReceiver extends BroadcastReceiver {
    private SpamBlockerDB db;
    boolean blocked = false;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onReceive(final Context con, Intent intent) {

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String inc = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);

        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING) && inc != null) {
            String incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            blocked = checkIfNumberInContacts(con, incomingNumber);

            if (blocked) {
                buildNotification(con, incomingNumber);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public boolean checkIfNumberInContacts(Context con, String incomingNumber) {
        boolean wasBlocked = false;

        db = Room.databaseBuilder(con, SpamBlockerDB.class, "spamblocker.db").createFromAsset("databases/spamblocker.db").allowMainThreadQueries().build();

        SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy hh:mm");
        FilteredCalls call = new FilteredCalls();
        call.calltime = formatter.format(Calendar.getInstance().getTime());
        call.deleted = 0;
        call.number = incomingNumber;
        call.whitelisted = 0;
        call.callcount = 1;

        List<FilteredCalls> allCalls = db.callsDao().getAll();
        boolean callerExistsInDB = false;
        for (FilteredCalls item : allCalls) {

            if (incomingNumber.equals(item.number)) {
                callerExistsInDB = true;
                String updatedTime = Calendar.getInstance().getTime().toString();
                db.callsDao().updateCallTime(updatedTime, item.recID);
                int currentCount = db.callsDao().getCallCount(item.recID);
                db.callsDao().updateCallCount(currentCount + 1, item.recID);
            }
        }

        ContentResolver contentResolver = con.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));

        String contact = "";

        Cursor c = contentResolver.query(uri, new String[]{ContactsContract.PhoneLookup.HAS_PHONE_NUMBER}, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    contact = c.getString(c.getColumnIndex("NUMBER"));
                } while (c.moveToNext());
            }
        }

        if (contact == "" && !checkIfNumberIsWhitelisted(con, incomingNumber)) {
            TelecomManager telecomManager = (TelecomManager) con.getSystemService(Context.TELECOM_SERVICE);
            telecomManager.endCall();
            wasBlocked = true;
            if (!callerExistsInDB) {
                db.callsDao().insert(call);
            }
        }
        return wasBlocked;
    }

    public boolean checkIfNumberIsWhitelisted(Context con, String incomingNumber) {

        List<FilteredCalls> whiteList = db.callsDao().getWhiteList(1, 0);
        boolean isWhitelisted = false;
        for (FilteredCalls item : whiteList) {
            if (item.number.equals(incomingNumber)) {
                isWhitelisted = true;
            }

        }

        return isWhitelisted;

    }

    public void buildNotification(Context con, String incomingNumber) {
        Intent intent = new Intent(con, DetailedCallInfo.class);
        intent.putExtra("number", incomingNumber);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(con, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(con, Global.BLOCKED_CALL_CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icons)
                .setContentTitle("An unknown number " + incomingNumber + " has been blocked.")
                .setContentText("Tap here to see more information.")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManager notificationManager = (NotificationManager) con.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(3030, builder.build());
    }
}
