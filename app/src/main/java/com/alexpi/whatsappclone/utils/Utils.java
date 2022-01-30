package com.alexpi.whatsappclone.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.PhoneNumberUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Utils {

    private static final DateFormat format1 = new SimpleDateFormat("HH:mm"),
            format2 = new SimpleDateFormat("dd/MM/yyyy"),
            format3 = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public static String format1(long timestamp){
        return format1.format(new Date(timestamp));
    }
    public static String format2(long timestamp){ return format2.format(new Date(timestamp)); }
    public static String format3(long timestamp){ return format3.format(new Date(timestamp)); }

    public static String formatNumberCompat(String number){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return PhoneNumberUtils.formatNumber(number, Locale.getDefault().getCountry());
        else
            return PhoneNumberUtils.formatNumber(number);
    }
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return null != activeNetwork;
    }

}
