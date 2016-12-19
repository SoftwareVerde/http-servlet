package com.softwareverde.util;

public class Base64 {
    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    private static final int[] toInt = new int[128];

    static {
        for (int i=0; i < ALPHABET.length; ++i){
            toInt[ALPHABET[i]]= i;
        }
    }

    /**
     * Translates the specified byte array into Base64 string.
     * @param buffer    - The byte array.
     * @return          - The translated Base64 string.
     */
    public static String encode(final byte[] buffer) {
        final int size = buffer.length;
        final char[] ar = new char[((size + 2) / 3) * 4];
        int a = 0;
        int i = 0;
        while (i < size){
            final byte b0 = buffer[i++];
            final byte b1 = (i < size) ? buffer[i++] : 0;
            final byte b2 = (i < size) ? buffer[i++] : 0;

            int mask = 0x3F;
            ar[a++] = ALPHABET[(b0 >> 2) & mask];
            ar[a++] = ALPHABET[((b0 << 4) | ((b1 & 0xFF) >> 4)) & mask];
            ar[a++] = ALPHABET[((b1 << 2) | ((b2 & 0xFF) >> 6)) & mask];
            ar[a++] = ALPHABET[b2 & mask];
        }

        switch (size % 3) {
            case 1: ar[--a]  = '=';
            case 2: ar[--a]  = '=';
        }

        return new String(ar);
    }

    /**
     * Translates the specified Base64 string into a byte array.
     * @param string    - The Base64 string.
     * @return          - The byte array.
     */
    public static byte[] decode(final String string) {
        final int delta = (string.endsWith( "==" ) ? 2 : (string.endsWith( "=" ) ? 1 : 0));
        final Integer bufferSize = string.length() * 3/4 - delta;
        final byte[] buffer = new byte[bufferSize];
        final int mask = 0xFF;
        int index = 0;
        for (int i = 0; i < string.length(); i += 4){
            final char i1C = string.charAt(i);

            if (i+1 >= string.length()) {
                System.err.println("ERROR: Unexpected end of Base64 string.");
                return new byte[bufferSize];
            }
            final char i2C = string.charAt(i+1);

            if (i1C >= 128) {
                System.err.println("ERROR: Invalid Base64 character: "+ i1C +".");
                return new byte[bufferSize];
            }
            if (i2C >= 128) {
                System.err.println("ERROR: Invalid Base64 character: "+ i2C +".");
                return new byte[bufferSize];
            }

            final int c0 = toInt[i1C];
            final int c1 = toInt[i2C];
            buffer[index++] = (byte) (((c0 << 2) | (c1 >> 4)) & mask);
            if (index >= buffer.length){
                return buffer;
            }
            final int c2 = toInt[string.charAt(i+2)];
            buffer[index++] = (byte) (((c1 << 4) | (c2 >> 2)) & mask);
            if (index >= buffer.length){
                return buffer;
            }
            final int c3 = toInt[string.charAt(i+3)];
            buffer[index++] = (byte) (((c2 << 6) | c3) & mask);
        }
        return buffer;
    }

}