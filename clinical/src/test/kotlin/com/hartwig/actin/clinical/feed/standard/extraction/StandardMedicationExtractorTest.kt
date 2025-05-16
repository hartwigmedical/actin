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
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationWarning
import com.hartwig.feed.datamodel.FeedDosage
import com.hartwig.feed.datamodel.FeedMedication
import com.hartwig.feed.datamodel.FeedPatientDetail
import com.hartwig.feed.datamodel.FeedPatientRecord
import com.hartwig.feed.datamodel.FeedTumorDetail
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val MEDICATION_NAME = "medication_name"
private const val ATC_NAME = "atc_name"

class StandardMedicationExtractorTest {

    private val atcModel = mockk<AtcModel>()
    private val qtProlongatingDatabase = mockk<QtProlongatingDatabase>()
    private val drugInteractionsDatabase = mockk<DrugInteractionsDatabase>()
    private val atcClassification = atcClassification()
    private val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
    private val extractor = StandardMedicationExtractor(atcModel, drugInteractionsDatabase, qtProlongatingDatabase, treatmentDatabase)
    private val providedMedication = FeedMedication(
        name = MEDICATION_NAME,
        atcCode = "atc",
        administrationRoute = "route",
        dosage = FeedDosage(
            1.0,
            1.0,
            dosageUnit = "unit",
            frequency = 2.0,
            frequencyUnit = "unit",
            periodBetweenValue = 3.0,
            periodBetweenUnit = "unit",
            ifNeeded = false,
        ),
        startDate = LocalDate.of(2024, 2, 26),
        endDate = LocalDate.of(2024, 2, 26),
        isTrial = false,
        isSelfCare = false,
        dosageInstruction = null
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
    private val ehrPatientRecord = FeedPatientRecord(
        patientDetails = FeedPatientDetail(
            birthYear = 1980,
            gender = "MALE",
            registrationDate = LocalDate.of(2024, 2, 26),
            patientId = "hashedId",
            hartwigMolecularDataExpected = false
        ),
        medications = listOf(providedMedication),
        tumorDetails = FeedTumorDetail(
            diagnosisDate = LocalDate.of(2024, 2, 23),
            tumorLocation = "tumorLocation",
            tumorType = "tumorType",
            lesions = null,
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
        noAtcLookupTest(
            providedMedication.copy(name = "drug (STUDIE)", atcCode = "TRIAL_CODE", isTrial = false),
            medication.copy(name = "drug", atc = null, isTrialMedication = true)
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
    fun `Should trigger warning when anticancer medication cannot be found in drug database`() {
        every { qtProlongatingDatabase.annotateWithQTProlongating(any()) } returns QTProlongatingRisk.NONE
        every { drugInteractionsDatabase.annotateWithCypInteractions(any()) } returns emptyList()
        every { drugInteractionsDatabase.annotateWithTransporterInteractions(any()) } returns emptyList()
        val result = extractor.extract(ehrPatientRecord.copy(medications = listOf(providedMedication.copy(name = "drug (STUDIE)", atcCode = "L01ZZ"))))
        assertThat(result.evaluation.warnings).containsOnly(
            CurationWarning(
                "hashedId",
                CurationCategory.MEDICATION_NAME,
                "drug",
                "Anti cancer medication or supportive trial medication drug with ATC code L01ZZ found which is not present in drug database. " +
                        "Please add the missing drug to drug database"
            )
        )
    }

    @Test
    fun `Should not trigger warning for unspecified trial medication`() {
        every { qtProlongatingDatabase.annotateWithQTProlongating(any()) } returns QTProlongatingRisk.NONE
        every { drugInteractionsDatabase.annotateWithCypInteractions(any()) } returns emptyList()
        every { drugInteractionsDatabase.annotateWithTransporterInteractions(any()) } returns emptyList()
        val result = extractor.extract(
            ehrPatientRecord.copy(medications = listOf(providedMedication.copy(name = "orale studiemedicatie", atcCode = "L01ZZ")))
        )
        assertThat(result.evaluation.warnings).isEmpty()
    }

    private fun noAtcLookupTest(modifiedMedication: FeedMedication, expected: Medication) {
        every { qtProlongatingDatabase.annotateWithQTProlongating(any()) } returns QTProlongatingRisk.NONE
        every { drugInteractionsDatabase.annotateWithCypInteractions(any()) } returns emptyList()
        every { drugInteractionsDatabase.annotateWithTransporterInteractions(any()) } returns emptyList()
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