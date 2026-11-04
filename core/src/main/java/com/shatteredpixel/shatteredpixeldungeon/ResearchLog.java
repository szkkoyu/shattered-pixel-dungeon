package com.shatteredpixel.shatteredpixeldungeon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * 研究用ログと、難易度の自動更新の呼び出し口。
 *
 * Dungeon.newLevel() から、次の順で呼ばれる:
 *   1) beforeCreate() : level.create() の直前。前のフロアの結果から、倍率を更新する
 *   2) onNewLevel()   : level.create() の直後。CSVへ1行を追記する
 *
 * CSVの1行 = 「このフロアの情報」 + 「直前のフロアでの結果」
 */
public class ResearchLog {

	private static final String FILE_NAME = "research_floor_log.csv";
	private static final String HEADER =
			"session,run,time,depth,branch,type,feeling,mobs,mob_multiplier,"
			+ "hp_entry,hp_max,prev_depth,prev_time,prev_kills,prev_hp_net,prev_hp_ratio,auto";

	private static final long SESSION = System.currentTimeMillis();
	private static boolean pathPrinted = false;

	private static int run = 0;

	// 直前のフロアに入った時点の記録
	private static boolean hasPrev = false;
	private static int prevDepth;
	private static int prevBranch;
	private static String prevType = "";
	private static float prevDuration;
	private static int prevKills;
	private static int prevHpEntry;

	// beforeCreate() で計算した、直前フロアの結果(NaN = 計算できなかった)
	private static float prevHpRatio = Float.NaN;

	/** 直前の記録を、今回の判定に使えるか(新しいプレイの始まりなら、使えない) */
	private static boolean prevIsValid(int depth, int branch) {
		return hasPrev && Statistics.duration >= prevDuration && !(depth == 1 && branch == 0);
	}

	/** 通常のフロアか(ボス階や、特殊な階の結果は、判断に使わない) */
	private static boolean isNormalFloor(String type, int branch) {
		return branch == 0
				&& !type.endsWith("BossLevel")
				&& !type.equals("LastLevel")
				&& !type.equals("DeadEndLevel");
	}

	/** level.create() の直前に呼ぶ。前のフロアの結果から、倍率を更新する。 */
	public static void beforeCreate(int depth, int branch) {
		prevHpRatio = Float.NaN;
		if (!prevIsValid(depth, branch) || Dungeon.hero == null) return;
		if (!isNormalFloor(prevType, prevBranch)) return;

		float hpMax = Dungeon.hero.HT;
		if (hpMax <= 0) return;

		prevHpRatio = (Dungeon.hero.HP - prevHpEntry) / hpMax;

		if (ResearchParams.autoMode()) {
			float current = ResearchParams.mobMultiplier();
			float next = ResearchDDA.next(current, prevHpRatio);
			if (next != current) {
				ResearchParams.setMobMultiplier(next);
			}
			System.out.println("[SPD-LOG] DDA hpRatio=" + String.format(Locale.ROOT, "%.3f", prevHpRatio)
					+ " multiplier " + current + " -> " + next);
		}
	}

	/** level.create() の直後に呼ぶ。CSVへ1行を追記する。 */
	public static void onNewLevel(int depth, int branch, String type, String feeling, int mobs) {
		float duration = Statistics.duration;
		int kills = Statistics.enemiesSlain;
		int hp = 0;
		int hpMax = 0;
		if (Dungeon.hero != null) {
			hp = Dungeon.hero.HP;
			hpMax = Dungeon.hero.HT;
		}

		boolean valid = prevIsValid(depth, branch);
		if (!valid) {
			run++;
			hasPrev = false;
		}

		String prevCols;
		if (valid) {
			String ratio = Float.isNaN(prevHpRatio) ? "" : String.format(Locale.ROOT, "%.3f", prevHpRatio);
			prevCols = prevDepth
					+ "," + String.format(Locale.ROOT, "%.1f", duration - prevDuration)
					+ "," + (kills - prevKills)
					+ "," + (hp - prevHpEntry)
					+ "," + ratio;
		} else {
			prevCols = ",,,,";
		}

		String line = SESSION + "," + run + "," + System.currentTimeMillis()
				+ "," + depth + "," + branch + "," + type + "," + feeling
				+ "," + mobs + "," + ResearchParams.mobMultiplier()
				+ "," + hp + "," + hpMax + "," + prevCols
				+ "," + ResearchParams.autoMode();
		write(line);

		hasPrev = true;
		prevDepth = depth;
		prevBranch = branch;
		prevType = type;
		prevDuration = duration;
		prevKills = kills;
		prevHpEntry = hp;
	}

	private static void write(String line) {
		try {
			File f = new File(FILE_NAME);
			boolean isNew = !f.exists();
			if (!pathPrinted) {
				System.out.println("[SPD-LOG] research log file: " + f.getAbsolutePath());
				pathPrinted = true;
			}
			try (Writer w = new OutputStreamWriter(new FileOutputStream(f, true), StandardCharsets.UTF_8)) {
				if (isNew) {
					w.write(HEADER + "\n");
				}
				w.write(line + "\n");
			}
		} catch (IOException | RuntimeException e) {
			e.printStackTrace();
		}
	}
}

//プレイログをcsvファイルに書き出すためのプログラム（2026/10/21/23:56 > 2026/10/22/13:44 > 2026/11/4/13:25） /w Claude