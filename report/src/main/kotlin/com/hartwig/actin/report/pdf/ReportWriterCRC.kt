package com.hartwig.actin.report.pdf

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.chapters.ClinicalDetailsChapter
import com.hartwig.actin.report.pdf.chapters.EfficacyEvidenceChapter
import com.hartwig.actin.report.pdf.chapters.EfficacyEvidenceDetailsChapter
import com.hartwig.actin.report.pdf.chapters.SummaryChapterCRC
import com.hartwig.actin.report.pdf.chapters.TrialMatchingChapter
import com.hartwig.actin.report.pdf.util.Styles
import org.apache.logging.log4j.LogManager
import java.io.IOException

class ReportWriterCRC(override val writeToDisk: Boolean, override val outputDirectory: String?) : ReportWriter {

    @Throws(IOException::class)
    fun write(report: Report) {
        write(report, true)
    }

    @Synchronized
    @Throws(IOException::class)
    fun write(report: Report, enableExtendedMode: Boolean) {
        LOGGER.debug("Initializing output styles")
        Styles.initialize()

        val efficacyEvidenceDetailsChapter = if (enableExtendedMode) {
            LOGGER.info("Including SOC literature details")
            EfficacyEvidenceDetailsChapter(report.treatmentMatch.standardOfCareMatches)
        } else null

        val chapters = listOfNotNull(
            SummaryChapterCRC(report),
            EfficacyEvidenceChapter(report),
            ClinicalDetailsChapter(report),
            TrialMatchingChapter(report, enableExtendedMode),
            //efficacyEvidenceDetailsChapter
        )
        writePdfChapters(report.patientId, chapters, enableExtendedMode)
    }

    companion object {
        private val LOGGER = LogManager.getLogger(ReportWriterCRC::class.java)
    }
}