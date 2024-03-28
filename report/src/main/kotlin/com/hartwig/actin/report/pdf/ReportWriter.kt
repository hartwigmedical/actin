package com.hartwig.actin.report.pdf

import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.chapters.ClinicalDetailsChapter
import com.hartwig.actin.report.pdf.chapters.MolecularDetailsChapter
import com.hartwig.actin.report.pdf.chapters.ReportChapter
import com.hartwig.actin.report.pdf.chapters.SummaryChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingDetailsChapter
import com.hartwig.actin.report.pdf.tables.trial.ExternalTrialSummarizer
import com.hartwig.actin.report.pdf.util.Constants
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.util.Paths
import com.itextpdf.kernel.events.PdfDocumentEvent
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.CompressionConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.WriterProperties
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.properties.AreaBreakType
import java.io.ByteArrayOutputStream
import java.io.IOException
import org.apache.logging.log4j.LogManager

class ReportWriter(private val writeToDisk: Boolean, private val outputDirectory: String?, private val config: ReportConfiguration) {
    @Throws(IOException::class)
    fun write(report: Report) {
        write(report, false)
    }

    @Synchronized
    @Throws(IOException::class)
    fun write(report: Report, enableExtendedMode: Boolean) {
        LOGGER.debug("Initializing output styles")
        Styles.initialize()

        val detailsChapter = if (enableExtendedMode) {
            LOGGER.info("Including trial matching details")
            TrialMatchingDetailsChapter(report)
        } else null

        val chapters = listOfNotNull(
            SummaryChapter(report, ExternalTrialSummarizer(config.hideTrialsWithOverlappingMolecularTargets)),
            MolecularDetailsChapter(report),
            ClinicalDetailsChapter(report),
            TrialMatchingChapter(report, enableExtendedMode),
            detailsChapter
        )
        writePdfChapters(report.patientId, chapters, enableExtendedMode)
    }

    @Throws(IOException::class)
    private fun writePdfChapters(patientId: String, chapters: List<ReportChapter>, enableExtendedMode: Boolean) {
        val doc = initializeReport(patientId, enableExtendedMode)
        val pdfDocument = doc.pdfDocument
        val pageEventHandler: PageEventHandler = PageEventHandler.create(patientId)
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

    @Throws(IOException::class)
    private fun initializeReport(patientId: String, enableExtendedMode: Boolean): Document {
        val writer: PdfWriter
        if (writeToDisk && outputDirectory != null) {
            val outputFilePath =
                (Paths.forceTrailingFileSeparator(outputDirectory) + patientId + ".actin" + (if (enableExtendedMode) ".extended" else "")
                        + ".pdf")
            LOGGER.info("Writing PDF report to {}", outputFilePath)
            val properties = WriterProperties().setFullCompressionMode(true)
                .setCompressionLevel(CompressionConstants.BEST_COMPRESSION)
                .useSmartMode()
            writer = PdfWriter(outputFilePath, properties)
            writer.compressionLevel = 9
        } else {
            LOGGER.info("Generating in-memory PDF report")
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

    companion object {
        private val LOGGER = LogManager.getLogger(ReportWriter::class.java)
    }
}