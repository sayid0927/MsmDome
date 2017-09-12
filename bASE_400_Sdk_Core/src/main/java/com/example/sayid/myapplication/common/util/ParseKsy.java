package com.example.sayid.myapplication.common.util;

import com.example.sayid.myapplication.common.data.ConfigConst;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ParseKsy {
    private final static String TAG = "ParseKsy";

    public static String aaaa = "hksz$'6ysd80^%LE";
    public static String bbbb = "AES";

    public static String encode(String in) {
        if (ConfigConst.IS_PASS) {
            return encodePrivate(in);
        }
        return in;
    }

    private static String encodePrivate(String in) {
        String hex = "";
        try {
            byte[] bytIn = in.getBytes("UTF-8");
            SecretKeySpec skeySpec = new SecretKeySpec(aaaa.getBytes("UTF-8"), bbbb);
            Cipher cipher = Cipher.getInstance(bbbb);
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
            byte[] bytOut = cipher.doFinal(bytIn);
            hex = byte2hexString(bytOut);

        } catch (Exception e) {
            //.e(TAG, "ParseK：001:" + e.toString());
        }
        return hex;
    }

    public static String decode(String hex) {
        if (ConfigConst.IS_PASS) {
            return decodePrivate(hex);
        }
        return hex;
    }

    private static String decodePrivate(String hex) {
        String rr = "";
        try {
            byte[] bytIn = hex2Bin(hex);
            SecretKeySpec skeySpec = new SecretKeySpec(aaaa.getBytes("UTF-8"), bbbb);
            Cipher cipher = Cipher.getInstance(bbbb);
            cipher.init(Cipher.DECRYPT_MODE, skeySpec);
            byte[] bytOut = cipher.doFinal(bytIn);
            rr = new String(bytOut, "UTF-8");

        } catch (Exception e) {
            //.e(TAG, "ParseK：002:" + e.toString());
        }

        return rr;
    }

    private static byte[] hex2Bin(String src) {
        if (src.length() < 1)
            return null;
        byte[] encrypted = new byte[src.length() / 2];
        for (int i = 0; i < src.length() / 2; i++) {
            int high = Integer.parseInt(src.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(src.substring(i * 2 + 1, i * 2 + 2), 16);
            encrypted[i] = (byte) (high * 16 + low);
        }
        return encrypted;
    }

    private static String byte2hexString(byte buf[]) {
        StringBuffer strbuf = new StringBuffer(buf.length * 2);
        int i;
        for (i = 0; i < buf.length; i++) {
            strbuf.append(Integer.toString((buf[i] >> 4) & 0xf, 16) +
                    Integer.toString(buf[i] & 0xf, 16));
        }

        return strbuf.toString();
    }


    public static void main(String args[]) {
        System.out.println(ParseKsy.encode("http://192.168.10.92:6000/yunpay_server"));
        System.out.println(ParseKsy.encode("http://www.yphgrad101.com:9000"));


//		System.out.println(ParseKsy.decode("98306b87a4d44e26b5977588628a745b5a281d3cf04b1d04315c6866d2291a29"));

    }
}

