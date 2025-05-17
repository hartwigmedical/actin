package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.IhcTestConfig
import com.hartwig.actin.clinical.feed.standard.FeedTestData.FEED_PATIENT_RECORD
import com.hartwig.actin.clinical.feed.standard.HASHED_ID_IN_BASE64
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.feed.datamodel.DatedEntry
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val IHC_LINE = "HER2 immunohistochemie: negative"
private const val MICROSCOPIE_LINE = "TTF1 negatief"

private val UNUSED_DATE = LocalDate.of(2024, 4, 15)
private val IHC_TEST = IhcTest(
    item = "HER2",
    measure = "negative",
    measureDate = UNUSED_DATE,
    impliesPotentialIndeterminateStatus = true
)

private val IHC_TEST_EGFR = IhcTest(
    item = "EGFR",
    measure = "c.2573T>G",
    measureDate = UNUSED_DATE,
    scoreText = "variant",
    impliesPotentialIndeterminateStatus = false
)

private val EHR_PATIENT_RECORD_WITH_IHC_TEST =
    FEED_PATIENT_RECORD.copy(ihcTests = listOf(DatedEntry(IHC_LINE, UNUSED_DATE)))

class StandardIhcTestExtractorTest {

    private val molecularTestCuration = mockk<CurationDatabase<IhcTestConfig>> {
        every { find(any()) } returns emptySet()
    }
    private val extractor = StandardIhcTestExtractor(molecularTestCuration)

    @Test
    fun `Should ignore lines if ignored in curation`() {
        returnIgnoreFromCurationDB(IHC_LINE)
        val result = extractor.extract(EHR_PATIENT_RECORD_WITH_IHC_TEST)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should give curation warnings for uncurated lines`() {
        every { molecularTestCuration.find(IHC_LINE) } returns emptySet()
        val result = extractor.extract(EHR_PATIENT_RECORD_WITH_IHC_TEST)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                patientId = HASHED_ID_IN_BASE64,
                category = CurationCategory.MOLECULAR_TEST_IHC,
                feedInput = IHC_LINE,
                message = "Could not find molecular test ihc config for input 'HER2 immunohistochemie: negative'"
            )
        )
    }

    @Test
    fun `Should curate molecular tests from ihc tests, supporting multiple configs per input, but ignore any curation warnings`() {
        val anotherMolecularTest = IHC_TEST_EGFR.copy(item = "ERBB2")
        every { molecularTestCuration.find(MICROSCOPIE_LINE) } returns setOf(
            IhcTestConfig(input = MICROSCOPIE_LINE, curated = IHC_TEST_EGFR),
            IhcTestConfig(input = MICROSCOPIE_LINE, curated = anotherMolecularTest)
        )

        val result = extractor.extract(
            FEED_PATIENT_RECORD.copy(ihcTests = listOf(DatedEntry(MICROSCOPIE_LINE, UNUSED_DATE)))
        )
        assertThat(result.extracted).containsExactly(IHC_TEST_EGFR, anotherMolecularTest)
        assertThat(result.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract and curate IHC lines from ihc result`() {
        every { molecularTestCuration.find(IHC_LINE) } returns setOf(IhcTestConfig(input = IHC_LINE, curated = IHC_TEST))
        val uncuratedInput = "uncurated"
        val result = extractor.extract(
            FEED_PATIENT_RECORD.copy(
                ihcTests = listOf(
                    DatedEntry(IHC_LINE, UNUSED_DATE), DatedEntry(uncuratedInput, UNUSED_DATE)
                )
            )
        )
        assertThat(result.extracted).containsExactly(IHC_TEST)
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                patientId = HASHED_ID_IN_BASE64,
                category = CurationCategory.MOLECULAR_TEST_IHC,
                feedInput = uncuratedInput,
                message = "Could not find molecular test ihc config for input 'uncurated'"
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