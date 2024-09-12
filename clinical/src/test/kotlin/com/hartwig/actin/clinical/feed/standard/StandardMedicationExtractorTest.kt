package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.CypInteractionConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.datamodel.clinical.AtcClassification
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.CypInteraction
import com.hartwig.actin.datamodel.clinical.Dosage
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.QTProlongatingRisk
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test

private const val MEDICATION_NAME = "medication_name"
private const val ATC_NAME = "atc_name"

class StandardMedicationExtractorTest {

    private val atcModel = mockk<AtcModel>()
    private val qtProlongatingRiskCuration = mockk<CurationDatabase<QTProlongatingConfig>>()
    private val cypInteractionCuration = mockk<CurationDatabase<CypInteractionConfig>>()
    private val atcClassification = atcClassification()
    private val extractor = StandardMedicationExtractor(atcModel, qtProlongatingRiskCuration, cypInteractionCuration)
    private val providedMedication = ProvidedMedication(
        name = MEDICATION_NAME,
        atcCode = "atc",
        administrationRoute = "route",
        dosage = 1.0,
        dosageUnit = "unit",
        frequency = 2.0,
        frequencyUnit = "unit",
        periodBetweenDosagesValue = 3.0,
        periodBetweenDosagesUnit = "unit",
        administrationOnlyIfNeeded = false,
        startDate = LocalDate.of(2024, 2, 26),
        endDate = LocalDate.of(2024, 2, 26),
        isTrial = false,
        isSelfCare = false
    )
    private val medication = Medication(
        name = ATC_NAME,
        administrationRoute = "route",
        dosage = Dosage(
            dosageMin = 1.0, dosageMax = 1.0,
            dosageUnit = "unit", frequency = 2.0, frequencyUnit = "unit",
            periodBetweenValue = 3.0, periodBetweenUnit = "unit",
            ifNeeded = false
        ),
        startDate = LocalDate.of(2024, 2, 26),
        stopDate = LocalDate.of(2024, 2, 26),
        atc = atcClassification,
        isTrialMedication = false,
        isSelfCare = false,
        qtProlongatingRisk = QTProlongatingRisk.NONE,
        cypInteractions = emptyList()
    )
    private val ehrPatientRecord = ProvidedPatientRecord(
        patientDetails = ProvidedPatientDetail(
            birthYear = 1980,
            gender = "MALE",
            registrationDate = LocalDate.of(2024, 2, 26),
            hashedId = "hashedId",
            hartwigMolecularDataPending = false
        ),
        medications = listOf(providedMedication),
        tumorDetails = ProvidedTumorDetail(
            diagnosisDate = LocalDate.of(2024, 2, 23),
            tumorLocation = "tumorLocation",
            tumorType = "tumorType",
            lesions = emptyList(),
            measurableDiseaseDate = LocalDate.of(2024, 2, 23),
            measurableDisease = false,
            tumorGradeDifferentiation = "tumorGradeDifferentiation",
        )
    )


    @Test
    fun `Should curate QT and CYP and extract medication`() {
        every { qtProlongatingRiskCuration.find(ATC_NAME) } returns setOf(
            QTProlongatingConfig(
                ATC_NAME,
                false,
                QTProlongatingRisk.KNOWN
            )
        )
        every { cypInteractionCuration.find(ATC_NAME) } returns setOf(
            CypInteractionConfig(
                ATC_NAME,
                false,
                listOf(CypInteraction(CypInteraction.Type.INDUCER, CypInteraction.Strength.STRONG, "cyp_gene"))
            )
        )

        val result = extractor.extract(ehrPatientRecord)
        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).containsExactly(
            medication.copy(
                qtProlongatingRisk = QTProlongatingRisk.KNOWN,
                cypInteractions = listOf(CypInteraction(CypInteraction.Type.INDUCER, CypInteraction.Strength.STRONG, "cyp_gene")),
            )
        )
    }

    @Test
    fun `Should default CYP and QT when no config found`() {
        every { qtProlongatingRiskCuration.find(ATC_NAME) } returns emptySet()
        every { cypInteractionCuration.find(ATC_NAME) } returns emptySet()
        val result = extractor.extract(ehrPatientRecord)
        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).containsExactly(medication)
    }

    @Test
    fun `Should not look up ATC code when medication is trial`() {
        noAtcLookupTest(
            providedMedication.copy(atcCode = null, isTrial = true),
            medication.copy(name = MEDICATION_NAME, atc = null, isTrialMedication = true)
        )
    }

    @Test
    fun `Should not look up ATC code when medication is self care`() {
        noAtcLookupTest(
            providedMedication.copy(atcCode = null, isSelfCare = true),
            medication.copy(name = MEDICATION_NAME, atc = null, isSelfCare = true)
        )
    }

    @Test
    fun `Should throw an exception if atc code is null but medication is not trial or self care`() {
        every { qtProlongatingRiskCuration.find(MEDICATION_NAME) } returns emptySet()
        every { cypInteractionCuration.find(MEDICATION_NAME) } returns emptySet()
        assertThrows(java.lang.IllegalStateException::class.java) {
            extractor.extract(ehrPatientRecord.copy(medications = listOf(providedMedication.copy(atcCode = null))))
        }
    }

    private fun noAtcLookupTest(modifiedMedication: ProvidedMedication, expected: Medication) {
        every { qtProlongatingRiskCuration.find(MEDICATION_NAME) } returns emptySet()
        every { cypInteractionCuration.find(MEDICATION_NAME) } returns emptySet()
        val result = extractor.extract(ehrPatientRecord.copy(medications = listOf(modifiedMedication)))
        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).containsExactly(expected)
    }

    private fun atcClassification(): AtcClassification {
        val atcClassification = mockk<AtcClassification>()
        every { atcClassification.chemicalSubstance } returns AtcLevel("atc_code", ATC_NAME)
        every { atcModel.resolveByCode("atc", "") } returns atcClassification
        return atcClassification
    }

}