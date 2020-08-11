package com.example.spamblocker;

import android.Manifest;
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
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;

public class ServiceReceiver extends BroadcastReceiver {
    private SpamBlockerDB db;

    @Override
    public void onReceive(final Context con, Intent intent) {


        final TelephonyManager telephony = (TelephonyManager) con.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(new PhoneStateListener() {
            @RequiresApi(api = Build.VERSION_CODES.P)
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);
                //Toast.makeText(con, incomingNumber, Toast.LENGTH_LONG).show();
                if (TelephonyManager.CALL_STATE_RINGING == state) {
                    checkIfNumberInContacts(con, incomingNumber, telephony);
                }
            }
        }, PhoneStateListener.LISTEN_CALL_STATE);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public void checkIfNumberInContacts(Context con, String incomingNumber, TelephonyManager telephony) {
        db = Room.databaseBuilder(con, SpamBlockerDB.class, "spamblocker.db").createFromAsset("databases/spamblocker.db").allowMainThreadQueries().build();

        FilteredCalls call = new FilteredCalls();
        call.calltime = Calendar.getInstance().getTime().toString();
        call.deleted = 0;
        call.number = incomingNumber;
        call.whitelisted = 0;

        List<FilteredCalls> allCalls = db.callsDao().getAll();
        boolean callerExistsInDB = false;
        for (FilteredCalls item : allCalls) {
            if (incomingNumber.equals(item.number)) {
                callerExistsInDB = true;
                String updatedTime = Calendar.getInstance().getTime().toString();
                db.callsDao().updateCallTime(updatedTime, item.recID);
            }
        }
        ContentResolver contentResolver = con.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));

        //contentResolver.query(uri, new String[] {ContactsContract.PhoneLookup.HAS_PHONE_NUMBER}, null, null);
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
            if (!callerExistsInDB) {
                db.callsDao().insert(call);
            }
        }
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

}
