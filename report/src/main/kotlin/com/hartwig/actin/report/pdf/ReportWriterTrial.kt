package com.hartwig.actin.report.pdf

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.chapters.ClinicalDetailsChapter
import com.hartwig.actin.report.pdf.chapters.MolecularDetailsChapter
import com.hartwig.actin.report.pdf.chapters.ReportChapter
import com.hartwig.actin.report.pdf.chapters.SummaryChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingDetailsChapter
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
import org.apache.logging.log4j.LogManager
import java.io.ByteArrayOutputStream
import java.io.IOException

class ReportWriterTrial(private val writeToDisk: Boolean, private val outputDirectory: String?) : ReportWriter {


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
            SummaryChapter(report),
            MolecularDetailsChapter(report),
            ClinicalDetailsChapter(report),
            TrialMatchingChapter(report, enableExtendedMode),
            detailsChapter
        )
        writePdfChapters(report.patientId, chapters, enableExtendedMode)
    }

    companion object {
        private val LOGGER = LogManager.getLogger(ReportWriterTrial::class.java)
    }
}