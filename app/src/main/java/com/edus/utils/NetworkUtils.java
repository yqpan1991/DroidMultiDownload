package com.edus.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static android.telephony.TelephonyManager.NETWORK_TYPE_1xRTT;
import static android.telephony.TelephonyManager.NETWORK_TYPE_CDMA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EDGE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EHRPD;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_0;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_A;
import static android.telephony.TelephonyManager.NETWORK_TYPE_EVDO_B;
import static android.telephony.TelephonyManager.NETWORK_TYPE_GPRS;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSDPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSPAP;
import static android.telephony.TelephonyManager.NETWORK_TYPE_HSUPA;
import static android.telephony.TelephonyManager.NETWORK_TYPE_IDEN;
import static android.telephony.TelephonyManager.NETWORK_TYPE_LTE;
import static android.telephony.TelephonyManager.NETWORK_TYPE_UMTS;

/**
 * Created by panyongqiang on 16/4/14.
 */
public class NetworkUtils {

    private static final int MOBILE_TYPE_UNKNOWN = 0;
    private static final int MOBILE_TYPE_2G = 1;
    private static final int MOBILE_TYPE_3G = 2;
    private static final int MOBILE_TYPE_4G = 3;

    public static NetworkInfo getNetworkInfo(Context context) {
        try {
            ConnectivityManager connMan = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connMan.getActiveNetworkInfo();
            return info;
        } catch (Exception e) {
        }
        return null;
    }

    public static boolean isNetworkAvailable(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    public static boolean isWifiOr4GAvailable(Context context) {
        return isWifiAvailable(context) || is4GAvailable(context);
    }

    public static boolean isWifiAvailable(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public static boolean isMobileNetAvailable(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    public static boolean is4GAvailable(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE && (getMobileSubType(info.getSubtype()) == MOBILE_TYPE_4G));
    }

    private static int getMobileSubType(int subType) {
        switch (subType) {
            case NETWORK_TYPE_GPRS:
            case NETWORK_TYPE_EDGE:
            case NETWORK_TYPE_CDMA:
            case NETWORK_TYPE_1xRTT:
            case NETWORK_TYPE_IDEN:
                return MOBILE_TYPE_2G;
            case NETWORK_TYPE_UMTS:
            case NETWORK_TYPE_EVDO_0:
            case NETWORK_TYPE_EVDO_A:
            case NETWORK_TYPE_HSDPA:
            case NETWORK_TYPE_HSUPA:
            case NETWORK_TYPE_HSPA:
            case NETWORK_TYPE_EVDO_B:
            case NETWORK_TYPE_EHRPD:
            case NETWORK_TYPE_HSPAP:
                return MOBILE_TYPE_3G;
            case NETWORK_TYPE_LTE:
                return MOBILE_TYPE_4G;
            default:
                return MOBILE_TYPE_UNKNOWN;
        }
    }

    public static boolean is3GAvailable(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE && getMobileSubType(info.getSubtype()) == MOBILE_TYPE_3G);
    }

    public static boolean is2GAvailable(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE && getMobileSubType(info.getSubtype()) == MOBILE_TYPE_2G);
    }

    public static boolean is2GOr3GAvailable(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        if (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int mobileType = getMobileSubType(info.getSubtype());
            return mobileType == MOBILE_TYPE_2G || mobileType == MOBILE_TYPE_3G;
        }
        return false;
    }

}
