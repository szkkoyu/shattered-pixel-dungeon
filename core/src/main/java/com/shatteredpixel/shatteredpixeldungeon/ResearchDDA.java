package com.shatteredpixel.shatteredpixeldungeon;

/**
 * 難易度調整のルール(ルールベース版)。
 *
 * 入力: 直前のフロアでの、HPの純増減 ÷ 最大HP(hpRatio)
 *   例) -0.5 → 最大HPの半分を、純粋に失った
 *       +0.2 → 最大HPの2割ぶん、増えた(回復や、レベルアップなど)
 *
 * 出力: 次のフロアの、敵の数の倍率
 *
 * 数値(LOW, HIGH, STEP など)は、仮の値。実際のログを見て、調整すること。
 */
public class ResearchDDA {

	/** hpRatio がこれより小さい(大きく減った) → 苦戦とみなして、敵を減らす */
	public static final float LOW = -0.40f;

	/** hpRatio がこれより大きい(ほとんど減っていない) → 余裕とみなして、敵を増やす */
	public static final float HIGH = -0.10f;

	/** 1フロアでの、倍率の変化量 */
	public static final float STEP = 0.15f;

	/** 自動調整の範囲(ゲームが壊れないよう、手動の許容範囲より狭くする) */
	public static final float MIN = 0.5f;
	public static final float MAX = 2.0f;

	public static float next(float current, float hpRatio) {
		float m = current;
		if (hpRatio < LOW) {
			m -= STEP;
		} else if (hpRatio > HIGH) {
			m += STEP;
		}
		return Math.max(MIN, Math.min(MAX, m));
	}
}
