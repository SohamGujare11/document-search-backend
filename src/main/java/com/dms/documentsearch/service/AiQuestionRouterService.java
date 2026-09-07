package com.dms.documentsearch.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.dms.documentsearch.entity.Document;

@Service
public class AiQuestionRouterService {

    private final DocumentStatsService documentStatsService;


    public AiQuestionRouterService(
            DocumentStatsService documentStatsService) {

        this.documentStatsService =
                documentStatsService;
    }


    // ==========================================
    // DATABASE QUESTION DETECTION
    // ==========================================

    public boolean isDatabaseQuestion(
            String question) {

        if (question == null ||
                question.isBlank()) {

            return false;
        }


        String q =
                question
                        .toLowerCase(Locale.ROOT)
                        .trim();


        return
                q.contains("how many document")
                || q.contains("number of document")
                || q.contains("total document")

                || q.contains("how many pdf")
                || q.contains("number of pdf")
                || q.contains("total pdf")

                || q.contains("latest document")
                || q.contains("last document")
                || q.contains("recent document")
                || q.contains("most recent document")

                || q.contains("documents uploaded today")
                || q.contains("documents i uploaded today")
                || q.contains("uploaded documents today")

                || q.contains("documents uploaded yesterday")
                || q.contains("documents i uploaded yesterday")
                || q.contains("uploaded documents yesterday")

                || q.contains("who uploaded")
                || q.contains("who has uploaded");
    }


    // ==========================================
    // ANSWER DATABASE QUESTION
    // ==========================================

