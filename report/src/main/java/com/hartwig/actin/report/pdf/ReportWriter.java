package com.hartwig.actin.report.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.hartwig.actin.report.datamodel.Report;
import com.hartwig.actin.report.pdf.chapters.ClinicalDetailsChapter;
import com.hartwig.actin.report.pdf.chapters.MolecularDetailsChapter;
import com.hartwig.actin.report.pdf.chapters.ReportChapter;
import com.hartwig.actin.report.pdf.chapters.SummaryChapter;
import com.hartwig.actin.report.pdf.chapters.TrialMatchingChapter;
import com.hartwig.actin.report.pdf.chapters.TrialMatchingDetailsChapter;
import com.hartwig.actin.report.pdf.util.Constants;
import com.hartwig.actin.report.pdf.util.Styles;
import com.hartwig.actin.util.Paths;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.CompressionConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.properties.AreaBreakType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ReportWriter {

    private static final Logger LOGGER = LogManager.getLogger(ReportWriter.class);

    private final boolean writeToDisk;
    @Nullable
    private final String outputDirectory;

    ReportWriter(final boolean writeToDisk, @Nullable final String outputDirectory) {
        this.writeToDisk = writeToDisk;
        this.outputDirectory = outputDirectory;
    }

    public void write(@NotNull Report report) throws IOException {
        write(report, false);
    }

    public synchronized void write(@NotNull Report report, boolean skipTrialMatchingDetails) throws IOException {
        LOGGER.debug("Initializing output styles");
        Styles.initialize();
        List<ReportChapter> chapters = new LinkedList<>(Arrays.asList(new SummaryChapter(report), new MolecularDetailsChapter(report),
                new ClinicalDetailsChapter(report), new TrialMatchingChapter(report, skipTrialMatchingDetails)));

        if (skipTrialMatchingDetails) {
            LOGGER.info("Skipping trial matching details");
        } else {
            chapters.add(new TrialMatchingDetailsChapter(report));
        }

        writePdfChapters(report.patientId(), chapters);
    }

    private void writePdfChapters(@NotNull String patientId, @NotNull List<ReportChapter> chapters) throws IOException {
        Document doc = initializeReport(patientId);
        PdfDocument pdfDocument = doc.getPdfDocument();

        PageEventHandler pageEventHandler = PageEventHandler.create(patientId);
        pdfDocument.addEventHandler(PdfDocumentEvent.START_PAGE, pageEventHandler);

        for (int i = 0; i < chapters.size(); i++) {
            ReportChapter chapter = chapters.get(i);
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
    private Document initializeReport(@NotNull String patientId) throws IOException {
        PdfWriter writer;
        if (writeToDisk && outputDirectory != null) {
            String outputFilePath = Paths.forceTrailingFileSeparator(outputDirectory) + patientId + ".actin.pdf";
            LOGGER.info("Writing PDF report to {}", outputFilePath);
            WriterProperties properties = new WriterProperties()
                    .setFullCompressionMode(true)
                    .setCompressionLevel(CompressionConstants.BEST_COMPRESSION)
                    .useSmartMode();
            writer = new PdfWriter(outputFilePath, properties);
            writer.setCompressionLevel(9);
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
