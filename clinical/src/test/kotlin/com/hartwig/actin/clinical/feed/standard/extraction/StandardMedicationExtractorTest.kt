package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.DrugInteractionsDatabase
import com.hartwig.actin.clinical.QtProlongatingDatabase
import com.hartwig.actin.datamodel.clinical.AtcClassification
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.Dosage
import com.hartwig.actin.datamodel.clinical.DrugInteraction
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.QTProlongatingRisk
import com.hartwig.actin.datamodel.clinical.provided.ProvidedMedication
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientDetail
import com.hartwig.actin.datamodel.clinical.provided.ProvidedPatientRecord
import com.hartwig.actin.datamodel.clinical.provided.ProvidedTumorDetail
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val MEDICATION_NAME = "medication_name"
private const val ATC_NAME = "atc_name"

class StandardMedicationExtractorTest {

    private val atcModel = mockk<AtcModel>()
    private val qtProlongatingDatabase = mockk<QtProlongatingDatabase>()
    private val drugInteractionsDatabase = mockk<DrugInteractionsDatabase>()
    private val atcClassification = atcClassification()
    private val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
    private val extractor = StandardMedicationExtractor(atcModel, drugInteractionsDatabase, qtProlongatingDatabase, treatmentDatabase)
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
        cypInteractions = emptyList(),
        transporterInteractions = emptyList()
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
    fun `Should curate QT and drug interactions and extract medication`() {
        every { qtProlongatingDatabase.annotateWithQTProlongating(ATC_NAME) } returns QTProlongatingRisk.KNOWN
        every { drugInteractionsDatabase.annotateWithCypInteractions(ATC_NAME) } returns
                listOf(
                    DrugInteraction(
                        DrugInteraction.Type.INDUCER,
                        DrugInteraction.Strength.STRONG,
                        DrugInteraction.Group.CYP,
                        "cyp_gene"
                    )
                )

        every { drugInteractionsDatabase.annotateWithTransporterInteractions(ATC_NAME) } returns
                listOf(
                    DrugInteraction(
                        DrugInteraction.Type.INDUCER,
                        DrugInteraction.Strength.STRONG,
                        DrugInteraction.Group.TRANSPORTER,
                        "bcrp_gene"
                    )
                )

        val result = extractor.extract(ehrPatientRecord)
        assertThat(result.evaluation.warnings).isEmpty()
        assertThat(result.extracted).containsExactly(
            medication.copy(
                qtProlongatingRisk = QTProlongatingRisk.KNOWN,
                cypInteractions = listOf(
                    DrugInteraction(
                        DrugInteraction.Type.INDUCER,
                        DrugInteraction.Strength.STRONG,
                        DrugInteraction.Group.CYP,
                        "cyp_gene"
                    )
                ),
                transporterInteractions = listOf(
                    DrugInteraction(
                        DrugInteraction.Type.INDUCER,
                        DrugInteraction.Strength.STRONG,
                        DrugInteraction.Group.TRANSPORTER,
                        "bcrp_gene"
                    )
                )
            )
        )
    }

    @Test
    fun `Should default drug interactions and QT when no config found`() {
        every { qtProlongatingDatabase.annotateWithQTProlongating(ATC_NAME) } returns QTProlongatingRisk.NONE
        every { drugInteractionsDatabase.annotateWithCypInteractions(ATC_NAME) } returns emptyList()
        every { drugInteractionsDatabase.annotateWithTransporterInteractions(ATC_NAME) } returns emptyList()
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

    private fun noAtcLookupTest(modifiedMedication: ProvidedMedication, expected: Medication) {
        every { qtProlongatingDatabase.annotateWithQTProlongating(MEDICATION_NAME) } returns QTProlongatingRisk.NONE
        every { drugInteractionsDatabase.annotateWithCypInteractions(MEDICATION_NAME) } returns emptyList()
        every { drugInteractionsDatabase.annotateWithTransporterInteractions(MEDICATION_NAME) } returns emptyList()
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