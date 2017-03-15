package com.feedss.base.util;

import java.io.ByteArrayOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES加密对象
 *
 * @author skyfalling.
 */
public class Encryptor {

    public static  final Encryptor DEFAULT=new Encryptor("28F907E9293A6F2E");
    private static final String HEX_STRING = "0123456789ABCDEF";
    private static final String CIPHER_NAME = "AES";
    private byte[] key;
    private String encoding;

    /**
     * 构造方法
     *
     * @param key      密钥
     * @param encoding 编码
     */
    Encryptor(String key, String encoding) {
        this.encoding = encoding;
        try {
            this.key = key.getBytes(encoding);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param key 密钥
     */
    Encryptor(String key) {
        this(key, "UTF-8");
    }

    /**
     * @param src
     *
     * @return
     *
     * @throws Exception
     */
    public String encrypt(String src) {
        try {
            return byte2Hex(encrypt(src.getBytes(encoding)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param src
     *
     * @return
     *
     * @throws Exception
     */
    public String decrypt(String src) {
        try {
            return new String(decrypt(hex2Byte(src)), encoding);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param src
     *
     * @return
     *
     * @throws Exception
     */
    private byte[] encrypt(byte[] src) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_NAME);
            SecretKeySpec secureKey = new SecretKeySpec(key, CIPHER_NAME);
            cipher.init(Cipher.ENCRYPT_MODE, secureKey);//设置密钥和加密形式
            return cipher.doFinal(src);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param src
     *
     * @return
     *
     * @throws Exception
     */
    private byte[] decrypt(byte[] src) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER_NAME);
            SecretKeySpec secureKey = new SecretKeySpec(key, CIPHER_NAME);//设置加密Key
            cipher.init(Cipher.DECRYPT_MODE, secureKey);//设置密钥和解密形式
            return cipher.doFinal(src);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将字节数组转成编码成16进制数字
     *
     * @param bytes
     *
     * @return
     */
    private static String byte2Hex(byte[] bytes) {
        char[] buffer = new char[bytes.length * 2];
        for (int i = 0, j = 0; i < bytes.length; ++i) {
            int u = (bytes[i] + 256) % 256;
            buffer[j++] = HEX_STRING.charAt(u >>> 4);
            buffer[j++] = HEX_STRING.charAt(u & 0xf);
        }
        return new String(buffer);
    }

    /**
     * 将16进制数字二进制数组
     *
     * @param bytes
     *
     * @return
     */
    private static byte[] hex2Byte(String bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
        // 将每2位16进制整数组装成一个字节
        for (int i = 0; i < bytes.length(); i += 2) {
            baos.write(
                    HEX_STRING.indexOf(bytes.charAt(i)) << 4
                            | HEX_STRING.indexOf(bytes.charAt(i + 1))
            );
        }
        return baos.toByteArray();
    }

}
