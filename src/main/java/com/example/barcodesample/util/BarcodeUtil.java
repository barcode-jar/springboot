package com.example.barcodesample.util;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.awt.geom.AffineTransform;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.example.barcodesample.Controller.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pao.barcode.*;
import pao.barcode.dmatrix.DxCodeSize;

public class BarcodeUtil {

    private static final Logger logger = LoggerFactory.getLogger(BarcodeController.class);


    private  String STATIC_PATH = "src/main/resources/static/";
    private String PDF_PATH = "src/main/resources/pdf/";
    private String IMG_PATH = "src/main/resources/images/";
    //private static final String STATIC_PATH = "src/main/resources/static/";
    //private static final String PDF_PATH = "src/main/resources/pdf/";
    

    public BarcodeUtil()
    {
        
        Path app_folder = Paths.get("/app/src/main/resources");
        
        if (Files.exists(app_folder) && Files.isDirectory(app_folder)) {
            // Docker コンテナ上 (とりあえず)
            STATIC_PATH = "/app/" + STATIC_PATH;
            PDF_PATH = "/app/" + PDF_PATH;
            IMG_PATH = "/app/" + IMG_PATH;
        }
    }

    public BufferedImage generateBarcodeImage(BarcodeForm form) throws Exception {
        return generateBarcodeImage(null, form, false, null);
    }
    public BufferedImage generateBarcodeImage(BarcodeForm form, Color backColor) throws Exception {
        return generateBarcodeImage(null, form, false, backColor);
    }
    public BufferedImage generateBarcodeImage(String fileName, BarcodeForm form) throws Exception {
        return generateBarcodeImage(fileName, form, false, null);
    }
    public BufferedImage generateBarcodeImage(String fileName, BarcodeForm form, boolean flg_x0y0) throws Exception {
        return generateBarcodeImage(fileName, form, flg_x0y0, null);
    }
    public BufferedImage generateBarcodeImage(String fileName, BarcodeForm form, Color backColor) throws Exception {
        return generateBarcodeImage(fileName, form, false, backColor);
    }
    // バーコードイメージ生成
    // fileName - Barcode.jarのコンストラクタ引数が異なる 
    //   "ファイル名":画像ファイル生成
    //   null:画像データ生成(Graphics2D仕様)
    // flg_x0y0 - PDFにバーコード画像を埋め込む場合 true (PDF側で座標をセットするため) 
    public BufferedImage generateBarcodeImage(String fileName, BarcodeForm form, boolean flg_x0y0, Color backColor) throws Exception {
    
        // イメージ / Graphics2D
        BufferedImage image = null;
        Graphics2D g2d = null;

        // Graphics2Dで描画してBase64でエンコードした画像データを渡す場合
        if(fileName == null) {

            // イメージ / Graphics2D 生成  (画像キャンパスは800x800にしておきます。これを超えると表示されません。)
            int w_h = 512;
            if(!form.getKind().equals("yubin")) {
                image = new BufferedImage(w_h, w_h, BufferedImage.TYPE_INT_RGB);
            } else {
                // 郵便カスタマバーコードは横幅を4倍
                image = new BufferedImage(w_h * 4, w_h, BufferedImage.TYPE_INT_RGB);
            }

            g2d = image.createGraphics();

            g2d.setColor(backColor);
            if(!form.getKind().equals("yubin")) {
                g2d.fillRect(0, 0, w_h, w_h);
            } else {
                g2d.fillRect(0, 0, w_h * 4, w_h);
            }
        
        }
        

        // バーコード描画 - ここにバーコード描画ロジック全てが詰まっています。
        drawBarcode(fileName, g2d, form, backColor, flg_x0y0);

        if(fileName != null) {
            // 画像ファイルロードが遅いので1秒待機する
            //のは やめて、/images を使用するようにした。(staticの下は遅い)
            //Thread.sleep(1000);
            return null;
        } else if(g2d != null){
            g2d.dispose();
            return image;
        }

        return null;
 
     }

     // バーコードイメージを PDFに埋め込む
    public void generateBarcodePdfByImageFile(BarcodeForm form) throws Exception {

        //文書オブジェクトを生成
		Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
		
		//出力先(アウトプットストリーム)の生成
		FileOutputStream fos = new FileOutputStream(PDF_PATH + "barcode.pdf");
		
		//アウトプットストリームをPDFWriterに設定
		PdfWriter pdfwriter = PdfWriter.getInstance(doc, fos);
		
		//文章オブジェクト オープン
		doc.open();

        //Imageインスタンスの作成
        // 速度アップのため static は、リロードが遅いので使わない。
		//Image img = Image.getInstance(STATIC_PATH + "barcode_pdf.png");
        URL  imgURL = null;
        imgURL = new File(IMG_PATH + "barcode_pdf.png").toURI().toURL();
        Image img = Image.getInstance(imgURL);

        int h = (int)img.getHeight();
        int w = (int)img.getWidth();

        

        //表示位置の設定(左下がY座標原点のためY座標反転してまず)
		img.setAbsolutePosition(form.getX() , doc.getPageSize().getHeight() - form.getY() - h);
		
		//表示サイズの設定
		img.scaleAbsolute(w, h);
		
		//PdfContentByteの取得
		PdfContentByte pdfContentByte = pdfwriter.getDirectContent();
		
		//文章に画像を追加する
		pdfContentByte.addImage(img);

        //文章オブジェクト クローズ
		doc.close();
		
		//PDFWriter クローズ
		pdfwriter.close();
		
    }

    // Graphics2D で PDFにバーコードを描画
    public void generateBarcodePdfByGraphics(BarcodeForm form) throws Exception {
 
        float pdfWidth = PageSize.A4.getHeight();
        float pdfHeight = PageSize.A4.getWidth();

        //文書オブジェクトを生成
        Document doc = new Document(new Rectangle(pdfWidth, pdfHeight));
        
		//アウトプットストリームをPDFWriterに設定
        PdfWriter pdfwriter = PdfWriter.getInstance(
            doc, new FileOutputStream(PDF_PATH + "barcode.pdf"));
		
		//文章オブジェクト オープン
		doc.open();

        PdfContentByte canvas = pdfwriter.getDirectContent();
        PdfTemplate template = canvas.createTemplate(pdfWidth, pdfHeight);
        Graphics2D g2d = new PdfGraphics2D(template, pdfWidth, pdfHeight);

        // バーコード描画 - ここにバーコード描画ロジック全てが詰まっています。
        drawBarcode(g2d, form, Color.WHITE);

        g2d.dispose();
        canvas.addTemplate(template, 0, 0);
        doc.close();
		pdfwriter.close();
    }

