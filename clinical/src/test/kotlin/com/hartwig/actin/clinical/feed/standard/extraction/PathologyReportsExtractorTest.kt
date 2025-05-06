package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.feed.standard.EhrTestData
import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPathologyReport
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class PathologyReportsExtractorTest {

    private val extractor = PathologyReportsExtractor()

    private val ehrPatientRecord = EhrTestData.createEhrPatientRecord()

    private val defaultDate = LocalDate.of(1970, 1, 1)

    @Test
    fun `Should extract empty pathology reports list when no pathology report is provided`() {
        assertThat(extractor.extract(ehrPatientRecord).extracted).isEmpty()
    }

    @Test
    fun `Should extract pathology reports from the tumor details pathology`() {

        val providedPathologyReport = ProvidedPathologyReport(
            reportRequested = false,
            source = "internal",
            diagnosis = "diagnosis",
            tissueDate = defaultDate,
            authorisationDate = defaultDate,
            rawPathologyReport = "rawPathologyReport"
        )

        val record = ehrPatientRecord.copy(
            tumorDetails = ehrPatientRecord.tumorDetails.copy(
                pathology = listOf(
                    providedPathologyReport,
                    providedPathologyReport.copy(
                        tissueId = "tissueId",
                        reportRequested = true,
                        source = "external",
                        lab = "lab",
                        rawPathologyReport = "raw pathology report"
                    )
                )
            )
        )

        val expected = PathologyReport(
            reportRequested = false,
            source = "internal",
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
                    reportRequested = true,
                    source = "external",
                    lab = "lab",
                    report = "raw pathology report"
                )
            )
        )
    }
}