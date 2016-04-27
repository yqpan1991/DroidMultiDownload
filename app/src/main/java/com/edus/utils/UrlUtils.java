package com.edus.utils;

import android.net.Uri;

/**
 * Created by Panda on 2016/4/27.
 */
public class UrlUtils {

    private static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
    public static String getEncodedUrl(String url){
        return Uri.encode(url, ALLOWED_URI_CHARS);
    }

}
