package com.edus.utils;

import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by panyongqiang on 16/4/14.
 */
public class FileUtils {

    public static String getTitleFromUrl(String urlStr) {
        try {
            URL url = new URL(urlStr);
            String file = url.getFile();
            int index = file.lastIndexOf("/");
            if (index >= 0) {
                file = file.substring(index + 1);
            }
            file = URLDecoder.decode(file, "UTF-8");
            return file;
        } catch (Exception e1) {
        }

        return "";
    }



}
