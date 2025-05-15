package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.IhcTestConfig
import com.hartwig.actin.clinical.feed.standard.FeedTestData.FEED_PATIENT_RECORD
import com.hartwig.actin.clinical.feed.standard.HASHED_ID_IN_BASE64
import com.hartwig.actin.clinical.feed.standard.OTHER_CONDITION_INPUT
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.feed.datamodel.DatedEntry
import com.hartwig.feed.datamodel.FeedPathology
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val IHC_LINE = "HER2 immunohistochemie: negative"
private val IHC_TEST = IhcTest(item = "HER2", measure = "negative", impliesPotentialIndeterminateStatus = true)
private const val MICROSCOPIE_LINE = "TTF1 negatief"

private val PATHOLOGY_REPORT = FeedPathology(
    lab = "lab",
    diagnosis = "diagnosis",
    reportRequested = true,
    rawPathologyReport = "Microscopie:\n$MICROSCOPIE_LINE\n\nConclusie:\n\nunrelated.\r\n\r\n\r\n$IHC_LINE\n\n",
    tissueId = "",
)
private val IHC_TEST_EGFR =
    IhcTest(
        item = "EGFR",
        measure = "c.2573T>G",
        measureDate = LocalDate.parse("2024-03-25"),
        scoreText = "variant",
        impliesPotentialIndeterminateStatus = false
    )

private val EHR_PATIENT_RECORD_WITH_PATHOLOGY =
    FEED_PATIENT_RECORD.copy(tumorDetails = FEED_PATIENT_RECORD.tumorDetails.copy(pathology = listOf(PATHOLOGY_REPORT)))

private val UNUSED_DATE = LocalDate.of(2024, 4, 15)

class StandardIhcTestExtractorTest {

    private val molecularTestCuration = mockk<CurationDatabase<IhcTestConfig>> {
        every { find(any()) } returns emptySet()
    }
    private val extractor = StandardIhcTestExtractor(molecularTestCuration)

    @Test
    fun `Should return no molecular test configs when tumor differentiation is null`() {
        val noDifferentiation =
            FEED_PATIENT_RECORD.copy(tumorDetails = FEED_PATIENT_RECORD.tumorDetails.copy(tumorGradeDifferentiation = null))
        val result = extractor.extract(noDifferentiation)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract and curate IHC lines from tumor grade differentiation`() {
        every { molecularTestCuration.find(IHC_LINE) } returns setOf(
            IhcTestConfig(
                input = IHC_LINE,
                curated = IHC_TEST
            )
        )
        val result = extractor.extract(EHR_PATIENT_RECORD_WITH_PATHOLOGY)
        assertThat(result.extracted).containsExactly(IHC_TEST)
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should ignore lines if ignored in curation`() {
        returnIgnoreFromCurationDB(IHC_LINE)
        val result =
            extractor.extract(EHR_PATIENT_RECORD_WITH_PATHOLOGY)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should give curation warnings for uncurated lines`() {
        every { molecularTestCuration.find(IHC_LINE) } returns emptySet()
        val result =
            extractor.extract(EHR_PATIENT_RECORD_WITH_PATHOLOGY)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                patientId = HASHED_ID_IN_BASE64,
                category = CurationCategory.MOLECULAR_TEST_IHC,
                feedInput = "HER2 immunohistochemie: negative",
                message = "Could not find molecular test ihc config for input 'HER2 immunohistochemie: negative'"
            )
        )
    }

    @Test
    fun `Should curate molecular tests from other conditions, supporting multiple configs per input, but ignore any curation warnings`() {
        val anotherMolecularTest = IHC_TEST_EGFR.copy(item = "ERBB2")
        every { molecularTestCuration.find(OTHER_CONDITION_INPUT) } returns setOf(
            IhcTestConfig(input = OTHER_CONDITION_INPUT, curated = IHC_TEST_EGFR),
            IhcTestConfig(input = OTHER_CONDITION_INPUT, curated = anotherMolecularTest)
        )
        val result = extractor.extract(
            FEED_PATIENT_RECORD.copy(
                otherConditions = listOf(
                    DatedEntry(name = OTHER_CONDITION_INPUT, startDate = UNUSED_DATE),
                    DatedEntry(name = "another prior condition", startDate = UNUSED_DATE)
                )
            )
        )
        assertThat(result.extracted).containsExactly(IHC_TEST_EGFR, anotherMolecularTest)
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should curate molecular tests from tumor grade differentiation lines, supporting multiple configs per input, but ignore any curation warnings`() {
        val anotherMolecularTest = IHC_TEST_EGFR.copy(item = "ERBB2")
        every { molecularTestCuration.find(MICROSCOPIE_LINE) } returns setOf(
            IhcTestConfig(input = MICROSCOPIE_LINE, curated = IHC_TEST_EGFR),
            IhcTestConfig(input = MICROSCOPIE_LINE, curated = anotherMolecularTest)
        )
        val result = extractor.extract(
            FEED_PATIENT_RECORD.copy(
                tumorDetails = FEED_PATIENT_RECORD.tumorDetails.copy(
                    pathology = listOf(
                        PATHOLOGY_REPORT.copy(
                            rawPathologyReport = PATHOLOGY_REPORT.rawPathologyReport.replace(
                                IHC_LINE, ""
                            )
                        )
                    )

                )
            )
        )
        assertThat(result.extracted).containsExactly(IHC_TEST_EGFR, anotherMolecularTest)
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract and curate IHC lines from ihc result`() {
        every { molecularTestCuration.find(IHC_LINE) } returns setOf(
            IhcTestConfig(
                input = IHC_LINE,
                curated = IHC_TEST
            )
        )
        val uncuratedInput = "uncurated"
        val result = extractor.extract(
            FEED_PATIENT_RECORD.copy(
                ihcTests = listOf(
                    DatedEntry(IHC_LINE, UNUSED_DATE),
                    DatedEntry(uncuratedInput, UNUSED_DATE)
                )
            )
        )
        assertThat(result.extracted).containsExactly(IHC_TEST)
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                patientId = HASHED_ID_IN_BASE64,
                category = CurationCategory.MOLECULAR_TEST_IHC,
                feedInput = uncuratedInput,
                message = "Could not find molecular test config for input 'uncurated'"
            )
        )
    }

    @Test
    fun `Should ignore lines if ignored in curation using ihc result alone or combined with patient id`() {
        val firstIhc = "first IHC"
        val secondIhc = "second IHC"
        val record = FEED_PATIENT_RECORD.copy(
            ihcTests = listOf(DatedEntry(firstIhc, UNUSED_DATE), DatedEntry(secondIhc, UNUSED_DATE))
        )

        val inputWithPatientAndIhc = "$HASHED_ID_IN_BASE64 | $firstIhc"
        returnIgnoreFromCurationDB(inputWithPatientAndIhc)

        returnIgnoreFromCurationDB(secondIhc)

        val result = extractor.extract(record)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }

    private fun returnIgnoreFromCurationDB(inputWithPatientAndIhc: String) {
        every { molecularTestCuration.find(inputWithPatientAndIhc) } returns setOf(
            IhcTestConfig(input = inputWithPatientAndIhc, ignore = true)
        )
    }
}