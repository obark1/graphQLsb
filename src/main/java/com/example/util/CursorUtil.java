package com.example.util;

import java.util.Base64;
import java.nio.charset.StandardCharsets;

public class CursorUtil {

    // Encode an ID into a cursor
    public static String encodeCursor(Long id) {
        if (id == null) {
            return null;
        }
        String data = "student:" + id;
        return Base64.getEncoder()
                .encodeToString(data.getBytes(StandardCharsets.UTF_8));
    }

    // Decode a cursor back to an ID
    public static Long decodeCursor(String cursor) {
        if (cursor == null || cursor.isEmpty()) {
            return null;
        }
        try {
            byte[] decoded = Base64.getDecoder().decode(cursor);
            String data = new String(decoded, StandardCharsets.UTF_8);
            // Extract ID from "student:123" format
            return Long.parseLong(data.split(":")[1]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor: " + cursor);
        }
    }
}
