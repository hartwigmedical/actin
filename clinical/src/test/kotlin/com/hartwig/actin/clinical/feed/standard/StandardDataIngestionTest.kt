package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.clinical.CurationRequirement
import com.hartwig.actin.clinical.CurationResult
import com.hartwig.actin.clinical.PatientIngestionStatus
import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.curation.TestDrugInteractionsFactory
import com.hartwig.actin.clinical.curation.TestQtProlongatingFactory
import com.hartwig.actin.clinical.feed.standard.extraction.StandardBloodTransfusionExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardBodyHeightExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardBodyWeightExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardClinicalStatusExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardIntolerancesExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardLabValuesExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardMedicationExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardOncologicalHistoryExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardPatientDetailsExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardPriorIHCTestExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardComorbidityExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardPriorPrimariesExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardPriorSequencingTestExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSurgeryExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardToxicityExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardTumorDetailsExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardVitalFunctionsExtractor
import com.hartwig.actin.clinical.feed.tumor.TumorStageDeriver
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.doid.config.DoidManualConfig
import com.hartwig.actin.icd.TestIcdFactory
import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val INPUT_JSON = resourceOnClasspath("feed/standard/input")
private val OUTPUT_RECORD_JSON = resourceOnClasspath("feed/standard/output/ACTN01029999.clinical.json")

class StandardDataIngestionTest {

    @Test
    fun `Should load EHR data from json and convert to clinical record`() {
        val doidModel = TestDoidModelFactory.createWithDoidManualConfig(
            DoidManualConfig(
                emptySet(),
                emptySet(),
                mapOf(
                    "299" to CurationDoidValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID,
                    "3908" to CurationDoidValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID,
                    "10286" to CurationDoidValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID,
                    "0050933" to CurationDoidValidator.DISEASE_OF_CELLULAR_PROLIFERATION_DOID,
                    "5082" to CurationDoidValidator.DISEASE_DOID,
                    "11335" to CurationDoidValidator.DISEASE_DOID,
                    "0060500" to CurationDoidValidator.DISEASE_DOID,
                    "0081062" to CurationDoidValidator.DISEASE_DOID,
                    "0040046" to CurationDoidValidator.DISEASE_DOID
                ),
                emptySet()
            )
        )
        val treatmentDatabase = TestTreatmentDatabaseFactory.createProper()
        val curationDatabase = CurationDatabaseContext.create(
            CURATION_DIRECTORY,
            CurationDoidValidator(doidModel),
            TestIcdFactory.createTestModel(),
            TestTreatmentDatabaseFactory.createProper()
        )
        val feed = StandardDataIngestion(
            directory = INPUT_JSON,
            medicationExtractor = StandardMedicationExtractor(
                atcModel = TestAtcFactory.createProperAtcModel(),
                drugInteractionsDatabase = TestDrugInteractionsFactory.createProper(),
                qtProlongatingDatabase = TestQtProlongatingFactory.createProper(),
                treatmentDatabase = treatmentDatabase
            ),
            surgeryExtractor = StandardSurgeryExtractor(curationDatabase.surgeryNameCuration),
            intolerancesExtractor = StandardIntolerancesExtractor(curationDatabase.comorbidityCuration),
            vitalFunctionsExtractor = StandardVitalFunctionsExtractor(),
            bloodTransfusionExtractor = StandardBloodTransfusionExtractor(),
            labValuesExtractor = StandardLabValuesExtractor(curationDatabase.laboratoryTranslation),
            comorbidityExtractor = StandardComorbidityExtractor(
                curationDatabase.comorbidityCuration
            ),
            toxicityExtractor = StandardToxicityExtractor(curationDatabase.comorbidityCuration),
            treatmentHistoryExtractor = StandardOncologicalHistoryExtractor(
                curationDatabase.treatmentHistoryEntryCuration
            ),
            clinicalStatusExtractor = StandardClinicalStatusExtractor(curationDatabase.ecgCuration),

            tumorDetailsExtractor = StandardTumorDetailsExtractor(
                curationDatabase.primaryTumorCuration,
                curationDatabase.lesionLocationCuration,
                TumorStageDeriver.create(doidModel)
            ),
            secondPrimaryExtractor = StandardPriorPrimariesExtractor(curationDatabase.secondPrimaryCuration),
            patientDetailsExtractor = StandardPatientDetailsExtractor(),
            bodyWeightExtractor = StandardBodyWeightExtractor(),
            bodyHeightExtractor = StandardBodyHeightExtractor(),
            ihcTestExtractor = StandardPriorIHCTestExtractor(curationDatabase.molecularTestIhcCuration),
            sequencingTestExtractor = StandardPriorSequencingTestExtractor(curationDatabase.sequencingTestCuration),
            dataQualityMask = DataQualityMask()
        )
        val expected = ClinicalRecordJson.read(OUTPUT_RECORD_JSON)
        val result = feed.ingest()

        assertThat(curationDatabase.validate()).isEmpty()

        assertThat(result.size).isEqualTo(1)
        val patientResult = result.first()
        assertThat(patientResult.first.clinicalRecord).isEqualTo(expected)
        assertThat(patientResult.first.status).isEqualTo(PatientIngestionStatus.WARN_CURATION_REQUIRED)
        assertThat(patientResult.first.curationResults).containsExactlyInAnyOrder(
            CurationResult(
                categoryName = "Non Oncological History",
                requirements = listOf(
                    CurationRequirement(
                        feedInput = "aandoening van mitralis-, aorta- en tricuspidalisklep",
                        message = "Could not find non-oncological history config for input 'aandoening van mitralis-, aorta- en tricuspidalisklep'"
                    )
                )
            ),
            CurationResult(
                categoryName = "Complication",
                requirements = listOf(
                    CurationRequirement(
                        feedInput = "Uncurateable",
                        message = "Could not find complication config for input 'Uncurateable'"
                    )
                )
            ),
            CurationResult(
                categoryName = "Toxicity",
                requirements = listOf(CurationRequirement(feedInput = "Pain", message = "Could not find toxicity config for input 'Pain'"))
            ),
            CurationResult(
                categoryName = "Laboratory Translation",
                requirements = listOf(
                    CurationRequirement(
                        feedInput = "dc_NeutrGran | Neutrof. granulocyten",
                        message = "Could not find laboratory translation for lab value with code 'dc_NeutrGran' and name 'Neutrof. granulocyten'"
                    ),
                    CurationRequirement(
                        feedInput = "bg_O2Sgem | O2-Saturatie gemeten",
                        message = "Could not find laboratory translation for lab value with code 'bg_O2Sgem' and name 'O2-Saturatie gemeten'"
                    ),
                    CurationRequirement(
                        feedInput = "Plt | Trombocyten",
                        message = "Could not find laboratory translation for lab value with code 'Plt' and name 'Trombocyten'"
                    ),
                    CurationRequirement(
                        feedInput = "dc_Lymfo | Lymfocyten",
                        message = "Could not find laboratory translation for lab value with code 'dc_Lymfo' and name 'Lymfocyten'"
                    ),
                    CurationRequirement(
                        feedInput = "Hb | Hemoglobine",
                        message = "Could not find laboratory translation for lab value with code 'Hb' and name 'Hemoglobine'"
                    )
                ),
            ),
            CurationResult(
                categoryName = "Surgery Name",
                requirements = listOf(
                    CurationRequirement(
                        feedInput = "<CRYO Skelet door Radioloog",
                        message = "Could not find surgery config for input '<CRYO Skelet door Radioloog'"
                    )
                )
            )
        )
    }
}