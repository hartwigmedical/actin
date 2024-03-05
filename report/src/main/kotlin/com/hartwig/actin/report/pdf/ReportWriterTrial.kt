package com.hartwig.actin.report.pdf

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.chapters.ClinicalDetailsChapter
import com.hartwig.actin.report.pdf.chapters.MolecularDetailsChapter
import com.hartwig.actin.report.pdf.chapters.SummaryChapterTrial
import com.hartwig.actin.report.pdf.chapters.TrialMatchingChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingDetailsChapter
import com.hartwig.actin.report.pdf.util.Styles
import org.apache.logging.log4j.LogManager
import java.io.IOException

class ReportWriterTrial(override val writeToDisk: Boolean, override val outputDirectory: String?) : ReportWriter {

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
            SummaryChapterTrial(report),
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