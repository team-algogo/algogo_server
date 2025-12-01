package com.ssafy.algogo.problem.entity;

public enum DifficultyType {
    /* 백준 */
    UNRATED(PlatformType.BOJ),
    BRONZE_5(PlatformType.BOJ),
    BRONZE_4(PlatformType.BOJ),
    BRONZE_3(PlatformType.BOJ),
    BRONZE_2(PlatformType.BOJ),
    BRONZE_1(PlatformType.BOJ),

    SILVER_5(PlatformType.BOJ),
    SILVER_4(PlatformType.BOJ),
    SILVER_3(PlatformType.BOJ),
    SILVER_2(PlatformType.BOJ),
    SILVER_1(PlatformType.BOJ),

    GOLD_5(PlatformType.BOJ),
    GOLD_4(PlatformType.BOJ),
    GOLD_3(PlatformType.BOJ),
    GOLD_2(PlatformType.BOJ),
    GOLD_1(PlatformType.BOJ),

    PLATINUM_5(PlatformType.BOJ),
    PLATINUM_4(PlatformType.BOJ),
    PLATINUM_3(PlatformType.BOJ),
    PLATINUM_2(PlatformType.BOJ),
    PLATINUM_1(PlatformType.BOJ),

    DIAMOND_5(PlatformType.BOJ),
    DIAMOND_4(PlatformType.BOJ),
    DIAMOND_3(PlatformType.BOJ),
    DIAMOND_2(PlatformType.BOJ),
    DIAMOND_1(PlatformType.BOJ),

    RUBY_5(PlatformType.BOJ),
    RUBY_4(PlatformType.BOJ),
    RUBY_3(PlatformType.BOJ),
    RUBY_2(PlatformType.BOJ),
    RUBY_1(PlatformType.BOJ),

    /* 프로그래머스 */
    LEVEL_0(PlatformType.PROGRAMMERS),
    LEVEL_1(PlatformType.PROGRAMMERS),
    LEVEL_2(PlatformType.PROGRAMMERS),
    LEVEL_3(PlatformType.PROGRAMMERS),
    LEVEL_4(PlatformType.PROGRAMMERS),
    LEVEL_5(PlatformType.PROGRAMMERS),

    /* SWEA */
    D1(PlatformType.SWEA),
    D2(PlatformType.SWEA),
    D3(PlatformType.SWEA),
    D4(PlatformType.SWEA),
    D5(PlatformType.SWEA),
    D6(PlatformType.SWEA),
    D7(PlatformType.SWEA),
    D8(PlatformType.SWEA);

    private final PlatformType platform;

    DifficultyType(PlatformType platform) {
        this.platform = platform;
    }

    public PlatformType platform() {
        return platform;
    }
}
