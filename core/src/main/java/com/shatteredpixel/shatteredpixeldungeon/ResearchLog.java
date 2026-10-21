package com.shatteredpixel.shatteredpixeldungeon;

import com.shatteredpixel.shatteredpixeldungeon.ResearchParams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/** 研究用のログをCSVに追記する。 */
public class ResearchLog {

	private static final String FILE_NAME = "research_floor_log.csv";
	private static boolean pathPrinted = false;

	public static void write(String line) {
		try {
			File f = new File(FILE_NAME);
			boolean isNew = !f.exists();
			if (!pathPrinted) {
				System.out.println("[SPD-LOG] research log file: " + f.getAbsolutePath());
				pathPrinted = true;
			}
			try (Writer w = new OutputStreamWriter(new FileOutputStream(f, true), StandardCharsets.UTF_8)) {
				if (isNew) {
					w.write("time,depth,branch,type,feeling,mobs,mob_multiplier\n");
				}
				w.write(line + "\n");
			}
		} catch (IOException | RuntimeException e) {
			e.printStackTrace();
		}
	}
}

//プレイログをcsvファイルに書き出すためのプログラム（2026/10/21/23:56） /w Claude