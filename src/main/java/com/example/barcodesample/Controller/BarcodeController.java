package com.example.barcodesample.Controller;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ResourceBundle;
import java.util.UUID;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Docker 用　(@CrossOrigin)
//import org.springframework.web.bind.annotation.CrossOrigin;

import com.example.barcodesample.util.BarcodeUtil;

@Controller
public class BarcodeController {
    private static final Logger logger = LoggerFactory.getLogger(BarcodeController.class);

    private static final String RESOURCE_NAME = "application";
    private static ResourceBundle rb = ResourceBundle.getBundle(RESOURCE_NAME);

    // 初期表示
    //@CrossOrigin
    @GetMapping("/barcode")
    public String barcode(Model model) {
        model.addAttribute("form", new BarcodeForm());
        return "barcode";
    }

    // ブラウザの背景色
    static Color browserBackColor = new Color(0xf8, 0xf8, 0xf8); 

    // 「バーコード生成」ボタンクリック時
    //@CrossOrigin
    @PostMapping("/barcode")
    public String barcode(@ModelAttribute("form") BarcodeForm form, Model model, HttpServletRequest request) throws Exception {

        BarcodeUtil creator = new BarcodeUtil();

        // 画像形式に応じたファイル名を決定
        String imgExtension = "png";
        String imageFormat = form.getImageFormat();
        if ("JPEG".equals(imageFormat)) {
            imgExtension = "jpg";
        } else if ("BMP".equals(imageFormat)) {
            imgExtension = "bmp";
        } else if ("SVG".equals(imageFormat)) {
            imgExtension = "svg";
        }
        String imgFilename = "barcode." + imgExtension;

        model.addAttribute("posted", "posted");

        // SVG出力の場合
        if(form.getImageTransferMethod().equals("svg") || form.getImageFormat().equals("SVG")) {
            String svgString = creator.generateSVGString(form);
            if(svgString != null && !svgString.isEmpty()) {
                // SVG文字列をBase64エンコードしてdata URIとして渡す
                String svgBase64 = java.util.Base64.getEncoder().encodeToString(svgString.getBytes("UTF-8"));
                model.addAttribute("imgFilename", "data:image/svg+xml;base64," + svgBase64);
                model.addAttribute("svgString", svgString);
            } else {
                // SVG未対応の場合は空画像
                model.addAttribute("imgFilename", "");
                model.addAttribute("svgNotSupported", true);
            }
            model.addAttribute("form", form);
            return "barcode";
        }

        // 1.ブラウザ表示ー画像ファイル表示 or
        // 3.画像ダウンロードが選択されている時
        if(form.getImageTransferMethod().equals("image_file")
        || form.isDownload()) {
            creator.generateBarcodeImage(imgFilename, form, browserBackColor);
            imgFilename = "/images/" + imgFilename;
            model.addAttribute("imgFilename", imgFilename);
        } 
        // 2.ブラウザ表示ー新しいストリーム方式でgetImageBase64()を使用
        else {
            // 画像形式に応じたフォーマットとMIMEタイプを決定
            String base64Format = "png";
            String mimeType = "image/png";
            if ("JPEG".equals(imageFormat)) {
                base64Format = "jpg";
                mimeType = "image/jpeg";
            } else if ("BMP".equals(imageFormat)) {
                base64Format = "bmp";
                mimeType = "image/bmp";
            }
            // 新しいストリームコンストラクタを使用してBase64文字列を直接取得
            String imageBase64 = creator.generateBarcodeBase64(form, base64Format);
            model.addAttribute("imgFilename", "data:" + mimeType + ";base64," + imageBase64);
        }


        // PDFの生成
        if (form.isPdf()) {

            // 4.PDF画像埋め込み
            if (form.getPdfOption().equals("image_file")) {
                // まず、PDF埋め込み用画像生成 (PDF側で座標処理するためflg_x0y0:true)
                creator.generateBarcodeImage("barcode_pdf.png", form, true, Color.WHITE);
                // 上で作成済みのバーコード画像ファイルをPDFに埋め込むだけ
                creator.generateBarcodePdfByImageFile(form);
            }
            // 5.Graphics2Dを使ってバーコードをPDFに描画
            else {
                // Graphics2Dを使ってバーコードをPDFに描画
                creator.generateBarcodePdfByGraphics(form);
            }
        }
        
        
        

        model.addAttribute("form", form);
        
        return "barcode";

    }

    @GetMapping("/pdf")
    public void downloadOrDisplayPdf(@ModelAttribute("form") BarcodeForm form, HttpServletResponse response) throws Exception {
        // ダウンロードフラグの取得（例: download=true）
        boolean isDownload = form.isDownload();
    
        // レスポンスの設定
        response.setContentType("application/pdf");
    
        if (isDownload) {
            // ダウンロードの場合
            response.setHeader("Content-Disposition", "attachment; filename=barcode.pdf");
        } else {
            // ブラウザ表示の場合
            response.setHeader("Content-Disposition",  "inline; filename=barcode.pdf");
        }
    
        String pdfFilename = "src/main/resources/pdf/barcode.pdf";
        Path appFolder = Paths.get("/app/src/main/resources");
    
        if (Files.exists(appFolder) && Files.isDirectory(appFolder)) {
            // Docker コンテナ上 (とりあえず)
            pdfFilename = "/app/" + pdfFilename;
        }
    
        // PDFファイルの読み込みと出力
        try (OutputStream out = response.getOutputStream();
             InputStream in = new BufferedInputStream(new FileInputStream(pdfFilename))) {
            byte[] buff = new byte[1024];
            int len;
            while ((len = in.read(buff)) != -1) {
                out.write(buff, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    

    /*
    // 今回はPDFのブラウザへの出力はPDFファイル渡しです。
    // 動的に行う場合、こちらのロジックをお使いください。
    // html 側もコメントアウトしてあり、動作確認済みロジックです。
    @GetMapping("/pdf")
    public String download(@ModelAttribute("form") BarcodeForm form, Model model, HttpServletResponse response) throws Exception {

        //PDFの場合
        //PDFプレビューの設定を実施
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline;");

        //その他の設定を実施
        response.setHeader("Cache-Control", "private");
        response.setHeader("Pragma", "");

        try(OutputStream out = response.getOutputStream();
            InputStream in =  new BufferedInputStream(new FileInputStream("src/main/resources/pdf/barcode.pdf"));){
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = in.read(buff, 0, buff.length)) != -1) {
                out.write(buff, 0, len);
            }
            out.flush();
        }catch(Exception e){
            System.err.println(e);
        }

        //画面遷移先はnullを指定
        return "";

    }
    */

    

}
