package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.CypInteractionConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.clinical.datamodel.AtcClassification
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.Dosage
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val MEDICATION_NAME = "medication_name"
private const val ATC_NAME = "atc_name"

class EhrMedicationExtractorTest {

    private val atcModel = mockk<AtcModel>()
    private val qtProlongatingRiskCuration = mockk<CurationDatabase<QTProlongatingConfig>>()
    private val cypInteractionCuration = mockk<CurationDatabase<CypInteractionConfig>>()
    private val atcClassification = atcClassification()
    private val extractor = EhrMedicationExtractor(atcModel, qtProlongatingRiskCuration, cypInteractionCuration)
    private val ehrMedication = EhrMedication(
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
        qtProlongatingRisk = QTProlongatingRisk.UNKNOWN,
        cypInteractions = emptyList()
    )
    private val ehrPatientRecord = EhrPatientRecord(
        patientDetails = EhrPatientDetail(1980, "MALE", LocalDate.of(2024, 2, 26), "hashedId"),
        medications = listOf(ehrMedication),
        tumorDetails = EhrTumorDetail(
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
        noAtcLookupTest(ehrMedication.copy(isTrial = true), medication.copy(name = MEDICATION_NAME, atc = null, isTrialMedication = true))
    }

    @Test
    fun `Should not look up ATC code when medication is self care`() {
        noAtcLookupTest(
            ehrMedication.copy(isSelfCare = true),
            medication.copy(name = MEDICATION_NAME, atc = null, isSelfCare = true)
        )
    }

    private fun noAtcLookupTest(modifiedMedication: EhrMedication, expected: Medication) {
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