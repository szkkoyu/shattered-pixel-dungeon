package com.shatteredpixel.shatteredpixeldungeon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

/**
 * 研究用のパラメータを、テキストファイル(research_params.txt)で管理する。
 *
 * ファイルの例:
 *   mob_multiplier=1.0
 *   auto=true
 *
 * mob_multiplier : 敵の数の倍率(2階以降)
 * auto           : true なら、フロアを作るたびに、倍率が自動で更新されて、ファイルに書き戻される
 */
public class ResearchParams {

	private static final String FILE_NAME = "research_params.txt";
	public static final float MULT_MIN = 0.2f;
	public static final float MULT_MAX = 3.0f;

	private static boolean pathPrinted = false;

	/** 敵の数の倍率。ファイルが無い、または不正なら 1.0 */
	public static float mobMultiplier() {
		String v = load().getProperty("mob_multiplier");
		if (v == null) return 1.0f;
		try {
			return clamp(Float.parseFloat(v.trim()));
		} catch (NumberFormatException e) {
			return 1.0f;
		}
	}

	/** 自動更新が有効か */
	public static boolean autoMode() {
		String v = load().getProperty("auto");
		return v != null && v.trim().equalsIgnoreCase("true");
	}

	/** 倍率を更新して、ファイルへ書き戻す(他の項目は保持する) */
	public static void setMobMultiplier(float value) {
		Properties p = load();
		p.setProperty("mob_multiplier", String.format(Locale.ROOT, "%.3f", clamp(value)));
		File f = new File(FILE_NAME);
		try (Writer w = new OutputStreamWriter(new FileOutputStream(f), StandardCharsets.UTF_8)) {
			p.store(w, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static float clamp(float x) {
		return Math.max(MULT_MIN, Math.min(MULT_MAX, x));
	}

	private static Properties load() {
		Properties p = new Properties();
		File f = new File(FILE_NAME);
		if (!pathPrinted) {
			System.out.println("[SPD-LOG] research params file: " + f.getAbsolutePath());
			pathPrinted = true;
		}
		if (!f.exists()) return p;
		try (InputStreamReader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
			p.load(r);
		} catch (IOException | RuntimeException e) {
			e.printStackTrace();
		}
		return p;
	}
}

//2026/10/22/1:29 > 11/4/13:24 /w Claude