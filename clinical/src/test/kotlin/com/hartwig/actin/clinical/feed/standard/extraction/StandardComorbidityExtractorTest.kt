package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationWarning
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.curation.config.ToxicityCuration
import com.hartwig.actin.clinical.feed.standard.EhrTestData.createEhrPatientRecord
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.Ecg
import com.hartwig.actin.datamodel.clinical.EcgMeasure
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.OtherCondition
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.datamodel.clinical.provided.ProvidedAllergy
import com.hartwig.actin.datamodel.clinical.provided.ProvidedComplication
import com.hartwig.actin.datamodel.clinical.provided.ProvidedOtherCondition
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val OTHER_CONDITION_NAME = "other_condition"
private const val COMPLICATION_NAME = "complication"

class StandardComorbidityExtractorTest {

    private val startDate = LocalDate.of(2024, 4, 22)

    private val ehrPatientRecordWithOtherCondition = createEhrPatientRecord().copy(
        priorOtherConditions = listOf(
            ProvidedOtherCondition(
                name = OTHER_CONDITION_NAME,
                startDate = startDate,
                endDate = startDate.plusDays(1)
            )
        )
    )

    private val curatedComplication = Complication(COMPLICATION_NAME, setOf(IcdCode("code")), startDate.year, startDate.monthValue)
    private val comorbidityCuration = mockk<CurationDatabase<ComorbidityConfig>>()
    private val extractor = StandardComorbidityExtractor(comorbidityCuration)

    @Test
    fun `Should extract other conditions and curate the condition`() {
        val otherCondition = OtherCondition(
            name = OTHER_CONDITION_NAME,
            icdCodes = setOf(IcdCode("icdCode"))
        )

        val anotherCondition = "another_condition"
        every { comorbidityCuration.find(OTHER_CONDITION_NAME) } returns setOf(
            ComorbidityConfig(
                input = OTHER_CONDITION_NAME,
                ignore = false,
                curated = otherCondition
            ),
            ComorbidityConfig(
                input = OTHER_CONDITION_NAME,
                ignore = false,
                curated = otherCondition.copy(anotherCondition)
            )
        )
        val result = extractor.extract(ehrPatientRecordWithOtherCondition)
        assertThat(result.extracted).containsExactly(
            otherCondition.withDefaultDate(startDate),
            otherCondition.copy(year = startDate.year, month = startDate.monthValue, name = anotherCondition)
        )
    }


    @Test
    fun `Should extract ECG from other conditions`() {
        val ecg = Ecg(
            name = "ecg",
            qtcfMeasure = EcgMeasure(1, "qUnit"),
            jtcMeasure = EcgMeasure(2, "jUnit")
        )
        every { comorbidityCuration.find(OTHER_CONDITION_NAME) } returns setOf(
            ComorbidityConfig(
                input = OTHER_CONDITION_NAME,
                ignore = false,
                curated = ecg
            )
        )
        val result = extractor.extract(ehrPatientRecordWithOtherCondition)
        assertThat(result.extracted).containsExactly(ecg.withDefaultDate(startDate))
    }

    @Test
    fun `Should return curation warnings when no curation found`() {
        every { comorbidityCuration.find(OTHER_CONDITION_NAME) } returns emptySet()
        val result = extractor.extract(ehrPatientRecordWithOtherCondition)
        assertThat(result.extracted).isEmpty()
        assertThat(result.evaluation.warnings).containsExactly(
            CurationWarning(
                ehrPatientRecordWithOtherCondition.patientDetails.hashedId,
                CurationCategory.NON_ONCOLOGICAL_HISTORY,
                OTHER_CONDITION_NAME,
                "Could not find non-oncological history config for input 'other_condition'"
            )
        )
    }

    @Test
    fun `Should extract complication with end date`() {
        assertExtractedComplication(ProvidedComplication(COMPLICATION_NAME, startDate, LocalDate.now()))
    }

