# Barcode.jar Spring Boot サンプル

Java のバーコード生成ライブラリ **Barcode.jar** を Spring Boot から使うサンプルです。ブラウザの画面でバーコードの種類と値を選び、PNG・JPEG・BMP・SVG の画像や PDF に出力します。

- 製品ページ：https://www.pao.ac/barcode.jar/
- ライブラリ：Maven Central `ac.pao:barcode-jar`（体験版。出力に赤い「SAMPLE」の印が付きます）

## 対応バーコード

CODE128、CODE39、CODE93、GS1-128、NW7（Codabar）、JAN-13／JAN-8、UPC-A／UPC-E、ITF、Matrix 2of5、NEC 2of5、GS1 DataBar（標準型・限定型・拡張型）、QR コード、DataMatrix、PDF417、コンビニバーコード、郵便カスタマバーコード

## 動かし方

Java 8 以降（Docker の場合は Docker だけ）が必要です。

### Docker で起動

```bash
docker compose up -d --build
```

ブラウザで http://localhost:5000/barcode を開きます。止めるときは `docker compose down` です。

### 直接起動

```bash
./gradlew bootRun        # Windows は gradlew.bat bootRun
```

ブラウザで http://localhost:5000/barcode を開きます。

## Barcode.jar の使い方

依存関係に追加するだけで使えます。

```gradle
dependencies {
    implementation 'ac.pao:barcode-jar:5.3.1'
}
```

```xml
<dependency>
  <groupId>ac.pao</groupId>
  <artifactId>barcode-jar</artifactId>
  <version>5.3.1</version>
</dependency>
```

バーコードを作る処理は `src/main/java/com/example/barcodesample/util/BarcodeUtil.java` にまとまっています。

## 体験版と製品版

Maven Central の Barcode.jar は体験版で、出力に「SAMPLE」の印が付きます（QR コードなどの 2 次元コードは、印が重なるため読み取れないことがあります）。製品版をご購入いただくと、印のない出力になります。詳しくは[製品ページ](https://www.pao.ac/barcode.jar/)をご覧ください。

## ライセンス

- このリポジトリのサンプルコード：MIT（[LICENSE](LICENSE)）
- Barcode.jar：Pao@Office の使用許諾
- PDF 出力に使っている iText 5（`com.itextpdf:itextpdf`）：AGPL。業務で使う場合は iText のライセンスをご確認ください。

---

# Barcode.jar Spring Boot sample

A Spring Boot sample for **Barcode.jar**, a barcode generation library for Java. Pick a barcode type and value in the browser and render it as PNG, JPEG, BMP, SVG or PDF.

- Product page: https://www.pao.ac/barcode.jar/
- Library: Maven Central `ac.pao:barcode-jar` (trial edition — output carries a red "SAMPLE" mark)

## Run

Requires Java 8 or later (or just Docker).

```bash
docker compose up -d --build   # with Docker
./gradlew bootRun              # without Docker (Windows: gradlew.bat bootRun)
```

Open http://localhost:5000/barcode.

## Use Barcode.jar in your project

```gradle
implementation 'ac.pao:barcode-jar:5.3.1'
```

The barcode code is in `src/main/java/com/example/barcodesample/util/BarcodeUtil.java`.

## License

- Sample code in this repository: MIT ([LICENSE](LICENSE))
- Barcode.jar: Pao@Office license (the Maven Central build is a trial edition)
- iText 5 (`com.itextpdf:itextpdf`, used for PDF output): AGPL
