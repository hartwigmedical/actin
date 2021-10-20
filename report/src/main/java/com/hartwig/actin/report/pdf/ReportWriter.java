package com.hartwig.actin.report.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.hartwig.actin.common.ActinRecord;
import com.hartwig.actin.report.pdf.chapters.ReportChapter;
import com.hartwig.actin.report.pdf.chapters.SummaryChapter;
import com.hartwig.actin.report.pdf.util.Constants;
import com.hartwig.actin.util.Paths;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.property.AreaBreakType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ReportWriter {

    private static final Logger LOGGER = LogManager.getLogger(ReportWriter.class);

    private final boolean writeToDisk;
    @NotNull
    private final String outputDirectory;

    ReportWriter(final boolean writeToDisk, @NotNull final String outputDirectory) {
        this.writeToDisk = writeToDisk;
        this.outputDirectory = outputDirectory;
    }

    public void write(@NotNull ActinRecord record) throws IOException {
        ReportChapter[] chapters = new ReportChapter[] { new SummaryChapter(record) };

        writePdfChapters(record.sampleId(), chapters);
    }

    private void writePdfChapters(@NotNull String sampleId, @NotNull ReportChapter[] chapters) throws IOException {
        Document doc = initializeReport(sampleId);
        PdfDocument pdfDocument = doc.getPdfDocument();

        PageEventHandler pageEventHandler = PageEventHandler.create(sampleId);
        pdfDocument.addEventHandler(PdfDocumentEvent.START_PAGE, pageEventHandler);

        for (int i = 0; i < chapters.length; i++) {
            ReportChapter chapter = chapters[i];
            pdfDocument.setDefaultPageSize(chapter.pageSize());
            pageEventHandler.chapterTitle(chapter.name());
            pageEventHandler.resetChapterPageCounter();

            if (i > 0) {
                doc.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            }
            chapter.render(doc);
        }

        pageEventHandler.writePageCounts(doc.getPdfDocument());

        doc.close();
        pdfDocument.close();
    }

    @NotNull
    private Document initializeReport(@NotNull String sampleId) throws IOException {
        PdfWriter writer;
        if (writeToDisk) {
            String outputFilePath = Paths.forceTrailingFileSeparator(outputDirectory) + sampleId + ".actin.pdf";
            LOGGER.info("Writing PDF report to {}", outputFilePath);
            writer = new PdfWriter(outputFilePath);
        } else {
            LOGGER.info("Generating in-memory PDF report");
            writer = new PdfWriter(new ByteArrayOutputStream());
        }

        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(PageSize.A4);
        pdf.getDocumentInfo().setTitle(Constants.METADATA_TITLE);
        pdf.getDocumentInfo().setAuthor(Constants.METADATA_AUTHOR);

        Document document = new Document(pdf);
        document.setMargins(Constants.PAGE_MARGIN_TOP,
                Constants.PAGE_MARGIN_RIGHT,
                Constants.PAGE_MARGIN_BOTTOM,
                Constants.PAGE_MARGIN_LEFT);

        return document;
    }
}
