package com.dms.documentsearch.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class OCRService {


    // ==========================================
    // CONFIGURATION
    // ==========================================

    private static final String TESSDATA_PATH =
            "C:\\Program Files\\Tesseract-OCR\\tessdata";


    /*
     * PDF pages are rendered at 300 DPI.
     *
     * This is usually a good balance between
     * OCR quality, memory usage and speed.
     */
    private static final float PDF_DPI = 300;


    /*
     * Small amount of upscaling for grayscale
     * and enhanced OCR passes.
     */
    private static final double SCALE_FACTOR = 1.25;


    /*
     * Threshold used only for the enhanced
     * black/white OCR pass.
     *
     * We do NOT depend only on this version.
     */
    private static final int THRESHOLD = 170;


    // ==========================================
    // CREATE TESSERACT INSTANCE
    // ==========================================

    private Tesseract createTesseract() {

        Tesseract tesseract =
                new Tesseract();


        // ======================================
        // TESSERACT DATA LOCATION
        // ======================================

        tesseract.setDatapath(
                TESSDATA_PATH
        );


        // ======================================
        // OCR LANGUAGE
        // ======================================

        /*
         * English model also recognizes
         * standard numbers.
         */
        tesseract.setLanguage(
                "eng"
        );


        // ======================================
        // PAGE SEGMENTATION MODE
        // ======================================

        /*
         * PSM 3 =
         * fully automatic page segmentation.
         *
         * Good default for general documents.
         */
        tesseract.setPageSegMode(
                3
        );


        // ======================================
        // OCR ENGINE MODE
        // ======================================

        /*
         * OEM 1 =
         * LSTM OCR engine.
         */
        tesseract.setOcrEngineMode(
                1
        );


        return tesseract;
    }


    // ==========================================
    // RUN TESSERACT
    // ==========================================

    private String runOCR(
            BufferedImage image)
            throws TesseractException {


        if (image == null) {

            return "";
        }


        Tesseract tesseract =
                createTesseract();


        String text =
                tesseract.doOCR(
                        image
                );


        if (text == null) {

            return "";
        }


        return text.trim();
    }


    // ==========================================
    // CREATE COPY OF IMAGE
    // ==========================================

    private BufferedImage copyImage(
            BufferedImage originalImage) {


        BufferedImage copiedImage =
                new BufferedImage(
                        originalImage.getWidth(),
                        originalImage.getHeight(),
                        BufferedImage.TYPE_INT_RGB
                );


        Graphics2D graphics =
                copiedImage.createGraphics();


        graphics.setColor(
                Color.WHITE
        );


        graphics.fillRect(
                0,
                0,
                copiedImage.getWidth(),
                copiedImage.getHeight()
        );


        graphics.drawImage(
                originalImage,
                0,
                0,
                null
        );


        graphics.dispose();


        return copiedImage;
    }


    // ==========================================
    // SCALE IMAGE
    // ==========================================

    private BufferedImage scaleImage(
            BufferedImage originalImage,
            double scaleFactor) {


        int newWidth =
                (int) Math.round(
                        originalImage.getWidth()
                        * scaleFactor
                );


        int newHeight =
                (int) Math.round(
                        originalImage.getHeight()
                        * scaleFactor
                );


        BufferedImage scaledImage =
                new BufferedImage(
                        newWidth,
                        newHeight,
                        BufferedImage.TYPE_INT_RGB
                );


        Graphics2D graphics =
                scaledImage.createGraphics();


        graphics.setColor(
                Color.WHITE
        );


        graphics.fillRect(
                0,
                0,
                newWidth,
                newHeight
        );


        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );


        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );


        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );


        graphics.drawImage(
                originalImage,
                0,
                0,
                newWidth,
                newHeight,
                null
        );


        graphics.dispose();


        return scaledImage;
    }


    // ==========================================
    // CREATE GRAYSCALE IMAGE
    // ==========================================

    private BufferedImage createGrayscaleImage(
            BufferedImage originalImage) {


        BufferedImage scaledImage =
                scaleImage(
                        originalImage,
                        SCALE_FACTOR
                );


        BufferedImage grayscaleImage =
                new BufferedImage(
                        scaledImage.getWidth(),
                        scaledImage.getHeight(),
                        BufferedImage.TYPE_BYTE_GRAY
                );


        Graphics2D graphics =
                grayscaleImage.createGraphics();


        graphics.setColor(
                Color.WHITE
        );


        graphics.fillRect(
                0,
                0,
                grayscaleImage.getWidth(),
                grayscaleImage.getHeight()
        );


        graphics.drawImage(
                scaledImage,
                0,
                0,
                null
        );


        graphics.dispose();


        scaledImage.flush();


        return grayscaleImage;
    }


    // ==========================================
    // CREATE HIGH-CONTRAST IMAGE
    // ==========================================

    private BufferedImage createHighContrastImage(
            BufferedImage originalImage) {


        BufferedImage grayscaleImage =
                createGrayscaleImage(
                        originalImage
                );


        int width =
                grayscaleImage.getWidth();


        int height =
                grayscaleImage.getHeight();


        BufferedImage binaryImage =
                new BufferedImage(
                        width,
                        height,
                        BufferedImage.TYPE_BYTE_BINARY
                );


        // ======================================
        // APPLY THRESHOLD
        // ======================================

        for (int y = 0;
             y < height;
             y++) {


            for (int x = 0;
                 x < width;
                 x++) {


                int rgb =
                        grayscaleImage.getRGB(
                                x,
                                y
                        );


                /*
                 * Because this is grayscale,
                 * red/green/blue values are
                 * effectively the same.
                 */
                int gray =
                        rgb & 0xFF;


                int outputColor;


                if (gray < THRESHOLD) {

                    outputColor =
                            0xFF000000;

                } else {

                    outputColor =
                            0xFFFFFFFF;
                }


                binaryImage.setRGB(
                        x,
                        y,
                        outputColor
                );
            }
        }


        grayscaleImage.flush();


        return binaryImage;
    }


    // ==========================================
    // SCORE OCR RESULT
    // ==========================================

    /*
     * We need a simple way to decide which
     * OCR pass produced the most useful text.
     *
     * The score rewards:
     *
     * - letters
     * - numbers
     * - spaces
     * - useful document punctuation
     *
     * Numbers receive slightly more weight
     * because IDs, dates, invoice numbers,
     * PIN codes etc. are important in a DMS.
     */

    private int calculateTextScore(
            String text) {


        if (text == null ||
                text.isBlank()) {

            return 0;
        }


        int score = 0;


        for (int i = 0;
             i < text.length();
             i++) {


            char character =
                    text.charAt(i);


            if (Character.isDigit(character)) {

                score += 3;

            } else if (
                    Character.isLetter(character)) {

                score += 2;

            } else if (
                    Character.isWhitespace(character)) {

                score += 1;

            } else if (
                    character == ':' ||
                    character == '-' ||
                    character == '/' ||
                    character == '.' ||
                    character == ',' ||
                    character == '@') {

                score += 1;
            }
        }


        return score;
    }


    // ==========================================
    // CHOOSE BEST OCR RESULT
    // ==========================================

    private String chooseBestResult(
            String originalText,
            String grayscaleText,
            String enhancedText) {


        int originalScore =
                calculateTextScore(
                        originalText
                );


        int grayscaleScore =
                calculateTextScore(
                        grayscaleText
                );


        int enhancedScore =
                calculateTextScore(
                        enhancedText
                );


        System.out.println(
                "Original OCR score: "
                + originalScore
        );


        System.out.println(
                "Grayscale OCR score: "
                + grayscaleScore
        );


        System.out.println(
                "Enhanced OCR score: "
                + enhancedScore
        );


        String bestText =
                originalText;


        int bestScore =
                originalScore;


        String bestMethod =
                "ORIGINAL";


        if (grayscaleScore > bestScore) {

            bestText =
                    grayscaleText;

            bestScore =
                    grayscaleScore;

            bestMethod =
                    "GRAYSCALE";
        }


        if (enhancedScore > bestScore) {

            bestText =
                    enhancedText;

            bestMethod =
                    "ENHANCED";
        }


        System.out.println(
                "Best OCR method: "
                + bestMethod
        );


        if (bestText == null) {

            return "";
        }


        return bestText.trim();
    }


    // ==========================================
    // MULTI-PASS OCR
    // ==========================================

    private String performMultiPassOCR(
            BufferedImage originalImage)
            throws TesseractException {


        System.out.println(
                "Starting multi-pass OCR..."
        );


        BufferedImage originalCopy =
                null;


        BufferedImage grayscaleImage =
                null;


        BufferedImage enhancedImage =
                null;


        try {


            // ==================================
            // PASS 1
            // ORIGINAL IMAGE
            // ==================================

            System.out.println(
                    "OCR pass 1: original image..."
            );


            originalCopy =
                    copyImage(
                            originalImage
                    );


            String originalText =
                    runOCR(
                            originalCopy
                    );


            // ==================================
            // PASS 2
            // GRAYSCALE IMAGE
            // ==================================

            System.out.println(
                    "OCR pass 2: grayscale image..."
            );


            grayscaleImage =
                    createGrayscaleImage(
                            originalImage
                    );


            String grayscaleText =
                    runOCR(
                            grayscaleImage
                    );


            // ==================================
            // PASS 3
            // HIGH-CONTRAST IMAGE
            // ==================================

            System.out.println(
                    "OCR pass 3: enhanced image..."
            );


            enhancedImage =
                    createHighContrastImage(
                            originalImage
                    );


            String enhancedText =
                    runOCR(
                            enhancedImage
                    );


            // ==================================
            // SELECT BEST RESULT
            // ==================================

            String bestResult =
                    chooseBestResult(
                            originalText,
                            grayscaleText,
                            enhancedText
                    );


            System.out.println(
                    "Multi-pass OCR completed."
            );


            return bestResult;


        } finally {


            if (originalCopy != null) {

                originalCopy.flush();
            }


            if (grayscaleImage != null) {

                grayscaleImage.flush();
            }


            if (enhancedImage != null) {

                enhancedImage.flush();
            }
        }
    }


    // ==========================================
    // OCR JPG / JPEG / PNG
    // ==========================================

    public String extractText(
            File file)
            throws TesseractException {


        System.out.println(
                "Starting image OCR..."
        );


        BufferedImage originalImage =
                null;


        try {


            // ==================================
            // LOAD IMAGE
            // ==================================

            originalImage =
                    ImageIO.read(
                            file
                    );


            if (originalImage == null) {

                throw new TesseractException(
                        "Unable to read image file."
                );
            }


            // ==================================
            // MULTI-PASS OCR
            // ==================================

            String text =
                    performMultiPassOCR(
                            originalImage
                    );


            System.out.println(
                    "Image OCR completed."
            );


            System.out.println(
                    "Total OCR characters extracted: "
                    + text.length()
            );


            return text;


        } catch (IOException e) {


            throw new TesseractException(
                    "Failed to read image: "
                    + e.getMessage()
            );


        } finally {


            if (originalImage != null) {

                originalImage.flush();
            }
        }
    }


    // ==========================================
    // OCR SCANNED PDF
    // ==========================================

    public String extractTextFromPdf(
            File pdfFile)
            throws IOException,
                   TesseractException {


        StringBuilder extractedText =
                new StringBuilder();


        // ======================================
        // OPEN PDF
        // ======================================

        try (PDDocument document =
                Loader.loadPDF(
                        pdfFile
                )) {


            // ==================================
            // CREATE PDF RENDERER
            // ==================================

            PDFRenderer renderer =
                    new PDFRenderer(
                            document
                    );


            int totalPages =
                    document
                            .getNumberOfPages();


            System.out.println(
                    "OCR PDF pages: "
                    + totalPages
            );


            // ==================================
            // PROCESS EVERY PAGE
            // ==================================

            for (int page = 0;
                 page < totalPages;
                 page++) {


                System.out.println(
                        "=================================="
                );


                System.out.println(
                        "OCR processing page: "
                        + (page + 1)
                );


                BufferedImage originalImage =
                        null;


                try {


                    // ==========================
                    // RENDER PAGE
                    // ==========================

                    originalImage =
                            renderer
                                    .renderImageWithDPI(
                                            page,
                                            PDF_DPI,
                                            ImageType.RGB
                                    );


                    System.out.println(
                            "PDF page rendered at "
                            + PDF_DPI
                            + " DPI."
                    );


                    // ==========================
                    // MULTI-PASS OCR
                    // ==========================

                    String pageText =
                            performMultiPassOCR(
                                    originalImage
                            );


                    // ==========================
                    // STORE PAGE TEXT
                    // ==========================

                    if (pageText != null &&
                            !pageText.isBlank()) {


                        extractedText
                                .append(
                                        "----- PAGE "
                                        + (page + 1)
                                        + " -----"
                                )


                                .append(
                                        System.lineSeparator()
                                )


                                .append(
                                        pageText.trim()
                                )


                                .append(
                                        System.lineSeparator()
                                )


                                .append(
                                        System.lineSeparator()
                                );
                    }


                    System.out.println(
                            "OCR completed page: "
                            + (page + 1)
                    );


                } finally {


                    // ==========================
                    // RELEASE PAGE IMAGE
                    // ==========================

                    if (originalImage != null) {

                        originalImage.flush();
                    }
                }
            }
        }


        // ======================================
        // FINISHED
        // ======================================

        System.out.println(
                "=================================="
        );


        System.out.println(
                "PDF OCR completed successfully."
        );


        System.out.println(
                "Total OCR characters extracted: "
                + extractedText.length()
        );


        return extractedText.toString();
    }
}