package com.alcedo.file.upload.utils;

import java.util.UUID;

/**
 * @ClassName: Uuid
 * @Author:  Alcedo
 * @CreateTime: 2023-05-12
 * @Description:
 */
public class Uuid {

    public static String generateUniqueId(int length) {
        UUID uuid = UUID.randomUUID();
        String hash = uuid.toString().replaceAll("-", "");
        return hash.substring(0, Math.min(length, hash.length()));
    }
}
