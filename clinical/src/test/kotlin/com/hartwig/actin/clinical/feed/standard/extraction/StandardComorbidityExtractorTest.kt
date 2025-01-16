package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.feed.standard.EhrTestData.createEhrPatientRecord
import com.hartwig.actin.clinical.feed.standard.ProvidedComplication
import com.hartwig.actin.clinical.feed.standard.ProvidedOtherCondition
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.OtherCondition
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val OTHER_CONDITION_NAME = "prior_condition"

private val OTHER_CONDITION = OtherCondition(
    name = OTHER_CONDITION_NAME,
    icdCodes = setOf(IcdCode("icdCode"))
)

private val EHR_OTHER_CONDITION = ProvidedOtherCondition(
    name = OTHER_CONDITION_NAME,
    startDate = LocalDate.of(2024, 2, 27),
    endDate = LocalDate.of(2024, 2, 28)
)

private val EHR_PATIENT_RECORD_WITH_OTHER_CONDITIONS = createEhrPatientRecord().copy(
    priorOtherConditions = listOf(
        EHR_OTHER_CONDITION
    )
)

private const val COMPLICATION_NAME = "complication"
private val EHR_START_DATE = LocalDate.of(2023, 6, 15)
private val CURATED_COMPLICATION = Complication(COMPLICATION_NAME, EHR_START_DATE.year, EHR_START_DATE.monthValue, setOf(IcdCode("code")))

class StandardComorbidityExtractorTest {

    private val comorbidityCuration = mockk<CurationDatabase<ComorbidityConfig>>()
    private val extractor = StandardComorbidityExtractor(comorbidityCuration)

    @Test
    fun `Should extract other conditions and curate the condition`() {
        val anotherCondition = "another_condition"
        every { comorbidityCuration.find(OTHER_CONDITION_NAME) } returns setOf(
            ComorbidityConfig(
                input = OTHER_CONDITION_NAME,
                ignore = false,
                curated = OTHER_CONDITION
            ),
            ComorbidityConfig(
                input = OTHER_CONDITION_NAME,
                ignore = false,
                curated = OTHER_CONDITION.copy(anotherCondition)
            )
        )
        val result = extractor.extract(EHR_PATIENT_RECORD_WITH_OTHER_CONDITIONS)
        assertThat(result.extracted).containsExactly(
            OTHER_CONDITION.withDefaultYearAndMonth(2024, 2),
            OTHER_CONDITION.copy(year = 2024, month = 2, name = anotherCondition)
        )
    }

    @Test
    fun `Should return curation warnings when no curation found`() {
        every { comorbidityCuration.find(OTHER_CONDITION_NAME) } returns emptySet()
        val result = extractor.extract(EHR_PATIENT_RECORD_WITH_OTHER_CONDITIONS)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                EHR_PATIENT_RECORD_WITH_OTHER_CONDITIONS.patientDetails.hashedId,
                CurationCategory.NON_ONCOLOGICAL_HISTORY,
                OTHER_CONDITION_NAME,
                "Could not find non-oncological history config for input 'prior_condition'"
            )
        )
    }

    @Test
    fun `Should extract complication with end date`() {
        assertExtractedComplication(ProvidedComplication(COMPLICATION_NAME, EHR_START_DATE, LocalDate.now()))
    }

    @Test
    fun `Should extract complication without end date`() {
        assertExtractedComplication(ProvidedComplication(COMPLICATION_NAME, EHR_START_DATE, null))
    }

    private fun assertExtractedComplication(providedComplication: ProvidedComplication) {
        every { comorbidityCuration.find(COMPLICATION_NAME) }.returns(
            setOf(ComorbidityConfig("input", false, curated = CURATED_COMPLICATION))
        )
        val ehrPatientRecord = createEhrPatientRecord().copy(complications = listOf(providedComplication))
        val result = extractor.extract(ehrPatientRecord)
        assertThat(result.extracted).hasSize(1)
        val extracted = result.extracted[0]
        assertThat(extracted.name).isEqualTo(COMPLICATION_NAME)
        assertThat(extracted.year).isEqualTo(EHR_START_DATE.year)
        assertThat(extracted.month).isEqualTo(EHR_START_DATE.monthValue)
    }


    @Test
    fun `Should extract intolerances from other conditions, supporting multiple configs per input`() {
        val name = "allergy"
        val curated = Intolerance(name = "curated", icdCodes = setOf(IcdCode("icd", null)))
        val anotherCurated = Intolerance(name = "another curated", icdCodes = setOf(IcdCode("icd", null)))

        val intoleranceCuration = mockk<CurationDatabase<ComorbidityConfig>> {
            every { find(name) } returns setOf<ComorbidityConfig>(
                ComorbidityConfig(input = name, ignore = false, curated = curated),
                ComorbidityConfig(input = name, ignore = false, curated = anotherCurated)
            )
        }
        val extractor = StandardComorbidityExtractor(intoleranceCuration)

        val year = 2024
        val month = 4
        val results = extractor.extract(
            createEhrPatientRecord().copy(
                priorOtherConditions = listOf(ProvidedOtherCondition(name = name, startDate = LocalDate.of(year, month, 22)))
            )
        )
        assertThat(results.extracted).isEqualTo(listOf(curated, anotherCurated).map { it.withDefaultYearAndMonth(year, month) })
        assertThat(results.evaluation.warnings).isEmpty()
    }
}