    public String answerDatabaseQuestion(
            String question,
            String username,
            boolean isAdmin) {

        String q =
                question
                        .toLowerCase(Locale.ROOT)
                        .trim();


        // ======================================
        // WHO UPLOADED YESTERDAY - ADMIN
        // ======================================

        if (
                q.contains("who")
                && q.contains("upload")
                && q.contains("yesterday")
        ) {

            if (!isAdmin) {

                return "You can only view information about your own document uploads.";
            }


            List<Document> documents =
                    documentStatsService
                            .getAllDocumentsUploadedYesterdayList();


            if (documents.isEmpty()) {

                return "No documents were uploaded yesterday.";
            }


            String users =
                    documents
                            .stream()
                            .map(Document::getOwnerUsername)
                            .distinct()
                            .collect(
                                    Collectors.joining(", ")
                            );


            return "Users who uploaded documents yesterday: "
                    + users
                    + ".";
        }


        // ======================================
        // WHO UPLOADED TODAY - ADMIN
        // ======================================

        if (
                q.contains("who")
                && q.contains("upload")
                && q.contains("today")
        ) {

            if (!isAdmin) {

                return "You can only view information about your own document uploads.";
            }


            List<Document> documents =
                    documentStatsService
                            .getAllDocumentsUploadedTodayList();


            if (documents.isEmpty()) {

                return "No documents have been uploaded today.";
            }


            String users =
                    documents
                            .stream()
                            .map(Document::getOwnerUsername)
                            .distinct()
                            .collect(
                                    Collectors.joining(", ")
                            );


            return "Users who uploaded documents today: "
                    + users
                    + ".";
        }


        // ======================================
        // PDF COUNT
        // ======================================

        if (
                q.contains("how many pdf")
                || q.contains("number of pdf")
                || q.contains("total pdf")
        ) {

            long count;


            if (isAdmin &&
                    (
                        q.contains("system")
                        || q.contains("all")
                        || q.contains("total")
                    )) {

                count =
                        documentStatsService
                                .getAllPdfCount();


                return "There are "
                        + count
                        + (count == 1
                            ? " PDF document in the system."
                            : " PDF documents in the system.");
            }


            count =
                    documentStatsService
                            .getPdfCount(username);


            return "You have "
                    + count
                    + (count == 1
                        ? " PDF document."
                        : " PDF documents.");
        }


        // ======================================
        // YESTERDAY DOCUMENT COUNT
        // ======================================

        if (
                (
                    q.contains("how many document")
                    || q.contains("number of document")
                    || q.contains("total document")
                )
                && q.contains("yesterday")
        ) {

            if (
                    isAdmin
                    && !q.contains("i ")
                    && !q.contains("my ")
            ) {

                long count =
                        documentStatsService
                                .getAllDocumentsUploadedYesterday();


                return count
                        + (count == 1
                            ? " document was uploaded yesterday."
                            : " documents were uploaded yesterday.");
            }


            long count =
                    documentStatsService
                            .getDocumentsUploadedYesterday(
                                    username
                            );


            return "You uploaded "
                    + count
                    + (count == 1
                        ? " document yesterday."
                        : " documents yesterday.");
        }


        // ======================================
        // TODAY DOCUMENT COUNT
        // ======================================

        if (
                (
                    q.contains("how many document")
                    || q.contains("number of document")
                    || q.contains("total document")
                )
                && q.contains("today")
        ) {

            if (
                    isAdmin
                    && !q.contains("i ")
                    && !q.contains("my ")
            ) {

                long count =
                        documentStatsService
                                .getAllDocumentsUploadedToday();


                return count
                        + (count == 1
                            ? " document has been uploaded today."
                            : " documents have been uploaded today.");
            }


            long count =
                    documentStatsService
                            .getDocumentsUploadedToday(
                                    username
                            );


            return "You uploaded "
                    + count
                    + (count == 1
                        ? " document today."
                        : " documents today.");
        }


        // ======================================
        // LIST YESTERDAY'S DOCUMENTS
        // ======================================

        if (
                q.contains("yesterday")
                && q.contains("document")
                && (
                    q.contains("which")
                    || q.contains("list")
                    || q.contains("what")
                )
        ) {

            List<Document> documents;


            if (
                    isAdmin
                    && !q.contains("my ")
                    && !q.contains("i ")
            ) {

                documents =
                        documentStatsService
                                .getAllDocumentsUploadedYesterdayList();
            }

            else {

                documents =
                        documentStatsService
                                .getDocumentsUploadedYesterdayList(
                                        username
                                );
            }


            if (documents.isEmpty()) {

                return "No documents were uploaded yesterday.";
            }


            String names =
                    documents
                            .stream()
                            .map(document ->
                                    document.getOriginalFileName()
                                    + " ("
                                    + document.getOwnerUsername()
                                    + ")"
                            )
                            .collect(
                                    Collectors.joining(", ")
                            );


            return "Documents uploaded yesterday: "
                    + names
                    + ".";
        }


        // ======================================
        // LIST TODAY'S DOCUMENTS
        // ======================================

        if (
                q.contains("today")
                && q.contains("document")
                && (
                    q.contains("which")
                    || q.contains("list")
                    || q.contains("what")
                )
        ) {

            List<Document> documents;


            if (
                    isAdmin
                    && !q.contains("my ")
                    && !q.contains("i ")
            ) {

                documents =
                        documentStatsService
                                .getAllDocumentsUploadedTodayList();
            }

            else {

                documents =
                        documentStatsService
                                .getDocumentsUploadedTodayList(
                                        username
                                );
            }


            if (documents.isEmpty()) {

                return "No documents have been uploaded today.";
            }


            String names =
                    documents
                            .stream()
                            .map(document ->
                                    document.getOriginalFileName()
                                    + " ("
                                    + document.getOwnerUsername()
                                    + ")"
                            )
                            .collect(
                                    Collectors.joining(", ")
                            );


            return "Documents uploaded today: "
                    + names
                    + ".";
        }


        // ======================================
        // LATEST DOCUMENT
        // ======================================

        if (
                q.contains("latest document")
                || q.contains("last document")
                || q.contains("recent document")
                || q.contains("most recent document")
        ) {

            Optional<Document> latest;


            if (
                    isAdmin
                    && (
                        q.contains("system")
                        || q.contains("overall")
                    )
            ) {

                latest =
                        documentStatsService
                                .getLatestSystemDocument();
            }

            else {

                latest =
                        documentStatsService
                                .getLatestDocument(
                                        username
                                );
            }


            if (latest.isEmpty()) {

                return "No uploaded documents were found.";
            }


            Document document =
                    latest.get();


            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(
                            "dd MMM yyyy, hh:mm a"
                    );


            return "The latest document is \""
                    + document.getOriginalFileName()
                    + "\", uploaded by "
                    + document.getOwnerUsername()
                    + " on "
                    + document.getUploadedAt()
                              .format(formatter)
                    + ".";
        }


        // ======================================
        // TOTAL DOCUMENT COUNT
        // ======================================

        if (
                q.contains("how many document")
                || q.contains("number of document")
                || q.contains("total document")
        ) {

            if (
                    isAdmin
                    && (
                        q.contains("system")
                        || q.contains("all")
                        || q.contains("total")
                    )
            ) {

                long count =
                        documentStatsService
                                .getAllDocumentsCount();


                return "There are "
                        + count
                        + (count == 1
                            ? " document in the system."
                            : " documents in the system.");
            }


            long count =
                    documentStatsService
                            .getTotalDocuments(
                                    username
                            );


            return "You have "
                    + count
                    + (count == 1
                        ? " document."
                        : " documents.");
        }


        return null;
    }
}