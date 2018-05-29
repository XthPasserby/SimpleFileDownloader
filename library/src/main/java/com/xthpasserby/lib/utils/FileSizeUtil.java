package com.xthpasserby.lib.utils;

public class FileSizeUtil {
    private static final int UNIT = 1024;
    private static final char[] UNIT_CHAR = {'K', 'M', 'G', 'T', 'P' , 'E'};

    public static String byteToSize(long byteCount) {
        if (byteCount < UNIT) return byteCount + " B";
        int exp = (int) (Math.log(byteCount) / Math.log(UNIT));
        return String.format("%.1f %sB", byteCount / Math.pow(UNIT, exp), UNIT_CHAR[exp - 1]);
    }
}
