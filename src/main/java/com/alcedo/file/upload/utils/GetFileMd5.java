package com.alcedo.file.upload.utils;

import org.apache.commons.io.FileUtils;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * @ClassName: GetFileMd5
 * @Author:  Alcedo
 * @CreateTime: 2022-03-08
 * @Description:
 */
public class GetFileMd5 {

    public static void main(String[] args) {
        String[] strHex = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"};
        String filePath = "D:\\data\\车辆三维模型.zip";

        //way1
        try {
            StringBuffer md5BufferOne = new StringBuffer();
            MessageDigest toMd5WayOne = MessageDigest.getInstance("MD5");
            byte[] md5One = toMd5WayOne.digest(FileUtils.readFileToByteArray(new File(filePath)));
            for (int md5 : md5One) {
                if (md5 < 0) {
                    md5 += 256;
                }
                int d1 = md5 / 16;
                int d2 = md5 % 16;
                md5BufferOne.append(strHex[d1] + strHex[d2]);
            }
            System.out.println(md5BufferOne.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //way2
        try {
            StringBuffer md5BufferTwo = new StringBuffer();
            MessageDigest toMd5WayTwo = MessageDigest.getInstance("MD5");
            byte[] md5Two = toMd5WayTwo.digest(FileUtils.readFileToByteArray(new File(filePath)));
            for (int md5 : md5Two) {
                int d = md5;
                if (d < 0) {
                    d = md5 & 0xff;
                    // 与上一行效果等同
                    // i += 256;
                }
                if (d < 16) md5BufferTwo.append("0");
                md5BufferTwo.append(Integer.toHexString(d));
            }
            System.out.println(md5BufferTwo.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //way3
        try {
            byte[] buffer = new byte[1024 * 8];
            int len = 0;
            MessageDigest toMd5WayThree = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(filePath);
            while ((len = fis.read(buffer)) != -1) {
                toMd5WayThree.update(buffer, 0, len);
            }
            fis.close();
            byte[] md5Three = toMd5WayThree.digest();
            BigInteger bigIntegerMd5 = new BigInteger(1, md5Three);
            System.out.println(bigIntegerMd5.toString(16));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //way4
        try {
            String md5DigestAsHex = DigestUtils.md5DigestAsHex(new FileInputStream(filePath));
            System.out.println(md5DigestAsHex);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