    public String toBase64String(BufferedImage image, String type) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, type, baos);
        byte[] bytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 新しいストリーム方式でバーコードを生成し、Base64文字列を返す
     * ストリームコンストラクタとgetImageBase64()メソッドを使用
     * Graphics2Dを経由せず、直接ストリームに出力する効率的な方式
     * @param form バーコードフォーム
     * @param imgFormat 画像フォーマット ("png", "jpeg", "bmp"など)
     * @return Base64エンコードされた画像文字列
     */
    public String generateBarcodeBase64(BarcodeForm form, String imgFormat) throws Exception {
        // バーコードを最小サイズで生成
        String minimalBarcodeBase64 = drawBarcodeToStream(form, imgFormat);

        // マージンを含めない場合（デフォルト）は、バーコードのみ返す
        // HTML側でCSS positionを使ってX/Y座標に配置される
        if (!form.isIncludeMargin()) {
            return minimalBarcodeBase64;
        }

        // 以下、マージンを含める場合の処理
        // X/Y座標を取得
        int x = form.getX();
        int y = form.getY();

        // X/Yが両方0の場合はそのまま返す（最小サイズのまま）
        if (x == 0 && y == 0) {
            return minimalBarcodeBase64;
        }

        // バーコードをX/Y座標に配置した大きなキャンバスを生成
        Color backColor = form.isTransparentBackground() ? null : parseHexColor(form.getBackColor());
        if (!form.isTransparentBackground() && backColor == null) backColor = Color.WHITE;

        return placeBarcodeOnCanvas(minimalBarcodeBase64, x, y, imgFormat, backColor, form.getKind());
    }

    /**
     * バーコード画像を指定座標に配置したキャンバスを生成
     * キャンバスサイズは必要最小限（バーコード＋座標オフセット＋マージン）
     * 背景色はバーコード領域のみに適用され、マージン部分は透明（PNG）または白（JPEG/BMP）
     */
    private String placeBarcodeOnCanvas(String barcodeBase64, int x, int y, String imgFormat, Color backColor, String kind) throws Exception {
        if (barcodeBase64 == null || barcodeBase64.isEmpty()) {
            return barcodeBase64;
        }

        // Base64をデコードしてBufferedImageに変換
        byte[] imageBytes = Base64.getDecoder().decode(barcodeBase64);
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(imageBytes);
        BufferedImage barcodeImg = ImageIO.read(bais);

        if (barcodeImg == null) {
            return barcodeBase64;
        }

        // キャンバスサイズを決定（バーコード+座標オフセット+小さなマージン）
        int barcodeWidth = barcodeImg.getWidth();
        int barcodeHeight = barcodeImg.getHeight();
        int margin = 2; // 最小マージン
        int canvasWidth = x + barcodeWidth + margin;
        int canvasHeight = y + barcodeHeight + margin;

        // 透過をサポートしない形式かどうか判定
        String lowerFormat = imgFormat.toLowerCase();
        boolean supportsTransparency = lowerFormat.equals("png") || lowerFormat.equals("gif");

        // キャンバスを生成（透過サポートならARGB、そうでなければRGB）
        int imageType = supportsTransparency ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB;
        BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight, imageType);
        Graphics2D g2d = canvas.createGraphics();

        // 透過をサポートしない形式の場合、マージン部分を白で塗りつぶし
        if (!supportsTransparency) {
            g2d.setColor(Color.WHITE);
            g2d.fillRect(0, 0, canvasWidth, canvasHeight);
        }

        // バーコード領域のみ背景色を塗りつぶし（バーコード画像に既に背景が含まれているので通常は不要）
        // バーコードライブラリが背景色を含めて画像を生成するため、ここでは追加の塗りつぶしは行わない

        // バーコードをX/Y座標に描画（バーコード画像には既に背景色が含まれている）
        g2d.drawImage(barcodeImg, x, y, null);
        g2d.dispose();

        // キャンバスをBase64に変換
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String format = imgFormat.equalsIgnoreCase("jpg") ? "jpeg" : imgFormat;
        ImageIO.write(canvas, format, baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /**
     * ストリームベースでバーコードを描画し、Base64文字列を返す
     * （バーコードは最小サイズで生成され、X/Y座標は無視される）
     */
    private String drawBarcodeToStream(BarcodeForm form, String imgFormat) throws Exception {
        // バーコード生成時パラメータ
        String code = form.getCode();
        String kind = form.getKind();
        // X/Yは無視（バーコードライブラリがストリームモードで0にリセットするため）
        int x = 0;
        int y = 0;
        int width = form.getWidth();
        int height = form.getHeight();
        String drawType = form.getDrawType();
        boolean dispStartStopCode = form.getDispStartStopCode();
        String codeSet128 = form.getCodeSet128();
        String typeDatabar14 = form.getTypeDatabar14();
        String typeDatabarExpanded = form.getTypeDatabarExpanded();
        int fontSize = form.getFontSize();

        // バーコードの色と背景色
        Color foreColor = parseHexColor(form.getForeColor());
        if (foreColor == null) foreColor = Color.BLACK;
        Color backColor = form.isTransparentBackground() ? null : parseHexColor(form.getBackColor());
        if (!form.isTransparentBackground() && backColor == null) backColor = Color.WHITE;

        // 《》で括られた文字列を制御コードに変換(ESC/STX/ETX/CR/LF...等)
        String escArr[] = {"NUL", "SOH", "STX", "ETX", "EOT", "ENQ", "ACK", "BEL", "BS", "HT",
        "LF", "VT", "FF", "CR", "SO", "SI", "DLE", "DC1", "DC2", "DC3", "DC4", "NAK",
        "SYN", "ETB", "CAN", "EM", "SUB", "ESC", "FS", "GS", "RS", "US"};

        Pattern pattern = Pattern.compile("《(.+?)》");
        Matcher matcher = pattern.matcher(code);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String sEscKakko = matcher.group(0);
            String sEscVal = sEscKakko.replaceAll("《|》", "");
            int index = getIndex(escArr, sEscVal);
            char cEsc = (char) index;
            matcher.appendReplacement(sb, Character.toString(cEsc));
        }
        matcher.appendTail(sb);
        code = sb.toString();

        // 一次元バーコードの場合
        if(!(kind.equals("QR")
        || kind.equals("DataMatrix")
        || kind.equals("PDF417")
        || kind.equals("yubin")
        || kind.equals("conv")
        ))
        {
            IBarCode bar = null;
            switch(kind)
            {
                case "code128" :
                    // ストリームコンストラクタを使用
                    bar = new Code128(imgFormat, true);
                    if(codeSet128 == "CODE-A")
                        ((Code128)bar).setCodeABC(CodeSet128.CODE_A);
                    if(codeSet128 == "CODE-B")
                        ((Code128)bar).setCodeABC(CodeSet128.CODE_B);
                    if(codeSet128 == "CODE-C")
                        ((Code128)bar).setCodeABC(CodeSet128.CODE_C);
                    break;
                case "code39" :
                    bar = new Code39(imgFormat, true);
                    bar.setDispStartStopCode(dispStartStopCode);
                    break;
                case "code93" :
                    bar = new Code93(imgFormat, true);
                    break;
                case "nw7" :
                    bar = new NW7(imgFormat, true);
                    bar.setDispStartStopCode(dispStartStopCode);
                    break;
                case "gs1-128" :
                    bar = new GS1_128(imgFormat, true);
                    break;
                case "gs1-Databar-14" :
                    bar = new Gs1Databar14(imgFormat, true);
                    if(typeDatabar14.equals("Omni-directional"))
                        ((Gs1Databar14)bar).setSymbolType(Gs1Databar14.DatabarType.OMNIDIRECTIONAL);
                    else if(typeDatabar14.equals("Stacked"))
                        ((Gs1Databar14)bar).setSymbolType(Gs1Databar14.DatabarType.STACKED);
                    else
                        ((Gs1Databar14)bar).setSymbolType(Gs1Databar14.DatabarType.STACKED_OMNIDIRECTIONAL);
                    break;
                case "gs1-Databar-Limited" :
                    bar = new Gs1DatabarLimited(imgFormat, true);
                    break;
                case "gs1-Databar-Expanded" :
                    bar = new Gs1DatabarExpanded(imgFormat, true);
                    if(typeDatabarExpanded.equals("UnStacked"))
                        ((Gs1DatabarExpanded)bar).setSymbolType(Gs1DatabarExpanded.DatabarType.UNSTACKED);
                    else
                        ((Gs1DatabarExpanded)bar).setSymbolType(Gs1DatabarExpanded.DatabarType.STACKED);
                    break;
                case "jan13" :
                    bar = new Jan13(imgFormat, true);
                    bar.setTextKintou(false);
                    break;
                case "jan8" :
                    bar = new Jan8(imgFormat, true);
                    bar.setTextKintou(false);
                    break;
                case "UPC-A" :
                    bar = new UpcA(imgFormat, true);
                    bar.setTextKintou(false);
                    break;
                case "UPC-E" :
                    bar = new UpcE(imgFormat, true);
                    bar.setTextKintou(false);
                    break;
                case "itf" :
                    bar = new ITF(imgFormat, true);
                    break;
                case "matrix2of5" :
                    bar = new Matrix2of5(imgFormat, true);
                    break;
                case "nec2of5" :
                    bar = new NEC2of5(imgFormat, true);
                    break;
                default :
                    bar = new Code128(imgFormat, true);
                    code = kind;
                    if(code.equals("")) code = "ZZZZZ";
                    break;
            }

            // 一次元バーコード共通処理
            bar.setTextWrite(true);
            if(!kind.equals("jan13") && !kind.equals("jan8") && !kind.equals("UPC-A") && !kind.equals("UPC-E"))
                bar.setTextKintou(true);

            // 色設定
            bar.setForeColor(foreColor);
            bar.setBackColor(backColor);

            // フォントサイズ設定
            if(fontSize > 0) {
                Font currentFont = bar.getTextFont();
                bar.setTextFont(new Font(currentFont.getName(), currentFont.getStyle(), fontSize));
            }

            // バーコード画像描画
            if(drawType.equals("draw"))
                bar.draw(code, x, y, width, height);
            else if(drawType.equals("drawDirect"))
                bar.drawDirect(code, x, y, width, height);
            else if(drawType.equals("drawDelicate"))
                bar.drawDelicate(code, x, y, width, height);

            // getImageBase64()で直接Base64文字列を取得
            return bar.getImageBase64();
        }
        else
        {
            // 二次元バーコードの場合
            if(kind.equals("QR"))
            {
                QRCode bar = new QRCode(imgFormat, true);
                bar.setVersion(form.getQrVersion());
                bar.setErrorCorrect(form.getQrErrorCorrectionLevel());
                bar.setEncodeMode(form.getEncode());
                bar.setForeColor(foreColor);
                bar.setBackColor(backColor);

                if(drawType.equals("draw"))
                    bar.draw(code, x, y, width, height);
                else if(drawType.equals("drawDirect"))
                    bar.drawDirect(code, x, y, width);
                else if(drawType.equals("drawDelicate"))
                    bar.drawDelicate(code, x, y, width);

                return bar.getImageBase64();
            }
            else if(kind.equals("DataMatrix"))
            {
                DataMatrix bar = new DataMatrix(imgFormat, true);
                int codeSize = form.getDataMatrixCodeSize();
                DxCodeSize dxCodeSize = DxCodeSize.DxSzAuto;
                dxCodeSize = dxCodeSize.fromIntVal(codeSize);
                bar.setCodeSize(dxCodeSize);
                bar.setStringEncoding(form.getEncode());
                bar.setForeColor(foreColor);
                bar.setBackColor(backColor);

                if(drawType.equals("draw"))
                    bar.draw(code, x, y, width, height);
                else if(drawType.equals("drawDirect"))
                    bar.drawDirect(code, x, y, width, height);
                else if(drawType.equals("drawDelicate"))
                    bar.drawDelicate(code, x, y, width);

                return bar.getImageBase64();
            }
            else if(kind.equals("PDF417"))
            {
                Pdf417 bar = new Pdf417(imgFormat, true);
                String sErrCorrectLevel = form.getPdf417ErrorCorrectionLevel();
                if(sErrCorrectLevel.equals("AUTO")) {
                    bar.setUseAutoErrorLevel(true);
                } else {
                    int errCorrectLevel = Integer.parseInt(sErrCorrectLevel);
                    bar.setUseAutoErrorLevel(false);
                    bar.setErrorLevel(errCorrectLevel);
                }
                bar.setAspectRatio(form.getPdf417AspectRatio());
                String sizeKind = form.getPdf417SizeKind();
                if(sizeKind.equals("AUTO")) {
                    bar.setSizeKind(Pdf417.SIZE_KIND.AUTO);
                } else if(sizeKind.equals("COL_NUM")) {
                    bar.setSizeKind(Pdf417.SIZE_KIND.COLUMNS);
                } else if(sizeKind.equals("ROW_NUM")) {
                    bar.setSizeKind(Pdf417.SIZE_KIND.ROWS);
                } else if(sizeKind.equals("COL_NUM_AND_ROW_NUM")) {
                    bar.setSizeKind(Pdf417.SIZE_KIND.COLUMNS_AND_ROWS);
                }
                bar.setStringEncoding(form.getEncode());
                bar.setForeColor(foreColor);
                bar.setBackColor(backColor);

                if(drawType.equals("draw"))
                    bar.draw(code, x, y, width, height);
                else if(drawType.equals("drawDirect"))
                    bar.drawDirect(code, x, y, width, height);
                else if(drawType.equals("drawDelicate"))
                    bar.drawDelicate(code, x, y, width);

                return bar.getImageBase64();
            }
            else if(kind.equals("yubin"))
            {
                YubinCustomer bar = new YubinCustomer(imgFormat, true);
                bar.setForeColor(foreColor);
                bar.setBackColor(backColor);
                bar.draw(code, x, y, height);
                return bar.getImageBase64();
            }
            else if(kind.equals("conv"))
            {
                GS1_128 bar = new GS1_128(imgFormat, true);
                bar.setTextWrite(true);
                bar.setTextKintou(true);
                bar.setDPI(600);
                bar.setForeColor(foreColor);
                bar.setBackColor(backColor);

                // フォントサイズ設定
                if(fontSize > 0) {
                    bar.setTextFont(new Font(bar.getTextFont().getName(), bar.getTextFont().getStyle(), fontSize));
                }

                if(drawType.equals("draw"))
                    bar.drawConvenience(code, x, y, width, height, GraphicsUnit.PIXEL, 600, false);
                else if(drawType.equals("drawDirect"))
                    bar.drawConvenienceDirect(code, x, y, width, height, GraphicsUnit.PIXEL, 600, false);
                else if(drawType.equals("drawDelicate"))
                    bar.drawConvenienceDelicate(code, x, y, width, height);

                return bar.getImageBase64();
            }
        }
        return "";
    }

    private void drawBarcode(Graphics2D g2d, BarcodeForm form, Color backColor)  throws Exception
    {
        drawBarcode(null, g2d, form, backColor, false);
    }
    private void drawBarcode(String fileName, Graphics2D g2d, BarcodeForm form, Color backColor, boolean flg_x0y0)  throws Exception
    {
        // バーコード生成時パラメータ
        String code = form.getCode();
        String kind = form.getKind();
        int x = form.getX();
        int y = form.getY();
        // PDFにバーコード画像を埋め込む場合、PDF側で座標をセットするため
        if(flg_x0y0) {x = 0; y = 0;}
        int width = form.getWidth();
        int height = form.getHeight();
        String drawType = form.getDrawType();
        boolean dispStartStopCode = form.getDispStartStopCode();
        String codeSet128 = form.getCodeSet128();
        String typeDatabar14 = form.getTypeDatabar14();
        String typeDatabarExpanded = form.getTypeDatabarExpanded();
        boolean pdf = form.isPdf();

        // バーコードの色と背景色をフォームから取得
        Color barForeColor = parseHexColor(form.getForeColor());
        if (barForeColor == null) barForeColor = Color.BLACK;

        // JPEG/BMP保存時は透過不可のため、白背景を強制
        boolean forceWhiteBackground = false;
        if (fileName != null) {
            String lowerFileName = fileName.toLowerCase();
            if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg") || lowerFileName.endsWith(".bmp")) {
                forceWhiteBackground = true;
            }
        }

        Color barBackColor;
        if (forceWhiteBackground) {
            // JPEG/BMPは透過不可なので白背景
            barBackColor = Color.WHITE;
        } else if (form.isTransparentBackground()) {
            // 透過チェックONの場合はnull（透明）
            barBackColor = null;
        } else {
            // 背景色指定がある場合はそれを使用
            barBackColor = parseHexColor(form.getBackColor());
            if (barBackColor == null) {
                barBackColor = (backColor != null) ? backColor : Color.WHITE;
            }
        }

        URL  imgURL = null;
        String imgPath = "";
        // 速度アップのため static は、リロードが遅いので使わない。
        //if(fileName != null) imgPath = STATIC_PATH + fileName;
        if (fileName != null) {
            imgURL = new File(IMG_PATH + fileName).toURI().toURL();
        }
        if (imgURL != null) {
            File file = new File(imgURL.getPath());
            imgPath = file.getPath();
            System.out.println("デバッグ情報: " + imgPath);
            logger.info("デバッグ情報: " + imgPath);
        }


        // 《》で括られた文字列を制御コードに変換(ESC/STX/ETX/CR/LF...等)・・・今のところCODE128のCODE-A用
        String escArr[] = {"NUL", "SOH", "STX", "ETX", "EOT", "ENQ", "ACK", "BEL", "BS", "HT",
        "LF", "VT", "FF", "CR", "SO", "SI", "DLE", "DC1", "DC2", "DC3", "DC4", "NAK",
        "SYN", "ETB", "CAN", "EM", "SUB", "ESC", "FS", "GS", "RS", "US"};

        Pattern pattern = Pattern.compile("《(.+?)》");
        Matcher matcher = pattern.matcher(code);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String sEscKakko = matcher.group(0);
            String sEscVal = sEscKakko.replaceAll("《|》", "");

            int index = getIndex(escArr, sEscVal);

            char cEsc = (char) index;
            matcher.appendReplacement(sb, Character.toString(cEsc));
        }
        matcher.appendTail(sb);
        code = sb.toString();
        

        //一次元バーコードの場合、IBarCode で一部まとめて処理します。
        if(!(kind.equals("QR") 
        || kind.equals("DataMatrix") 
        || kind.equals("PDF417")
        || kind.equals("yubin")
        || kind.equals("conv")
        ))
        {
            IBarCode bar = null;
            switch(kind)
            {
                case "code128" :
                    bar = (g2d == null)?new Code128(imgPath) : new Code128(g2d);
                    if(codeSet128 == "CODE-A")
                        ((Code128)bar).setCodeABC(CodeSet128.CODE_A);
                    if(codeSet128 == "CODE-B")
                        ((Code128)bar).setCodeABC(CodeSet128.CODE_B);
                    if(codeSet128 == "CODE-C")
                        ((Code128)bar).setCodeABC(CodeSet128.CODE_C);
                    break;
                case "code39" :
                    bar = (g2d == null)?new Code39(imgPath) : new Code39(g2d);
                    bar.setDispStartStopCode(dispStartStopCode);
                    break;
                case "code93" :
                    bar = (g2d == null)?new Code93(imgPath) : new Code93(g2d);
                    break;
                case "nw7" :
                    bar = (g2d == null)?new NW7(imgPath) : new NW7(g2d);
                    bar.setDispStartStopCode(dispStartStopCode);
                    break;
                case "gs1-128" :
                    bar = (g2d == null)?new GS1_128(imgPath) : new GS1_128(g2d);
                    break;
                case "gs1-Databar-14" :
                    bar = (g2d == null)?new Gs1Databar14(imgPath) : new Gs1Databar14(g2d);
                    if(typeDatabar14.equals("Omni-directional"))
                        ((Gs1Databar14)bar).setSymbolType(Gs1Databar14.DatabarType.OMNIDIRECTIONAL);
                    else if(typeDatabar14.equals("Stacked"))
                        ((Gs1Databar14)bar).setSymbolType(Gs1Databar14.DatabarType.STACKED);
                    else
                        ((Gs1Databar14)bar).setSymbolType(Gs1Databar14.DatabarType.STACKED_OMNIDIRECTIONAL);
                    break;
                case "gs1-Databar-Limited" :
                    bar = (g2d == null)?new Gs1DatabarLimited(imgPath) : new Gs1DatabarLimited(g2d);
                    break;
                case "gs1-Databar-Expanded" :
                    bar = (g2d == null)?new Gs1DatabarExpanded(imgPath) : new Gs1DatabarExpanded(g2d);
                    if(typeDatabarExpanded.equals("UnStacked"))
                        ((Gs1DatabarExpanded)bar).setSymbolType(Gs1DatabarExpanded.DatabarType.UNSTACKED);
                    else
                        ((Gs1DatabarExpanded)bar).setSymbolType(Gs1DatabarExpanded.DatabarType.STACKED);
                    break;
                case "jan13" :
                    bar = (g2d == null)?new Jan13(imgPath) : new Jan13(g2d);
                    bar.setTextKintou(false);
                    break;
                case "jan8" :
                    bar = (g2d == null)?new Jan8(imgPath) : new Jan8(g2d);
                    bar.setTextKintou(false);
                    break;
                case "UPC-A" :
                    bar = (g2d == null)?new UpcA(imgPath) : new UpcA(g2d);
                    bar.setTextKintou(false);
                    break;
                case "UPC-E" :
                    bar = (g2d == null)?new UpcE(imgPath) : new UpcE(g2d);
                    bar.setTextKintou(false);
                    break;
                case "itf" :
                    bar = (g2d == null)?new ITF(imgPath) : new ITF(g2d);
                    break;
                case "matrix2of5" :
                    bar = (g2d == null)?new Matrix2of5(imgPath) : new Matrix2of5(g2d);
                    break;
                case "nec2of5" :
                    bar = (g2d == null)?new NEC2of5(imgPath) : new NEC2of5(g2d);
                    break;
                default :
                    bar = new Code128(imgPath);
                    bar = (g2d == null)?new Code128(imgPath) : new Code128(g2d);
                    code = kind;
                    if(code.equals("")) code = "ZZZZZ";
                    break;
            }


            // *** 一次元バーコード共通処理 ***

            // バーコードの色と背景色を設定
            bar.setForeColor(barForeColor);
            bar.setBackColor(barBackColor);

            //添え字のを均等割り付けで描画(Janコード以外)
            bar.setTextWrite(true);
            if(!kind.equals("jan13") && !kind.equals("jan8") && !kind.equals("UPC-A") && !kind.equals("UPC-E"))
                bar.setTextKintou(true);

            // フォントサイズ設定
            int fontSize = form.getFontSize();
            if(fontSize > 0) {
                Font currentFont = bar.getTextFont();
                bar.setTextFont(new Font(currentFont.getName(), currentFont.getStyle(), fontSize));
            }

            // バーコード画像描画
            if(drawType.equals("draw"))
                bar.draw(code, x, y, width, height);
            else if(drawType.equals("drawDirect"))
                bar.drawDirect(code, x, y, width, height);
            else if(drawType.equals("drawDelicate"))
                bar.drawDelicate(code, x, y, width, height);

        }
        else
        {
            // 二次元バーコードの場合

            if(kind.equals("QR"))
            {
                // QR描画オブジェクト生成
                QRCode bar = (g2d == null)?new QRCode(imgPath) : new QRCode(g2d);
                // バーコードの色と背景色を設定
                bar.setForeColor(barForeColor);
                bar.setBackColor(barBackColor);

                // バージョン・エラー訂正レベル・文字エンコード
                bar.setVersion(form.getQrVersion());
                bar.setErrorCorrect(form.getQrErrorCorrectionLevel());
                bar.setEncodeMode(form.getEncode());

                // QRコード描画
                if(drawType.equals("draw"))
                    bar.draw(code, x, y, width, height);
                else if(drawType.equals("drawDirect"))
                    bar.drawDirect(code, x, y, width);
                else if(drawType.equals("drawDelicate"))
                    bar.drawDelicate(code, x, y, width);
            }
            else if(kind.equals("DataMatrix"))
            {
                DataMatrix bar = (g2d == null)?new DataMatrix(imgPath) : new DataMatrix(g2d);
                // バーコードの色と背景色を設定
                bar.setForeColor(barForeColor);
                bar.setBackColor(barBackColor);

                // コードサイズ
                // 選択された数値から、enum DxCodeSize へ変換
                int codeSize = form.getDataMatrixCodeSize();
                DxCodeSize dxCodeSize = DxCodeSize.DxSzAuto;
                dxCodeSize = dxCodeSize.fromIntVal(codeSize);
                // コードサイズ設定
                bar.setCodeSize(dxCodeSize);

                // バージョン・エラー訂正レベル・文字エンコード
                bar.setStringEncoding(form.getEncode());

                // DataMatrix 描画
                if(drawType.equals("draw"))
                    bar.draw(code, x, y, width, height);
                else if(drawType.equals("drawDirect"))
                    bar.drawDirect(code, x, y, width, height);
                else if(drawType.equals("drawDelicate"))
                    bar.drawDelicate(code, x, y, width);
            }
            else if(kind.equals("PDF417"))
            {
                Pdf417 bar = (g2d == null)?new Pdf417(imgPath) : new Pdf417(g2d);
                // バーコードの色と背景色を設定
                bar.setForeColor(barForeColor);
                bar.setBackColor(barBackColor);

                // 誤り訂正レベル
                String sErrCorrectLevel = form.getPdf417ErrorCorrectionLevel();
                if(sErrCorrectLevel.equals("AUTO")) {
                    bar.setUseAutoErrorLevel(true);
                } else {
                    int errCorrectLevel = Integer.parseInt(sErrCorrectLevel);
                    bar.setUseAutoErrorLevel(false);
                    bar.setErrorLevel(errCorrectLevel);
                }

                // アクセプト比(縦横比)
                bar.setAspectRatio(form.getPdf417AspectRatio());

                // サイズ種別
                String sizeKind = form.getPdf417SizeKind();
                if(sizeKind.equals("AUTO")) {
                    bar.setSizeKind(Pdf417.SIZE_KIND.AUTO);
                } else if(sizeKind.equals("COL_NUM")) {
                    bar.setSizeKind(Pdf417.SIZE_KIND.COLUMNS);
                } else if(sizeKind.equals("ROW_NUM")) {
                    bar.setSizeKind(Pdf417.SIZE_KIND.ROWS);
                } else if(sizeKind.equals("COL_NUM_AND_ROW_NUM")) {
                    bar.setSizeKind(Pdf417.SIZE_KIND.COLUMNS_AND_ROWS);
                }

                // バージョン・エラー訂正レベル・文字エンコード
                bar.setStringEncoding(form.getEncode());

                // PDF417 描画
                if(drawType.equals("draw"))
                    bar.draw(code, x, y, width, height);
                else if(drawType.equals("drawDirect"))
                    bar.drawDirect(code, x, y, width, height);
                else if(drawType.equals("drawDelicate"))
                    bar.drawDelicate(code, x, y, width);
            }
            else if(kind.equals("yubin"))
            {
                YubinCustomer bar = (g2d == null)?new YubinCustomer(imgPath) : new YubinCustomer(g2d);
                // バーコードの色と背景色を設定
                bar.setForeColor(barForeColor);
                bar.setBackColor(barBackColor);

                // 郵便カスタマバーコードを描画（heightで高さ指定）
                bar.draw(code, x, y, height);

            }
            else if(kind.equals("conv"))
            {
                GS1_128 bar = (g2d == null)?new GS1_128(imgPath) : new GS1_128(g2d);
                bar.setTextWrite(true);
                bar.setTextKintou(true);
                bar.setDPI(600);
                // バーコードの色と背景色を設定
                bar.setForeColor(barForeColor);
                bar.setBackColor(barBackColor);

                // フォントサイズ設定
                int fontSize = form.getFontSize();
                if(fontSize > 0) {
                    bar.setTextFont(new Font(bar.getTextFont().getName(), bar.getTextFont().getStyle(), fontSize));
                }

                // Draw
                if(drawType.equals("draw"))
                    bar.drawConvenience(code, x, y, width, height, GraphicsUnit.PIXEL, 600, false);
                else if(drawType.equals("drawDirect"))
                    bar.drawConvenienceDirect(code, x, y, width, height, GraphicsUnit.PIXEL, 600, false);
                else if(drawType.equals("drawDelicate"))
                    bar.drawConvenienceDelicate(code, x, y, width, height);

            }

        }
        
    }

    private int getIndex(String[] array, String input) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(input)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 16進数カラー文字列をjava.awt.Colorに変換
     * @param hexColor 16進数RGB形式 (例: "000000", "FF0000", "#FFFFFF")
     * @return Color オブジェクト、変換失敗時はnull
     */
    public static Color parseHexColor(String hexColor) {
        if (hexColor == null || hexColor.isEmpty()) {
            return null;
        }
        // #がある場合は除去
        if (hexColor.startsWith("#")) {
            hexColor = hexColor.substring(1);
        }
        try {
            int rgb = Integer.parseInt(hexColor, 16);
            return new Color(rgb);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // SVG文字列を生成
    public String generateSVGString(BarcodeForm form) throws Exception {
        String code = form.getCode();
        String kind = form.getKind();
        float x = form.getX();
        float y = form.getY();
        float width = form.getWidth();
        float height = form.getHeight();

        // 《》で括られた文字列を制御コードに変換(ESC/STX/ETX/CR/LF...等)
        String escArr[] = {"NUL", "SOH", "STX", "ETX", "EOT", "ENQ", "ACK", "BEL", "BS", "HT",
        "LF", "VT", "FF", "CR", "SO", "SI", "DLE", "DC1", "DC2", "DC3", "DC4", "NAK",
        "SYN", "ETB", "CAN", "EM", "SUB", "ESC", "FS", "GS", "RS", "US"};

        Pattern pattern = Pattern.compile("《(.+?)》");
        Matcher matcher = pattern.matcher(code);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String sEscKakko = matcher.group(0);
            String sEscVal = sEscKakko.replaceAll("《|》", "");
            int index = getIndex(escArr, sEscVal);
            char cEsc = (char) index;
            matcher.appendReplacement(sb, Character.toString(cEsc));
        }
        matcher.appendTail(sb);
        code = sb.toString();

        String svgString = "";
        logger.info("generateSVGString request kind={} code={}", kind, code);

        // フォントサイズ
        int fontSize = form.getFontSize();

        // GS1データバー タイプ
        String typeDatabar14 = form.getTypeDatabar14();
        String typeDatabarExpanded = form.getTypeDatabarExpanded();

        // バーコードの色と背景色
        Color foreColor = parseHexColor(form.getForeColor());
        if (foreColor == null) foreColor = Color.BLACK;
        // SVGは透明対応しないため、背景色はnullでなくwhiteをデフォルトに
        Color backColor = parseHexColor(form.getBackColor());
        if (backColor == null) backColor = Color.WHITE;

        switch(kind) {
            case "code128":
                Code128 code128 = new Code128();
                code128.setForeColor(foreColor);
                code128.setBackColor(backColor);
                if(fontSize > 0) code128.setTextFont(new Font(code128.getTextFont().getName(), code128.getTextFont().getStyle(), fontSize));
                svgString = code128.writeSVGToString(code, x, y, width, height);
                break;
            case "code39":
                Code39 code39 = new Code39();
                code39.setForeColor(foreColor);
                code39.setBackColor(backColor);
                code39.setDispStartStopCode(form.getDispStartStopCode());
                if(fontSize > 0) code39.setTextFont(new Font(code39.getTextFont().getName(), code39.getTextFont().getStyle(), fontSize));
                svgString = code39.writeSVGToString(code, x, y, width, height);
                break;
            case "code93":
                Code93 code93 = new Code93();
                code93.setForeColor(foreColor);
                code93.setBackColor(backColor);
                if(fontSize > 0) code93.setTextFont(new Font(code93.getTextFont().getName(), code93.getTextFont().getStyle(), fontSize));
                svgString = code93.writeSVGToString(code, x, y, width, height);
                break;
            case "nw7":
                NW7 nw7 = new NW7();
                nw7.setForeColor(foreColor);
                nw7.setBackColor(backColor);
                nw7.setDispStartStopCode(form.getDispStartStopCode());
                if(fontSize > 0) nw7.setTextFont(new Font(nw7.getTextFont().getName(), nw7.getTextFont().getStyle(), fontSize));
                svgString = nw7.writeSVGToString(code, x, y, width, height);
                break;
            case "gs1-128":
                GS1_128 gs1_128 = new GS1_128();
                gs1_128.setForeColor(foreColor);
                gs1_128.setBackColor(backColor);
                if(fontSize > 0) gs1_128.setTextFont(new Font(gs1_128.getTextFont().getName(), gs1_128.getTextFont().getStyle(), fontSize));
                svgString = gs1_128.writeSVGToString(code, x, y, width, height);
                break;
            case "itf":
                ITF itf = new ITF();
                itf.setForeColor(foreColor);
                itf.setBackColor(backColor);
                if(fontSize > 0) itf.setTextFont(new Font(itf.getTextFont().getName(), itf.getTextFont().getStyle(), fontSize));
                svgString = itf.writeSVGToString(code, x, y, width, height);
                break;
            case "matrix2of5":
                Matrix2of5 matrix2of5 = new Matrix2of5();
                matrix2of5.setForeColor(foreColor);
                matrix2of5.setBackColor(backColor);
                if(fontSize > 0) matrix2of5.setTextFont(new Font(matrix2of5.getTextFont().getName(), matrix2of5.getTextFont().getStyle(), fontSize));
                svgString = matrix2of5.writeSVGToString(code, x, y, width, height);
                break;
            case "nec2of5":
                NEC2of5 nec2of5 = new NEC2of5();
                nec2of5.setForeColor(foreColor);
                nec2of5.setBackColor(backColor);
                if(fontSize > 0) nec2of5.setTextFont(new Font(nec2of5.getTextFont().getName(), nec2of5.getTextFont().getStyle(), fontSize));
                svgString = nec2of5.writeSVGToString(code, x, y, width, height);
                break;
            case "QR":
                QRCode qr = new QRCode();
                qr.setForeColor(foreColor);
                qr.setBackColor(backColor);
                qr.setVersion(form.getQrVersion());
                qr.setErrorCorrect(form.getQrErrorCorrectionLevel());
                qr.setEncodeMode(form.getEncode());
                svgString = qr.writeSVGToString(code, x, y, width, height);
                break;
            case "DataMatrix":
                DataMatrix dm = new DataMatrix();
                dm.setForeColor(foreColor);
                dm.setBackColor(backColor);
                dm.setStringEncoding(form.getEncode());
                int codeSize = form.getDataMatrixCodeSize();
                DxCodeSize dxCodeSize = DxCodeSize.DxSzAuto;
                dxCodeSize = dxCodeSize.fromIntVal(codeSize);
                dm.setCodeSize(dxCodeSize);
                svgString = dm.writeSVGToString(code, x, y, width, height);
                break;
            case "PDF417":
                Pdf417 pdf417 = new Pdf417();
                pdf417.setForeColor(foreColor);
                pdf417.setBackColor(backColor);
                String sErrCorrectLevel = form.getPdf417ErrorCorrectionLevel();
                if(sErrCorrectLevel.equals("AUTO")) {
                    pdf417.setUseAutoErrorLevel(true);
                } else {
                    int errCorrectLevel = Integer.parseInt(sErrCorrectLevel);
                    pdf417.setUseAutoErrorLevel(false);
                    pdf417.setErrorLevel(errCorrectLevel);
                }
                pdf417.setAspectRatio(form.getPdf417AspectRatio());
                String sizeKind = form.getPdf417SizeKind();
                if(sizeKind.equals("AUTO")) {
                    pdf417.setSizeKind(Pdf417.SIZE_KIND.AUTO);
                } else if(sizeKind.equals("COL_NUM")) {
                    pdf417.setSizeKind(Pdf417.SIZE_KIND.COLUMNS);
                } else if(sizeKind.equals("ROW_NUM")) {
                    pdf417.setSizeKind(Pdf417.SIZE_KIND.ROWS);
                } else if(sizeKind.equals("COL_NUM_AND_ROW_NUM")) {
                    pdf417.setSizeKind(Pdf417.SIZE_KIND.COLUMNS_AND_ROWS);
                }
                pdf417.setStringEncoding(form.getEncode());
                svgString = pdf417.writeSVGToString(code, x, y, width, height);
                break;
            // JAN/UPCバーコード
            case "jan13":
                Jan13 jan13 = new Jan13();
                jan13.setForeColor(foreColor);
                jan13.setBackColor(backColor);
                if(fontSize > 0) jan13.setTextFont(new Font(jan13.getTextFont().getName(), jan13.getTextFont().getStyle(), fontSize));
                svgString = jan13.writeSVGToString(code, x, y, width, height);
                break;
            case "jan8":
                Jan8 jan8 = new Jan8();
                jan8.setForeColor(foreColor);
                jan8.setBackColor(backColor);
                if(fontSize > 0) jan8.setTextFont(new Font(jan8.getTextFont().getName(), jan8.getTextFont().getStyle(), fontSize));
                svgString = jan8.writeSVGToString(code, x, y, width, height);
                break;
            case "UPC-A":
                UpcA upcA = new UpcA();
                upcA.setForeColor(foreColor);
                upcA.setBackColor(backColor);
                if(fontSize > 0) upcA.setTextFont(new Font(upcA.getTextFont().getName(), upcA.getTextFont().getStyle(), fontSize));
                svgString = upcA.writeSVGToString(code, x, y, width, height);
                break;
            case "UPC-E":
                UpcE upcE = new UpcE();
                upcE.setForeColor(foreColor);
                upcE.setBackColor(backColor);
                if(fontSize > 0) upcE.setTextFont(new Font(upcE.getTextFont().getName(), upcE.getTextFont().getStyle(), fontSize));
                svgString = upcE.writeSVGToString(code, x, y, width, height);
                break;
            // GS1 Databar系
            case "gs1-Databar-14":
                Gs1Databar14 gs1db14 = new Gs1Databar14();
                gs1db14.setForeColor(foreColor);
                gs1db14.setBackColor(backColor);
                // シンボルタイプを設定
                if(typeDatabar14.equals("Omni-directional"))
                    gs1db14.setSymbolType(Gs1Databar14.DatabarType.OMNIDIRECTIONAL);
                else if(typeDatabar14.equals("Stacked"))
                    gs1db14.setSymbolType(Gs1Databar14.DatabarType.STACKED);
                else
                    gs1db14.setSymbolType(Gs1Databar14.DatabarType.STACKED_OMNIDIRECTIONAL);
                if(fontSize > 0) gs1db14.setTextFont(new Font(gs1db14.getTextFont().getName(), gs1db14.getTextFont().getStyle(), fontSize));
                svgString = gs1db14.writeSVGToString(code, x, y, width, height);
                break;
            case "gs1-Databar-Limited":
                Gs1DatabarLimited gs1dbLtd = new Gs1DatabarLimited();
                gs1dbLtd.setForeColor(foreColor);
                gs1dbLtd.setBackColor(backColor);
                if(fontSize > 0) gs1dbLtd.setTextFont(new Font(gs1dbLtd.getTextFont().getName(), gs1dbLtd.getTextFont().getStyle(), fontSize));
                svgString = gs1dbLtd.writeSVGToString(code, x, y, width, height);
                break;
            case "gs1-Databar-Expanded":
                Gs1DatabarExpanded gs1dbExp = new Gs1DatabarExpanded();
                gs1dbExp.setForeColor(foreColor);
                gs1dbExp.setBackColor(backColor);
                // シンボルタイプを設定
                if(typeDatabarExpanded.equals("UnStacked"))
                    gs1dbExp.setSymbolType(Gs1DatabarExpanded.DatabarType.UNSTACKED);
                else
                    gs1dbExp.setSymbolType(Gs1DatabarExpanded.DatabarType.STACKED);
                if(fontSize > 0) gs1dbExp.setTextFont(new Font(gs1dbExp.getTextFont().getName(), gs1dbExp.getTextFont().getStyle(), fontSize));
                svgString = gs1dbExp.writeSVGToString(code, x, y, width, height);
                break;
            // 郵便カスタマーバーコード
            case "yubin":
                YubinCustomer yubin = new YubinCustomer();
                yubin.setForeColor(foreColor);
                yubin.setBackColor(backColor);
                svgString = yubin.writeSVGToString(code, x, y, height);
                break;
            // コンビニ収納代行バーコード
            case "conv":
                EAN128 ean128Conv = new EAN128();
                ean128Conv.setForeColor(foreColor);
                ean128Conv.setBackColor(backColor);
                if(fontSize > 0) ean128Conv.setTextFont(new Font(ean128Conv.getTextFont().getName(), ean128Conv.getTextFont().getStyle(), fontSize));
                svgString = ean128Conv.writeSVGConvenienceToString(code, x, y, width, height);
                break;
            default:
                logger.warn("Unknown barcode kind for SVG: {}", kind);
                svgString = "";
                break;
        }

        // 空文字が返された場合はSVG未実装
        if (svgString == null || svgString.isEmpty()) {
            logger.warn("SVG output not implemented for kind={}", kind);
        }

        logger.info("generateSVGString response kind={} length={}", kind, svgString == null ? 0 : svgString.length());
        return svgString;
    }

}
