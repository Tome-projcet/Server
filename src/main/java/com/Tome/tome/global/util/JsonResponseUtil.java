package com.Tome.tome.global.util;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

// Security 필터(로그인/로그아웃/401/403)에서는 컨트롤러를 거치지 않으므로 JSON을 직접 써서 응답한다
public class JsonResponseUtil {

    public static void write(HttpServletResponse response, int status, Map<String, ?> body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, ?> entry : body.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;
            json.append("\"").append(escape(entry.getKey())).append("\":");
            Object value = entry.getValue();
            if (value == null) {
                json.append("null");
            } else if (value instanceof Number || value instanceof Boolean) {
                json.append(value);
            } else {
                json.append("\"").append(escape(value.toString())).append("\"");
            }
        }
        json.append("}");

        response.getWriter().write(json.toString());
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
