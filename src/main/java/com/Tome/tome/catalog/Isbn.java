package com.Tome.tome.catalog;

import java.util.regex.Pattern;

public final class Isbn {
    private static final Pattern TOKENS = Pattern.compile("[0-9Xx-]+");
    private Isbn() {}
    public static String normalize(String input) {
        if (input == null) return "";
        var matcher = TOKENS.matcher(input);
        String converted = "";
        while (matcher.find()) {
            String token = matcher.group().replace("-", "").toUpperCase();
            if (valid13(token)) return token;
            if (valid10(token)) converted = to13(token);
        }
        return converted;
    }
    private static boolean valid13(String s) {
        if (!s.matches("97[89][0-9]{10}")) return false;
        int sum = 0;
        for (int i = 0; i < 13; i++) sum += (s.charAt(i) - '0') * (i % 2 == 0 ? 1 : 3);
        return sum % 10 == 0;
    }
    private static boolean valid10(String s) {
        if (!s.matches("[0-9]{9}[0-9X]")) return false;
        int sum = 0;
        for (int i = 0; i < 10; i++) sum += (s.charAt(i) == 'X' ? 10 : s.charAt(i) - '0') * (10 - i);
        return sum % 11 == 0;
    }
    private static String to13(String s) {
        String prefix = "978" + s.substring(0, 9);
        int sum = 0;
        for (int i = 0; i < 12; i++) sum += (prefix.charAt(i) - '0') * (i % 2 == 0 ? 1 : 3);
        return prefix + ((10 - sum % 10) % 10);
    }
}
