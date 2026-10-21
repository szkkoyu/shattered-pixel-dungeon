package com.shatteredpixel.shatteredpixeldungeon;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** 研究用のパラメータを、テキストファイルから読み込む。 */
public class ResearchParams {

	private static final String FILE_NAME = "research_params.txt";
	private static boolean pathPrinted = false;

	/** 敵の数の倍率(2階以降に適用)。ファイルが無い、または不正なら 1.0 */
	public static float mobMultiplier() {
		return get("mob_multiplier", 1.0f, 0.2f, 3.0f);
	}

	private static float get(String key, float def, float min, float max) {
		File f = new File(FILE_NAME);
		if (!pathPrinted) {
			System.out.println("[SPD-LOG] research params file: " + f.getAbsolutePath());
			pathPrinted = true;
		}
		if (!f.exists()) return def;
		try (InputStreamReader r = new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8)) {
			Properties p = new Properties();
			p.load(r);
			String v = p.getProperty(key);
			if (v == null) return def;
			float x = Float.parseFloat(v.trim());
			return Math.max(min, Math.min(max, x));
		} catch (IOException | RuntimeException e) {
			e.printStackTrace();
			return def;
		}
	}
}

//2026/10/22/1:29 /w Claude