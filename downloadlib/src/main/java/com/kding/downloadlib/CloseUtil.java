package com.kding.downloadlib;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;

public class CloseUtil {

    public static void quietClose(BufferedSink sink) {
        if (null != sink) {
            try {
                sink.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void quietClose(BufferedSource source) {
        if (null != source) {
            try {
                source.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void quietClose(Buffer buffer) {
            if (null != buffer) {
                try {
                    buffer.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    public static void quietClose(RandomAccessFile file) {
        if (null != file) {
            try {
                file.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void quietClose(OutputStream os) {
        if (null != os) {
            try {
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void quietClose(InputStream is) {
        if (null != is) {
            try {
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
