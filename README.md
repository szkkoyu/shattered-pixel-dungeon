# ローグライクゲームにおける、行動ログを使った動的難易度調整(DDA)

大学3年次の研究として、オープンソースのローグライクゲーム
[Shattered Pixel Dungeon](https://github.com/00-Evan/shattered-pixel-dungeon)(v4.0.2、GPLv3)を改造し、
**プレイヤーの状態に合わせて、次の階層の難しさ(敵の数)を自動で調整する仕組み**を作っています。

> このリポジトリは、研究用に改造した非公式版です。元のゲームの作者・開発チームとは関係ありません。
> 元のゲームの README は [README_original.md](README_original.md) に残しています。

---

## 背景と問題意識

動的難易度調整(Dynamic Difficulty Adjustment, DDA)は、プレイヤーの腕前や状況に合わせて、
ゲームの難しさを自動で変える仕組みです。

最初に作ったのは、**階層を出る時点のHPの増減**だけを見るルールです。

```
HPの変化 = (階層を出るときのHP − 階層に入ったときのHP) ÷ 最大HP

大きく減った(−40%より下)  → 次の階層の敵を減らす
ほとんど減っていない(−10%より上) → 次の階層の敵を増やす
```

このルールには弱点があります。苦戦しても、階段の前で長く待機してHPを回復すれば、
降りる時点のHPはほぼ満タンになります。するとルールは「余裕がある」と誤って判断し、敵を増やしてしまいます。

そこで、**階層の中での行動(待機したターン数、待機中の回復量など)も記録して、難易度の調整に使う**ことを目指しています。

## 方針

- **まず動くものを作る。** 指導方針に合わせて、凝った設計よりも、小さく作って動かすことを優先しています。
- **比べられるようにする。** 最初のルール(HPだけ)は git のタグ `baseline-rule-v1` で固定し、書き換えずに比較用に残しています。
- **元のゲームへの変更は最小限にする。** 研究用の処理は新しいファイルにまとめ、元のコードには呼び出しの数行だけを足しています。
- **確認できていないことは「未確認」と書く。** 推測と、動かして確かめたことを区別しています。
- 機械学習(ロジスティック回帰、決定木など)は、余裕があれば試す「おまけ」と位置づけています。
  ルールで十分に検出できることを、機械学習で置き換えること自体は目的にしていません。

## 進み具合(2026年11月時点)

| 段階 | 内容 | 状態 |
|---|---|---|
| 0 | HPだけのルールで動く状態を保存(タグ `baseline-rule-v1`) | 完了 |
| 1 | 階層ごとのログを拡張(ゲームの識別子、死亡の記録) | 完了 |
| 2 | ターンごとの行動ログ(待機など) | 次に取り組む |
| 3 | 待機を考慮したルールの DDA を作り、設定ファイルで HP ルールと切り替えられるようにする | 未着手 |
| 4 | HP ルールと待機ルールを、自分のプレイで比べる | 未着手 |
| 5 | (余裕があれば)単純な機械学習モデルを試す | 未着手 |

## 実装の概要

研究用に追加したファイル(`core/src/main/java/com/shatteredpixel/shatteredpixeldungeon/`):

| ファイル | 役割 |
|---|---|
| `ResearchLog.java` | 階層を作るたび、また死亡したときに、CSV へ1行を記録する |
| `ResearchParams.java` | 設定ファイル `research_params.txt`(敵の数の倍率、自動調整のオン・オフ)を読み書きする |
| `ResearchDDA.java` | HP の変化から、次の階層の敵の数の倍率を決めるルール |

元のゲームのコードへの変更:

- `Dungeon.java`: 階層を作る前後と、死亡したときに、`ResearchLog` を呼ぶ(数行)
- `levels/RegularLevel.java`: 敵の数に倍率を掛ける(1階と、ボスの階は変えない)

元のゲーム(v4.0.2)からの変更は、次のリンクで一覧できます。
[v4.0.2 との差分](https://github.com/szkkoyu/shattered-pixel-dungeon/compare/57a4e06a4...master)

### 記録している項目(CSV、1行 = 1階層 または 1回の死亡)

階層の番号と種類、敵の数、敵の数の倍率、入ったときのHP・最大HP、
直前の階層での滞在時間・倒した敵の数・HPの増減、ゲームの識別子(シード)、死因 など。

## 動かし方(Windows / PowerShell)

JDK が必要です(動作を確認しているのは Temurin 21)。

```powershell
# 初回のみ: 設定ファイルの見本をコピーする
Copy-Item desktop\research_params.example.txt desktop\research_params.txt

# ビルドして起動する
.\gradlew desktop:debug
```

ログは `desktop\research_floor_log.csv` に追記されます。

## ライセンスとクレジット

- このリポジトリは、元のゲームと同じ **GNU General Public License v3.0** で公開しています([LICENSE.txt](LICENSE.txt))。
- 元のゲーム: [Shattered Pixel Dungeon](https://github.com/00-Evan/shattered-pixel-dungeon)(Evan Debenham)
  - さらにその元: [Pixel Dungeon](https://github.com/00-Evan/pixel-dungeon-gradle)(Watabou)
- 開発には AI アシスタント(Claude Code)を利用しています。変更は1つずつ、内容と理由を自分で確認しながら進めています。
