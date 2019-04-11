package com.uguke.java.util;

import java.io.UnsupportedEncodingException;

/**
 * 功能描述：Base64基类
 * @author LeiJue
 */
public final class Base64 {

    public static final String DEFAULT_ENCODING = "UTF-8";

    private Base64() {}

    public static String encodeToString(byte[] bytes) throws UnsupportedEncodingException {
        return encodeToString(bytes, DEFAULT_ENCODING);
    }

    public static String encodeToString(byte[] bytes, String encoding) throws UnsupportedEncodingException {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes cannot be null");
        }
        return new String(encodeToBytes(bytes), encoding);
    }

    public static byte[] encode(String str) throws UnsupportedEncodingException {
        return encode(str, DEFAULT_ENCODING);
    }

    public static byte[] encode(String str, String encoding) throws UnsupportedEncodingException {
        if (str == null) {
            throw new IllegalArgumentException("str cannot be null");
        }
        return encodeToBytes(str.getBytes(encoding));
    }

    public static byte[] encodeToBytes(byte[] bytes) {
        /* If we received a null argument, exit this method. */
        if (bytes == null) {
            throw new IllegalArgumentException("bytes cannot be null");
        }
        /*
         * Declare working variables including an array of bytes that will
         * contain the encoded data to be returned to the caller. Note that the
         * encoded array is about 1/3 larger than the input. This is because
         * every group of 3 bytes is being encoded into 4 bytes.
         */
        // index into source (bytes)
        int iSrcIdx;
        // index into destination (byteDest)
        int iDestIdx;
        byte[] byteDest = new byte[((bytes.length + 2) / 3) * 4];

        /*
         * Walk through the input array, 24 bits at a time, converting them from
         * 3 groups of 8 to 4 groups of 6 with two unset bits between. as per
         * Base64 spec see
         * http://www.javaworld.com/javaworld/javatips/jw-javatip36-p2.html for
         * example explanation
         */
        for (iSrcIdx = 0, iDestIdx = 0; iSrcIdx < bytes.length - 2; iSrcIdx += 3) {
            byteDest[iDestIdx++] = (byte) ((bytes[iSrcIdx] >>> 2) & 077);
            byteDest[iDestIdx++] = (byte) ((bytes[iSrcIdx + 1] >>> 4) & 017 | (bytes[iSrcIdx] << 4) & 077);
            byteDest[iDestIdx++] = (byte) ((bytes[iSrcIdx + 2] >>> 6) & 003 | (bytes[iSrcIdx + 1] << 2) & 077);
            byteDest[iDestIdx++] = (byte) (bytes[iSrcIdx + 2] & 077);
        }

        /*
         * If the number of bytes we received in the input array was not an even
         * multiple of 3, convert the remaining 1 or 2 bytes.
         */
        if (iSrcIdx < bytes.length) {
            byteDest[iDestIdx++] = (byte) ((bytes[iSrcIdx] >>> 2) & 077);
            if (iSrcIdx < bytes.length - 1) {
                byteDest[iDestIdx++] = (byte) ((bytes[iSrcIdx + 1] >>> 4) & 017 | (bytes[iSrcIdx] << 4) & 077);
                byteDest[iDestIdx++] = (byte) ((bytes[iSrcIdx + 1] << 2) & 077);
            } else {
                byteDest[iDestIdx++] = (byte) ((bytes[iSrcIdx] << 4) & 077);
            }
        }

        /*
         * Use the encoded data as indexes into the Base64 alphabet. (The Base64
         * alphabet is completely documented in RFC 1521.)
         */
        for (iSrcIdx = 0; iSrcIdx < iDestIdx; iSrcIdx++) {
            if (byteDest[iSrcIdx] < 26) {
                byteDest[iSrcIdx] = (byte) (byteDest[iSrcIdx] + 'A');
            } else if (byteDest[iSrcIdx] < 52) {
                byteDest[iSrcIdx] = (byte) (byteDest[iSrcIdx] + 'a' - 26);
            } else if (byteDest[iSrcIdx] < 62) {
                byteDest[iSrcIdx] = (byte) (byteDest[iSrcIdx] + '0' - 52);
            } else if (byteDest[iSrcIdx] < 63) {
                byteDest[iSrcIdx] = '+';
            } else {
                byteDest[iSrcIdx] = '/';
            }
        }

        /* Pad any unused bytes in the destination string with '=' characters. */
        for (; iSrcIdx < byteDest.length; iSrcIdx++) {
            byteDest[iSrcIdx] = '=';
        }

        return byteDest;
    }

    public static String decodeToString(byte[] bytes) throws UnsupportedEncodingException {
        return decodeToString(bytes, DEFAULT_ENCODING);
    }

    public static String decodeToString(byte[] bytes, String encoding) throws UnsupportedEncodingException {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes cannot be null");
        }
        byte [] temp = decodeToBytes(bytes);
        if (temp == null) {
            return "";
        }
        return new String(temp, encoding);
    }

    public static byte[] decode(String str) throws UnsupportedEncodingException {
        return decode(str, DEFAULT_ENCODING);
    }

    public static byte[] decode(String encoded, String encoding) throws IllegalArgumentException, UnsupportedEncodingException {
        if (null == encoded) {
            throw new IllegalArgumentException("encoded cannot be null");
        }
        return decodeToBytes(encoded.getBytes(encoding));
    }

    public static byte[] decodeToBytes(byte[] bytes) throws IllegalArgumentException {
        /* If we received a null argument, exit this method. */
        if (bytes == null) {
            throw new IllegalArgumentException("bytes cannot be null");
        }

        /*
         * Declare working variables including an array of bytes that will
         * contain the decoded data to be returned to the caller. Note that the
         * decoded array is about 3/4 smaller than the input. This is because
         * every group of 4 bytes is being encoded into 3 bytes.
         */
        // index into source (bytes)
        int iSrcIdx;
        // index from end of the src array (bytes)
        int reviSrcIdx;
        // index into destination (byteDest)
        int iDestIdx;
        byte[] byteTemp = new byte[bytes.length];

        /*
         * remove any '=' chars from the end of the bytes they would have
         * been padding to make it up to groups of 4 bytes note that I don't
         * need to remove it just make sure that when progressing throug array
         * we don't go past reviSrcIdx ;-)
         */
        for (reviSrcIdx = bytes.length; reviSrcIdx -1 > 0 && bytes[reviSrcIdx -1] == '='; reviSrcIdx--) {
            ; // do nothing. I'm just interested in value of reviSrcIdx
        }

        /* sanity check */
        if (reviSrcIdx -1 == 0) {
            return null;
            /* ie all padding */
        }

        /*
         * Set byteDest, this is smaller than bytes due to 4 -> 3 byte munge.
         * Note that this is an integer division! This fact is used in the logic
         * l8r. to make sure we don't fall out of the array and create an
         * OutOfBoundsException and also in handling the remainder
         */
        byte [] byteDest = new byte[((reviSrcIdx * 3) / 4)];

        /*
         * Convert from Base64 alphabet to encoded data (The Base64 alphabet is
         * completely documented in RFC 1521.) The order of the testing is
         * important as I use the '<' operator which looks at the hex value of
         * these ASCII chars. So convert from the smallest up
         *
         * do all of this in a new array so as not to edit the original input
         */
        for (iSrcIdx = 0; iSrcIdx < reviSrcIdx; iSrcIdx++) {
            if (bytes[iSrcIdx] == '+') {
                byteTemp[iSrcIdx] = 62;
            } else if (bytes[iSrcIdx] == '/') {
                byteTemp[iSrcIdx] = 63;
            } else if (bytes[iSrcIdx] < '0' + 10) {
                byteTemp[iSrcIdx] = (byte) (bytes[iSrcIdx] + 52 - '0');
            } else if (bytes[iSrcIdx] < ('A' + 26)) {
                byteTemp[iSrcIdx] = (byte) (bytes[iSrcIdx] - 'A');
            } else if (bytes[iSrcIdx] < 'a' + 26) {
                byteTemp[iSrcIdx] = (byte) (bytes[iSrcIdx] + 26 - 'a');
            }
        }

        /*
         * 4bytes -> 3bytes munge Walk through the input array, 32 bits at a
         * time, converting them from 4 groups of 6 to 3 groups of 8 removing
         * the two unset most significant bits of each sorce byte as this was
         * filler, as per Base64 spec. stop before potential buffer overun on
         * byteDest, remember that byteDest is 3/4 (integer division) the size
         * of input and won't necessary divide exactly (ie iDestIdx must be <
         * (integer div byteDest.length / 3)*3 see
         * http://www.javaworld.com/javaworld/javatips/jw-javatip36-p2.html for
         * example
         */
        for (iSrcIdx = 0, iDestIdx = 0; iSrcIdx < reviSrcIdx
                && iDestIdx < ((byteDest.length / 3) * 3); iSrcIdx += 4) {
            byteDest[iDestIdx++] = (byte) ((byteTemp[iSrcIdx] << 2) & 0xFC | (byteTemp[iSrcIdx + 1] >>> 4) & 0x03);
            byteDest[iDestIdx++] = (byte) ((byteTemp[iSrcIdx + 1] << 4) & 0xF0 | (byteTemp[iSrcIdx + 2] >>> 2) & 0x0F);
            byteDest[iDestIdx++] = (byte) ((byteTemp[iSrcIdx + 2] << 6) & 0xC0 | byteTemp[iSrcIdx + 3] & 0x3F);
        }

        /*
         * tidy up any remainders if iDestIdx >= ((byteDest.length / 3)*3) but
         * iSrcIdx < reviSrcIdx then we have at most 2 extra destination bytes
         * to fill and posiblr 3 input bytes yet to process
         */
        if (iSrcIdx < reviSrcIdx) {
            if (iSrcIdx < reviSrcIdx - 2) {
                // "3 input bytes left"
                byteDest[iDestIdx++] = (byte) ((byteTemp[iSrcIdx] << 2) & 0xFC | (byteTemp[iSrcIdx + 1] >>> 4) & 0x03);
                byteDest[iDestIdx] = (byte) ((byteTemp[iSrcIdx + 1] << 4) & 0xF0 | (byteTemp[iSrcIdx + 2] >>> 2) & 0x0F);
            } else if (iSrcIdx < reviSrcIdx - 1) {
                // "2 input bytes left"
                byteDest[iDestIdx] = (byte) ((byteTemp[iSrcIdx] << 2) & 0xFC | (byteTemp[iSrcIdx + 1] >>> 4) & 0x03);
            }
            /*
             * wont have just one input byte left (unless input wasn't base64
             * encoded ) due to the for loop steps and array sizes, after "="
             * pad removed, but for compleatness
             */
            else {
                throw new IllegalArgumentException("Warning: 1 input bytes left to process. This was not Base64 input");
            }
        }
        return byteDest;
    }

}
