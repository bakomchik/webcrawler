package ru.hh.test.utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

public class Utils {

    public static String replaceNonPrintable(String s) {
        return s.replaceAll("\\p{C}", "");
    }

    public static Long calculateSegment(Long loadFactor, Long idx) { //possibly Overflow
        return loadFactor * ((idx / loadFactor) + 1);
    }

    public static URL baseUrl(URL url) {
        try {
            return new URL(url.getProtocol(), url.getHost(), url.getPort(), "");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Cant build base url. from " + url.toString(), e);
        }
    }

    public static  String relative(URL url,String path) {
        try {
            return new URL(url.getProtocol(),url.getHost(),url.getPort(),path).toString();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Cant form url",e);
        }

    }
    public static String decode(URL url) {
        try {
            return URLDecoder.decode(url.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Cant decode url. "+url,e);
        }
    }
    public static URL baseUrl(String urlstr) {

        URL url = urlSafe(urlstr);
        return baseUrl(url);

    }

    public static URL urlSafe(String urlstr) {
        try {
            return new URL(urlstr);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Cant build base url. from " + urlstr, e);
        }
    }
}
