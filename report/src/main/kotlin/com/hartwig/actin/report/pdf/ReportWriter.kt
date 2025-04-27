package com.hartwig.actin.report.pdf

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.chapters.ReportChapter
import com.hartwig.actin.report.pdf.util.Constants
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.util.Paths
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.CompressionConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.kernel.pdf.event.PdfDocumentEvent
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.properties.AreaBreakType
import org.apache.logging.log4j.LogManager
import java.io.ByteArrayOutputStream
import java.time.LocalDate

class ReportWriter(private val writeToDisk: Boolean, private val outputDirectory: String?) {

    private val logger = LogManager.getLogger(ReportWriter::class.java)

    fun write(report: Report) {
        write(report, false)
    }

    @Synchronized
    fun write(report: Report, enableExtendedMode: Boolean) {
        logger.info("Building report for patient ${report.patientId} with configuration ${report.config}")

        logger.debug("Initializing output styles")
        Styles.initialize()

        val chapters = ReportContentProvider(report, enableExtendedMode).provideChapters()
        writePdfChapters(report.patientId, report.patientRecord.patient.hospitalPatientId, chapters, enableExtendedMode, report.reportDate)
    }

    private fun writePdfChapters(
        patientId: String,
        hospitalPatientId: String?,
        chapters: List<ReportChapter>,
        enableExtendedMode: Boolean,
        reportDate: LocalDate
    ) {
        val doc = initializeReport(patientId, enableExtendedMode)
        val pdfDocument = doc.pdfDocument
        val pageEventHandler: PageEventHandler = PageEventHandler.create(patientId, hospitalPatientId, reportDate)
        pdfDocument.addEventHandler(PdfDocumentEvent.START_PAGE, pageEventHandler)
        for (i in chapters.indices) {
            val chapter = chapters[i]
            pdfDocument.defaultPageSize = chapter.pageSize()
            pageEventHandler.chapterTitle(chapter.name())
            pageEventHandler.resetChapterPageCounter()
            if (i > 0) {
                doc.add(AreaBreak(AreaBreakType.NEXT_PAGE))
            }
            chapter.render(doc)
        }
        pageEventHandler.writePageCounts(doc.pdfDocument)
        doc.close()
        pdfDocument.close()
    }

    private fun initializeReport(patientId: String, enableExtendedMode: Boolean): Document {
        val writer: PdfWriter
        if (writeToDisk && outputDirectory != null) {
            val outputFilePath =
                (Paths.forceTrailingFileSeparator(outputDirectory) + patientId + ".actin" + (if (enableExtendedMode) ".extended" else "")
                        + ".pdf")
            logger.info("Writing PDF report to {}", outputFilePath)
            val properties = WriterProperties().setFullCompressionMode(true)
                .setCompressionLevel(CompressionConstants.BEST_COMPRESSION)
                .useSmartMode()
            writer = PdfWriter(outputFilePath, properties)
            writer.compressionLevel = 9
        } else {
            logger.info("Generating in-memory PDF report")
            writer = PdfWriter(ByteArrayOutputStream())
        }

        val pdf = PdfDocument(writer)
        pdf.defaultPageSize = PageSize.A4
        pdf.documentInfo.title = Constants.METADATA_TITLE
        pdf.documentInfo.author = Constants.METADATA_AUTHOR
        val document = Document(pdf)
        document.setMargins(
            Constants.PAGE_MARGIN_TOP,
            Constants.PAGE_MARGIN_RIGHT,
            Constants.PAGE_MARGIN_BOTTOM,
            Constants.PAGE_MARGIN_LEFT
        )

        return document
    }
}