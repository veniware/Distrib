package com.veniware.distrib;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;

import java.util.Hashtable;

/**
 * Created by veni on 10/8/2015.
 */
public class Messaging {

    public static void sendSms(String destination, String text) {
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(destination, null, text, null, null);
    }

    public static byte[] getChat(Context context) {
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) return new byte[0];

        ContentResolver contentResolver = HttpListener.self.getContentResolver();
        Cursor query = contentResolver.query(Uri.parse("content://sms"), new String[]{"*"}, null, null, null);

        if (query.getCount() > 0) {
            Hashtable<String, String> resolveContact = new Hashtable<>();

            StringBuilder sb = new StringBuilder();

            long count = 0;

            while (query.moveToNext()) {
                String addr = query.getString(query.getColumnIndex("address")).replace(" ", "").replace("|", "");
                String date = query.getString(query.getColumnIndex("date"));
                String body = escHTML(query.getString(query.getColumnIndex("body")).replace("|", ""));
                String type = query.getString(query.getColumnIndex("type"));

                String name;
                if (resolveContact.containsKey(addr)) {
                    name = resolveContact.get(addr);
                } else {
                    name = numberToContactName(addr).replace("|", "");
                    resolveContact.put(addr, name);
                }

                sb.append(addr + "|");
                sb.append(name + "|");
                sb.append(date + "|");
                sb.append(body + "|");
                sb.append(type + "|");

                if (count++ > 128) break;
            }

            if (query != null && !query.isClosed()) query.close();
            resolveContact.clear();

            return sb.toString().getBytes();
        }
        return new byte[0];
    }

    public static String numberToContactName(String number) {
        ContentResolver contentResolver = HttpListener.self.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor query = contentResolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if (query == null) return number;

        if(query.moveToFirst()) {
            String name = query.getString(query.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
            if (query != null && !query.isClosed()) query.close();
            if (name != null) return name;
        }

        if (query != null && !query.isClosed()) query.close();
        return number;
    }

    public static String escHTML(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&#34;")
                   .replace("'", "&#39;")
                   .replace("/", "&#47;");
    }
}
