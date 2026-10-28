package com.Tome.tome.domain.book.infra;

import java.util.regex.Pattern;

/** ISBN 검사 숫자를 검증하고 ISBN10을 ISBN13으로 통일한다. 정규식 일치만으로 유효성을 판단하지 않는다. */
public final class Isbn {
    private static final Pattern TOKENS = Pattern.compile("[0-9Xx-]+");
    /** 정적 유틸리티이므로 인스턴스 생성을 막는다. */
    private Isbn() {}
    /** 공백으로 연결된 ISBN 목록도 처리한다. 유효한 ISBN13을 우선하고 없으면 ISBN10을 변환한다. */
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
    /** 978/979 접두부와 1·3 가중치 검사 숫자를 확인한다. */
    private static boolean valid13(String s) {
        if (!s.matches("97[89][0-9]{10}")) return false;
        int sum = 0;
        for (int i = 0; i < 13; i++) sum += (s.charAt(i) - '0') * (i % 2 == 0 ? 1 : 3);
        return sum % 10 == 0;
    }
    /** 마지막 X를 포함한 ISBN10의 11 나머지 검사를 수행한다. */
    private static boolean valid10(String s) {
        if (!s.matches("[0-9]{9}[0-9X]")) return false;
        int sum = 0;
        for (int i = 0; i < 10; i++) sum += (s.charAt(i) == 'X' ? 10 : s.charAt(i) - '0') * (10 - i);
        return sum % 11 == 0;
    }
    /** ISBN10 앞에 978을 붙인 뒤 ISBN13 검사 숫자를 다시 계산한다. */
    private static String to13(String s) {
        String prefix = "978" + s.substring(0, 9);
        int sum = 0;
        for (int i = 0; i < 12; i++) sum += (prefix.charAt(i) - '0') * (i % 2 == 0 ? 1 : 3);
        return prefix + ((10 - sum % 10) % 10);
    }
}