    @Test
    fun `Should extract complication without end date`() {
        assertExtractedComplication(ProvidedComplication(COMPLICATION_NAME, startDate, null))
    }

    private fun assertExtractedComplication(providedComplication: ProvidedComplication) {
        every { comorbidityCuration.find(COMPLICATION_NAME) }.returns(
            setOf(ComorbidityConfig("input", false, curated = curatedComplication))
        )
        val ehrPatientRecord = createEhrPatientRecord().copy(complications = listOf(providedComplication))
        val result = extractor.extract(ehrPatientRecord)
        assertThat(result.extracted).hasSize(1)
        val extracted = result.extracted[0]
        assertThat(extracted.name).isEqualTo(COMPLICATION_NAME)
        assertThat(extracted.year).isEqualTo(startDate.year)
        assertThat(extracted.month).isEqualTo(startDate.monthValue)
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

        val results = extractor.extract(
            createEhrPatientRecord().copy(
                priorOtherConditions = listOf(ProvidedOtherCondition(name = name, startDate = startDate))
            )
        )
        assertThat(results.extracted).isEqualTo(
            listOf(curated, anotherCurated).map { it.copy(year = startDate.year, month = startDate.monthValue) }
        )
        assertThat(results.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract toxicities from other conditions`() {
        val name = "toxicity"
        val grade = 2
        val input = "input"
        val icdCodes = setOf(IcdCode("icd", null))
        val curation = ToxicityCuration(
            name = name,
            grade = grade,
            icdCodes = icdCodes
        )
        val toxicityCuration = mockk<CurationDatabase<ComorbidityConfig>> {
            every { find(input) } returns setOf(ComorbidityConfig(input = name, ignore = false, curated = curation))
        }
        val extractor = StandardComorbidityExtractor(toxicityCuration)
        val results = extractor.extract(
            createEhrPatientRecord().copy(priorOtherConditions = listOf(ProvidedOtherCondition(name = input, startDate = startDate)))
        )
        assertThat(results.extracted).containsExactly(
            Toxicity(
                name = name,
                grade = grade,
                icdCodes = icdCodes,
                evaluatedDate = startDate,
                source = ToxicitySource.EHR,
                endDate = null
            )
        )
        assertThat(results.evaluation.warnings).isEmpty()
    }

    @Test
    fun `Should extract intolerances from allergies when no curation present`() {
        assertExtractedIntolerances(emptySet(), "allergy", "")
    }

    @Test
    fun `Should extract intolerances from allergies and augment with curation when present`() {
        val curated = "curated"
        val icd = "icd"
        val curation = ComorbidityConfig(input = "allergy", ignore = false, curated = Intolerance(curated, setOf(IcdCode(icd, null))))
        assertExtractedIntolerances(setOf(curation), curated, icd)
    }

    private fun assertExtractedIntolerances(foundCuration: Set<ComorbidityConfig>, expectedName: String, expectedIcd: String) {
        val name = "allergy"
        val clinicalStatus = "clinicalStatus"
        val verificationStatus = "verificationStatus"
        val severity = "severity"
        val patientRecord = createEhrPatientRecord().copy(
            allergies = listOf(
                ProvidedAllergy(
                    name = name,
                    category = "category",
                    clinicalStatus = clinicalStatus,
                    verificationStatus = verificationStatus,
                    severity = severity,
                    startDate = LocalDate.of(2024, 4, 22),
                    endDate = LocalDate.of(2024, 4, 22)
                )
            )
        )
        every { comorbidityCuration.find(name) } returns foundCuration
        val results = extractor.extract(patientRecord)
        assertThat(results.extracted).containsExactly(
            Intolerance(
                name = expectedName,
                icdCodes = setOf(IcdCode(expectedIcd, null)),
                clinicalStatus = clinicalStatus,
                verificationStatus = verificationStatus,
                criticality = severity,
            )
        )
    }
}