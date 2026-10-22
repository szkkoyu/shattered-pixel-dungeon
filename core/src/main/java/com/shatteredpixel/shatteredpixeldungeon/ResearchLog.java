package com.shatteredpixel.shatteredpixeldungeon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * 研究用ログ。フロアが新しく作られるたびに、CSVへ1行を追記する。
 *
 * 1行の内容:
 *   - このフロア自体の情報(階層、種類、特徴、敵の数、倍率、入場時のHP)
 *   - 「直前のフロア」での結果(経過時間、倒した敵の数、HPの純増減)
 *
 * つまり、フロアNの行を見れば、フロアN-1でプレイヤーがどうなったかが分かる。
 */
public class ResearchLog {

	private static final String FILE_NAME = "research_floor_log.csv";
	private static final String HEADER =
			"session,run,time,depth,branch,type,feeling,mobs,mob_multiplier,"
			+ "hp_entry,hp_max,prev_depth,prev_time,prev_kills,prev_hp_net";

	// プログラムを起動した時刻(起動をまたいだ記録を区別する)
	private static final long SESSION = System.currentTimeMillis();
	private static boolean pathPrinted = false;

	// 何回目のプレイ(ラン)か
	private static int run = 0;

	// 直前のフロアに入った時点の記録
	private static boolean hasPrev = false;
	private static int prevDepth;
	private static float prevDuration;
	private static int prevKills;
	private static int prevHpEntry;

	public static void onNewLevel(int depth, int branch, String type, String feeling, int mobs) {
		float duration = Statistics.duration;
		int kills = Statistics.enemiesSlain;
		int hp = 0;
		int hpMax = 0;
		if (Dungeon.hero != null) {
			hp = Dungeon.hero.HP;
			hpMax = Dungeon.hero.HT;
		}

		// 新しいプレイの始まりを判定する(1階の生成、または時間が巻き戻った場合)
		if (!hasPrev || duration < prevDuration || (depth == 1 && branch == 0)) {
			run++;
			hasPrev = false;
		}

		String prevCols;
		if (hasPrev) {
			prevCols = prevDepth
					+ "," + String.format(Locale.ROOT, "%.1f", duration - prevDuration)
					+ "," + (kills - prevKills)
					+ "," + (hp - prevHpEntry);
		} else {
			prevCols = ",,,";
		}

		String line = SESSION + "," + run + "," + System.currentTimeMillis()
				+ "," + depth + "," + branch + "," + type + "," + feeling
				+ "," + mobs + "," + ResearchParams.mobMultiplier()
				+ "," + hp + "," + hpMax + "," + prevCols;
		write(line);

		// 今回の入場時点を、次回のために保存する
		hasPrev = true;
		prevDepth = depth;
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

//プレイログをcsvファイルに書き出すためのプログラム（2026/10/21/23:56 > 2026/10/22/13:44） /w Claude