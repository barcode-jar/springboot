package com.example.barcodesample.Controller;

public class BarcodeForm {

    private String kind;
    private int x = 50;
    private int y = 50;
    private int width;
    private int height;
    private String code;
    private String drawType;
    private boolean dispStartStopCode;
    private String codeSet128;
    private String typeDatabar14;
    private String typeDatabarExpanded;
    private boolean pdf;
    private boolean download;
    private String imageFormat = "PNG";

    public String getDrawType() {
        return drawType;
    }

    public void setDrawType(String drawType) {
        this.drawType = drawType;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean getDispStartStopCode() {
        return dispStartStopCode;
    }

    public void setDispStartStopCode(boolean dispStartStopCode) {
        this.dispStartStopCode = dispStartStopCode;
    }

    // codeSet128のゲッターとセッター
    public String getCodeSet128() {
        return codeSet128;
    }

    public void setCodeSet128(String codeSet128) {
        this.codeSet128 = codeSet128;
    }

    // typeDatabar14のゲッターとセッター
    public String getTypeDatabar14() {
        return typeDatabar14;
    }

    public void setTypeDatabar14(String typeDatabar14) {
        this.typeDatabar14 = typeDatabar14;
    }

    // typeDatabarExpandedのゲッターとセッター
    public String getTypeDatabarExpanded() {
        return typeDatabarExpanded;
    }

    public void setTypeDatabarExpanded(String typeDatabarExpanded) {
        this.typeDatabarExpanded = typeDatabarExpanded;
    }


    // ダウンロードのゲッターとセッター
    public boolean isDownload() {
        return download;
    }

    public void setDownload(boolean download) {
        this.download = download;
    }

    // PDF出力のゲッターとセッター
    public boolean isPdf() {
        return pdf;
    }

    public void setPdf(boolean pdf) {
        this.pdf = pdf;
    }

    // 画像フォーマットのゲッターとセッター
    public String getImageFormat() {
        return imageFormat;
    }

    public void setImageFormat(String imageFormat) {
        this.imageFormat = imageFormat;
    }

    // 画像ファイル・データの渡し方のゲッターとセッター
    // デフォルトは「画像データ渡し」(getImageBase64を使用)
    private String imageTransferMethod = "image_data";

    public String getImageTransferMethod() {
        return imageTransferMethod;
    }

    public void setImageTransferMethod(String imageTransferMethod) {
        this.imageTransferMethod = imageTransferMethod;
    }

    // PDFの埋め込み方のゲッターとセッター
    private String pdfOption = "0";

    public String getPdfOption() {
        return pdfOption;
    }

    public void setPdfOption(String pdfOption) {
        this.pdfOption = pdfOption;
    }

    private String qrErrorCorrectionLevel = "M";
    public String getQrErrorCorrectionLevel() {
        return qrErrorCorrectionLevel;
    }
    public void setQrErrorCorrectionLevel(String qrErrorCorrectionLevel) {
        this.qrErrorCorrectionLevel = qrErrorCorrectionLevel;
    }

    private int qrVersion = 0;
    public int getQrVersion() {
        return qrVersion;
    }
    public void setQrVersion(int qrVersion) {
        this.qrVersion = qrVersion;
    }

    private String encode = "UTF-8";
    public String getEncode() {
        return encode;
    }
    public void setEncode(String encode) {
        this.encode = encode;
    }

    // PDF417
    private String pdf417ErrorCorrectionLevel = "AUTO";
    private float pdf417AspectRatio = 0.5f;
    private String pdf417SizeKind = "AUTO";
    private int pdf417Cols = 3;
    private int pdf417Rows = 3;

    // Setter methods
    public void setPdf417ErrorCorrectionLevel(String pdf417ErrorCorrectionLevel) {
        this.pdf417ErrorCorrectionLevel = pdf417ErrorCorrectionLevel;
    }
    
    public void setPdf417AspectRatio(float pdf417AspectRatio) {
        this.pdf417AspectRatio = pdf417AspectRatio;
    }
    
    public void setPdf417SizeKind(String pdf417SizeKind) {
        this.pdf417SizeKind = pdf417SizeKind;
    }
    
    public void setPdf417Cols(int pdf417Cols) {
        this.pdf417Cols = pdf417Cols;
    }
    
    public void setPdf417Rows(int pdf417Rows) {
        this.pdf417Rows = pdf417Rows;
    }

    // Getter methods
    public String getPdf417ErrorCorrectionLevel() {
        return pdf417ErrorCorrectionLevel;
    }
    
    public float getPdf417AspectRatio() {
        return pdf417AspectRatio;
    }
    
    public String getPdf417SizeKind() {
        return pdf417SizeKind;
    }
    
    public int getPdf417Cols() {
        return pdf417Cols;
    }
    
    public Integer getPdf417Rows() {
        return pdf417Rows;
    }   

    // DataMatrix
    int dataMatrixCodeSize = -2;

    public void setDataMatrixCodeSize(int dataMatrixCodeSize) {
        this.dataMatrixCodeSize = dataMatrixCodeSize;
    }
    public int getDataMatrixCodeSize() {
        return dataMatrixCodeSize;
    }

    // フォントサイズ（デフォルト14pt）
    private int fontSize = 14;

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    // バーコードの色（前景色）- 16進数RGB形式 (例: "000000" = 黒)
    private String foreColor = "000000";

    public String getForeColor() {
        return foreColor;
    }

    public void setForeColor(String foreColor) {
        this.foreColor = foreColor;
    }

    // 背景色 - 16進数RGB形式 (例: "FFFFFF" = 白)
    private String backColor = "FFFFFF";

    public String getBackColor() {
        return backColor;
    }

    public void setBackColor(String backColor) {
        this.backColor = backColor;
    }

    // 背景透明フラグ
    private boolean transparentBackground = false;

    public boolean isTransparentBackground() {
        return transparentBackground;
    }

    public void setTransparentBackground(boolean transparentBackground) {
        this.transparentBackground = transparentBackground;
    }

    // X/Y座標までのマージンを画像データに含めるかどうか（デフォルトはfalse=含めない）
    private boolean includeMargin = false;

    public boolean isIncludeMargin() {
        return includeMargin;
    }

    public void setIncludeMargin(boolean includeMargin) {
        this.includeMargin = includeMargin;
    }

}
