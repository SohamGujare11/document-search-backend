package com.dms.documentsearch.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@Service
public class DocumentNumberExtractionService {

    // ==========================================
    // CONFIGURATION
    // ==========================================

    private static final String TESSDATA_PATH =
            "C:\\Program Files\\Tesseract-OCR\\tessdata";

    private static final float PDF_DPI = 400;

    private static final double SCALE_FACTOR = 1.5;


    // ==========================================
    // CREATE TESSERACT
    // ==========================================

    private Tesseract createTesseract() {

        Tesseract tesseract =
                new Tesseract();

        tesseract.setDatapath(
                TESSDATA_PATH
        );

        tesseract.setLanguage(
                "eng"
        );

        /*
         * PSM 6:
         *
         * Assume a single uniform block of text.
         *
         * This is different from the general
         * OCRService which uses PSM 3.
         *
         * Having a second segmentation strategy
         * can recover text missed by general OCR.
         */
        tesseract.setPageSegMode(
                6
        );

        /*
         * LSTM OCR engine.
         */
        tesseract.setOcrEngineMode(
                1
        );

        return tesseract;
    }


    // ==========================================
    // UPSCALE IMAGE
    // ==========================================

    private BufferedImage upscaleImage(
            BufferedImage originalImage) {

        int newWidth =
                (int) Math.round(
                        originalImage.getWidth()
                                * SCALE_FACTOR
                );

        int newHeight =
                (int) Math.round(
                        originalImage.getHeight()
                                * SCALE_FACTOR
                );

        BufferedImage scaledImage =
                new BufferedImage(
                        newWidth,
                        newHeight,
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D graphics =
                scaledImage.createGraphics();

        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC
        );

        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
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
    // RUN NUMBER-FOCUSED OCR
    // ==========================================

    private String performNumberOCR(
            BufferedImage originalImage)
            throws TesseractException {

        BufferedImage scaledImage =
                upscaleImage(
                        originalImage
                );

        try {

            Tesseract tesseract =
                    createTesseract();

            String text =
                    tesseract.doOCR(
                            scaledImage
                    );

            if (text == null) {
                return "";
            }

            return text.trim();

        } finally {

            scaledImage.flush();
        }
    }


    // ==========================================
    // EXTRACT EXTRA TEXT FROM PDF
    // ==========================================

    public String extractNumbersFromPdf(
            File pdfFile)
            throws IOException,
                   TesseractException {

        StringBuilder result =
                new StringBuilder();

        System.out.println(
                "Starting number-focused OCR..."
        );

        try (PDDocument document =
                Loader.loadPDF(pdfFile)) {

            PDFRenderer renderer =
                    new PDFRenderer(document);

            int totalPages =
                    document.getNumberOfPages();

            System.out.println(
                    "Number OCR PDF pages: "
                            + totalPages
            );

            for (int page = 0;
                 page < totalPages;
                 page++) {

                System.out.println(
                        "Number OCR processing page: "
                                + (page + 1)
                );

                BufferedImage image =
                        null;

                try {

                    /*
                     * Render at 400 DPI because
                     * small ID/document numbers
                     * need more detail.
                     */
                    image =
                            renderer.renderImageWithDPI(
                                    page,
                                    PDF_DPI,
                                    ImageType.RGB
                            );

                    String pageText =
                            performNumberOCR(
                                    image
                            );

                    if (pageText != null
                            && !pageText.isBlank()) {

                        result
                                .append(
                                        "----- NUMBER OCR PAGE "
                                                + (page + 1)
                                                + " -----"
                                )
                                .append(
                                        System.lineSeparator()
                                )
                                .append(
                                        pageText
                                )
                                .append(
                                        System.lineSeparator()
                                )
                                .append(
                                        System.lineSeparator()
                                );
                    }

                    System.out.println(
                            "Number OCR completed page: "
                                    + (page + 1)
                    );

                } finally {

                    if (image != null) {
                        image.flush();
                    }
                }
            }
        }

        System.out.println(
                "Number-focused OCR completed."
        );

        System.out.println(
                "Number OCR characters extracted: "
                        + result.length()
        );

        return result.toString();
    }
}