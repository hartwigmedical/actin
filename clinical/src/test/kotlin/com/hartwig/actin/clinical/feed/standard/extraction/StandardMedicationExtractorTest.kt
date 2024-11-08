package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.DrugInteractionConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.clinical.feed.standard.ProvidedMedication
import com.hartwig.actin.clinical.feed.standard.ProvidedPatientDetail
import com.hartwig.actin.clinical.feed.standard.ProvidedPatientRecord
import com.hartwig.actin.clinical.feed.standard.ProvidedTumorDetail
import com.hartwig.actin.datamodel.clinical.AtcClassification
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.DrugInteraction
import com.hartwig.actin.datamodel.clinical.Dosage
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.QTProlongatingRisk
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import java.time.LocalDate

private const val MEDICATION_NAME = "medication_name"
private const val ATC_NAME = "atc_name"

class StandardMedicationExtractorTest {

    private val atcModel = mockk<AtcModel>()
    private val qtProlongatingRiskCuration = mockk<CurationDatabase<QTProlongatingConfig>>()
    private val drugInteractionCuration = mockk<CurationDatabase<DrugInteractionConfig>>()
    private val atcClassification = atcClassification()
    private val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
    private val extractor = StandardMedicationExtractor(atcModel, treatmentDatabase, qtProlongatingRiskCuration, drugInteractionCuration)
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
            hartwigMolecularDataExpected = false
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
        every { drugInteractionCuration.find(ATC_NAME) } returns setOf(
            DrugInteractionConfig(
                ATC_NAME,
                false,
                listOf(DrugInteraction(DrugInteraction.Type.INDUCER, DrugInteraction.Strength.STRONG, "cyp_gene")),
                listOf(DrugInteraction(DrugInteraction.Type.INDUCER, DrugInteraction.Strength.STRONG, "bcrp_gene"))
            )
        )

        val result = extractor.extract(ehrPatientRecord)
        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).containsExactly(
            medication.copy(
                qtProlongatingRisk = QTProlongatingRisk.KNOWN,
                cypInteractions = listOf(DrugInteraction(DrugInteraction.Type.INDUCER, DrugInteraction.Strength.STRONG, "cyp_gene")),
            )
        )
    }

    @Test
    fun `Should default CYP and QT when no config found`() {
        every { qtProlongatingRiskCuration.find(ATC_NAME) } returns emptySet()
        every { drugInteractionCuration.find(ATC_NAME) } returns emptySet()
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
        every { drugInteractionCuration.find(MEDICATION_NAME) } returns emptySet()
        assertThrows(java.lang.IllegalStateException::class.java) {
            extractor.extract(ehrPatientRecord.copy(medications = listOf(providedMedication.copy(atcCode = null))))
        }
    }

    private fun noAtcLookupTest(modifiedMedication: ProvidedMedication, expected: Medication) {
        every { qtProlongatingRiskCuration.find(MEDICATION_NAME) } returns emptySet()
        every { drugInteractionCuration.find(MEDICATION_NAME) } returns emptySet()
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