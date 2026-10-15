package com.Tome.tome.catalog;

import org.springframework.stereotype.Component;
import java.util.*;

/** Stable names shared by onboarding, external KDC mapping and recommendation. */
@Component
public class GenreTaxonomy {
    private static final Map<String, List<String>> ALIASES = new LinkedHashMap<>();
    static {
        ALIASES.put("소설", List.of("소설", "한국소설", "외국소설", "소설/시/희곡"));
        ALIASES.put("시/에세이", List.of("시/에세이", "시", "에세이", "수필"));
        ALIASES.put("인문", List.of("인문", "인문학", "철학", "심리학"));
        ALIASES.put("사회", List.of("사회", "사회과학", "정치", "법률"));
        ALIASES.put("경제/경영", List.of("경제/경영", "경제경영", "경제", "경영"));
        ALIASES.put("자기계발", List.of("자기계발"));
        ALIASES.put("과학/기술", List.of("과학/기술", "과학", "자연과학", "기술과학", "컴퓨터/모바일"));
        ALIASES.put("예술", List.of("예술", "예술/대중문화"));
        ALIASES.put("역사/여행", List.of("역사/여행", "역사", "역사/문화", "여행", "지리"));
        ALIASES.put("생활/건강", List.of("생활/건강", "건강/취미", "가정/요리/뷰티", "요리", "육아"));
        ALIASES.put("언어", List.of("언어", "외국어", "어학"));
        ALIASES.put("종교", List.of("종교", "종교/역학"));
        ALIASES.put("기타", List.of("기타", "총류"));
    }
    public List<String> names() { return List.copyOf(ALIASES.keySet()); }
    public String canonical(String name) {
        if (name == null) return "기타";
        String value = name.trim();
        return ALIASES.entrySet().stream().filter(e -> e.getValue().contains(value))
                .map(Map.Entry::getKey).findFirst().orElse("기타");
    }
    public List<String> aliases(String name) { return ALIASES.get(canonical(name)); }
    public boolean supported(String name) {
        return name != null && ALIASES.values().stream().anyMatch(v -> v.contains(name.trim()));
    }
    public String fromKdc(String code) {
        if (code == null || !code.trim().matches("[0-9]{3}(\\.[0-9]+)?")) return "기타";
        String kdc = code.trim();
        int section = Integer.parseInt(kdc.substring(0, 3));
        if (section >= 800 && section < 900) {
            // KDC literature: the final digit classifies poetry, fiction and essays.
            return section % 10 == 1 || section % 10 == 4 ? "시/에세이" : "소설";
        }
        if (section >= 320 && section < 330) return "경제/경영";
        if (section >= 510 && section < 520 || section >= 590 && section < 600) return "생활/건강";
        return switch (section / 100) {
            case 1 -> "인문";
            case 2 -> "종교";
            case 3 -> "사회";
            case 4, 5 -> "과학/기술";
            case 6 -> "예술";
            case 7 -> "언어";
            case 9 -> "역사/여행";
            default -> "기타";
        };
    }
}
