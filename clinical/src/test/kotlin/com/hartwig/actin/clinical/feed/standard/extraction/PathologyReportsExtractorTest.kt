package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.feed.standard.FeedTestData.FEED_PATIENT_RECORD
import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.feed.datamodel.FeedPathology
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class PathologyReportsExtractorTest {

    private val extractor = PathologyReportsExtractor()
    private val defaultDate = LocalDate.of(1970, 1, 1)

    @Test
    fun `Should extract empty pathology reports list when no pathology report is provided`() {
        assertThat(extractor.extract(FEED_PATIENT_RECORD).extracted).isEmpty()
    }

    @Test
    fun `Should extract pathology reports from the tumor details pathology`() {

        val providedPathologyReport = FeedPathology(
            tissueId = null,
            reportRequested = false,
            lab = "NKI-AvL",
            diagnosis = "diagnosis",
            tissueDate = defaultDate,
            authorisationDate = defaultDate,
            rawPathologyReport = "rawPathologyReport",
            reportHash = "ID 1"
        )

        val record = FEED_PATIENT_RECORD.copy(
            tumorDetails = FEED_PATIENT_RECORD.tumorDetails.copy(
                pathology = listOf(
                    providedPathologyReport,
                    providedPathologyReport.copy(
                        tissueId = "tissueId",
                        reportRequested = true,
                        lab = "lab",
                        reportDate = defaultDate,
                        rawPathologyReport = "raw pathology report"
                    )
                )
            )
        )

        val expected = PathologyReport(
            lab = "NKI-AvL",
            diagnosis = "diagnosis",
            tissueDate = defaultDate,
            authorisationDate = defaultDate,
            report = "rawPathologyReport"
        )

        val extracted = extractor.extract(record).extracted
        assertThat(extracted).isNotEmpty()
        assertThat(extracted).isEqualTo(
            listOf(
                expected,
                expected.copy(
                    tissueId = "tissueId",
                    lab = "lab",
                    reportDate = defaultDate,
                    report = "raw pathology report"
                )
            )
        )
    }
}