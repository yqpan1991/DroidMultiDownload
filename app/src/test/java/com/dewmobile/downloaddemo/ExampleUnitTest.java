package com.dewmobile.downloaddemo;

import android.net.Uri;
import android.util.Log;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testEncode(){
        try {
            System.out.print(URLDecoder.decode("中国","UTF-8"));
            System.out.print(URLDecoder.decode("http://7.hunlang.com/kkk12/%E6%B8%B8%E6%88%8F%E7%8E%AF%E5%A2%83%E7%BB%84%E4%BB%B6%E5%AE%89%E8%A3%85%E5%8C%85.exe","UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}