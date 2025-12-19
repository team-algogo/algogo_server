package com.ssafy.algogo.problem.entity;

public enum DifficultyType {

	UNRATED(PlatformType.BOJ, 0),

	BRONZE_5(PlatformType.BOJ, 5),
	BRONZE_4(PlatformType.BOJ, 6),
	BRONZE_3(PlatformType.BOJ, 7),
	BRONZE_2(PlatformType.BOJ, 8),
	BRONZE_1(PlatformType.BOJ, 9),

	SILVER_5(PlatformType.BOJ, 10),
	SILVER_4(PlatformType.BOJ, 11),
	SILVER_3(PlatformType.BOJ, 12),
	SILVER_2(PlatformType.BOJ, 13),
	SILVER_1(PlatformType.BOJ, 14),

	GOLD_5(PlatformType.BOJ, 15),
	GOLD_4(PlatformType.BOJ, 16),
	GOLD_3(PlatformType.BOJ, 17),
	GOLD_2(PlatformType.BOJ, 18),
	GOLD_1(PlatformType.BOJ, 19),

	PLATINUM_5(PlatformType.BOJ, 20),
	PLATINUM_4(PlatformType.BOJ, 21),
	PLATINUM_3(PlatformType.BOJ, 22),
	PLATINUM_2(PlatformType.BOJ, 23),
	PLATINUM_1(PlatformType.BOJ, 24),

	DIAMOND_5(PlatformType.BOJ, 25),
	DIAMOND_4(PlatformType.BOJ, 26),
	DIAMOND_3(PlatformType.BOJ, 27),
	DIAMOND_2(PlatformType.BOJ, 28),
	DIAMOND_1(PlatformType.BOJ, 29),

	RUBY_5(PlatformType.BOJ, 30),
	RUBY_4(PlatformType.BOJ, 31),
	RUBY_3(PlatformType.BOJ, 32),
	RUBY_2(PlatformType.BOJ, 33),
	RUBY_1(PlatformType.BOJ, 34),

	LEVEL_0(PlatformType.PROGRAMMERS, 3),
	LEVEL_1(PlatformType.PROGRAMMERS, 7),
	LEVEL_2(PlatformType.PROGRAMMERS, 12),
	LEVEL_3(PlatformType.PROGRAMMERS, 17),
	LEVEL_4(PlatformType.PROGRAMMERS, 22),
	LEVEL_5(PlatformType.PROGRAMMERS, 27),

	D1(PlatformType.SWEA, 5),
	D2(PlatformType.SWEA, 8),
	D3(PlatformType.SWEA, 12),
	D4(PlatformType.SWEA, 16),
	D5(PlatformType.SWEA, 20),
	D6(PlatformType.SWEA, 24),
	D7(PlatformType.SWEA, 28),
	D8(PlatformType.SWEA, 32);

	private final PlatformType platform;
	private final int score;

	DifficultyType(PlatformType platform, int score) {
		this.platform = platform;
		this.score = score;
	}

	public PlatformType platform() {
		return platform;
	}

	public int score() {
		return score;
	}
}
