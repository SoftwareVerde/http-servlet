package com.softwareverde.util;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    public static Integer parseInt(final String numberString) {
        if (numberString == null) { return null; }

        try {
            return NumberFormat.getNumberInstance(java.util.Locale.US).parse(numberString.trim()).intValue();
        }
        catch (Exception e) {
            return 0;
        }
    }

    public static Long parseLong(final String numberString) {
        if (numberString == null) { return null; }

        try {
            return NumberFormat.getNumberInstance(java.util.Locale.US).parse(numberString.trim()).longValue();
        }
        catch (Exception e) {
            return 0L;
        }
    }

    public static Float parseFloat(final String numberString) {
        if (numberString == null) { return null; }

        try {
            return NumberFormat.getNumberInstance(java.util.Locale.US).parse(numberString.trim()).floatValue();
        }
        catch (Exception e) {
            return 0.0f;
        }
    }

    public static Double parseDouble(final String numberString) {
        if (numberString == null) { return null; }

        try {
            return NumberFormat.getNumberInstance(java.util.Locale.US).parse(numberString.trim()).doubleValue();
        }
        catch (Exception e) {
            return 0.0D;
        }
    }

    public static Boolean coalesce(final Boolean bool) {
        return Util.coalesce(bool, false);
    }
    public static Float coalesce(final Float f) { return Util.coalesce(f, 0.0F); }
    public static Integer coalesce(final Integer number) {
        return Util.coalesce(number, 0);
    }
    public static Long coalesce(final Long number) {
        return Util.coalesce(number, 0L);
    }
    public static String coalesce(final String string) {
        return Util.coalesce(string, "");
    }
    public static <T> T coalesce(final T value, final T defaultValue) {
        return (value != null ? value : defaultValue);
    }

    public static String md5(final String s) {
        try {
            final MessageDigest messageDigest = java.security.MessageDigest.getInstance("MD5");
            final byte[] array = messageDigest.digest(s.getBytes());
            final StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < array.length; ++i) {
                stringBuilder.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return stringBuilder.toString();
        }
        catch (NoSuchAlgorithmException e) { }
        return null;
    }

    public static String sha256(final String s) {
        try {
            final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            final byte[] array = messageDigest.digest(s.getBytes());
            final StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < array.length; ++i) {
                stringBuilder.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return stringBuilder.toString();
        }
        catch (NoSuchAlgorithmException e) { }
        return null;
    }

    private static byte[] _readStream(final InputStream inputStream) {
        final byte[] bytes;

        try {
            final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] readBuffer = new byte[1024];
            while ((nRead = inputStream.read(readBuffer, 0, readBuffer.length)) != -1) {
                buffer.write(readBuffer, 0, nRead);
            }
            inputStream.close();

            buffer.flush();
            bytes = buffer.toByteArray();
            buffer.close();

        }
        catch (final Exception e) {
            return (new byte[0]);
        }

        return bytes;
    }

    public static String streamToString(final InputStream inputStream) {
        try {
            return new String(Util._readStream(inputStream), "UTF-8");
        }
        catch (final Exception e) { }

        return "";
    }

    /**
     * Returns the content of a file contained within the project's resources directory.
     * @param filename  - The file name of the resource within the resources. It should be prefixed with a forward-slash.
     * @return          - The contents of the resource if found, otherwise an empty string.
     */
    public static String getResource(final String filename) {
        final InputStream resourceStream = Util.class.getResourceAsStream(filename);
        if (resourceStream == null) { return ""; }
        return Util.streamToString(resourceStream);
    }

    public static byte[] getFileContents(final File file) {
        try {
            final InputStream inputStream = new FileInputStream(file);
            return Util._readStream(inputStream);
        }
        catch (final Exception e) { }

        return (new byte[0]);
    }

    public static byte[] getFileContents(final String filename) {
        final File file = new File(filename);
        if ((! file.exists()) || (! file.canRead())) { return new byte[0]; }
        return Util.getFileContents(file);
    }
}