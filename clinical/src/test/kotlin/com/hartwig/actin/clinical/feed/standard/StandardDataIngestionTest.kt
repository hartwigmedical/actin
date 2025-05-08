package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.curation.TestDrugInteractionsFactory
import com.hartwig.actin.clinical.curation.TestQtProlongatingFactory
import com.hartwig.actin.clinical.feed.standard.extraction.PathologyReportsExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardBloodTransfusionExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardBodyHeightExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardBodyWeightExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardClinicalStatusExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardComorbidityExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardIhcTestExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardLabValuesExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardMedicationExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardOncologicalHistoryExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardPatientDetailsExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardPriorPrimariesExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSequencingTestExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardSurgeryExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardTumorDetailsExtractor
import com.hartwig.actin.clinical.feed.standard.extraction.StandardVitalFunctionsExtractor
import com.hartwig.actin.clinical.feed.tumor.TumorStageDeriver
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationRequirement
import com.hartwig.actin.datamodel.clinical.ingestion.CurationResult
import com.hartwig.actin.datamodel.clinical.ingestion.PatientIngestionStatus
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
            vitalFunctionsExtractor = StandardVitalFunctionsExtractor(),
            bloodTransfusionExtractor = StandardBloodTransfusionExtractor(),
            labValuesExtractor = StandardLabValuesExtractor(curationDatabase.labMeasurementCuration),
            comorbidityExtractor = StandardComorbidityExtractor(
                curationDatabase.comorbidityCuration
            ),
            treatmentHistoryExtractor = StandardOncologicalHistoryExtractor(
                curationDatabase.treatmentHistoryEntryCuration
            ),
            clinicalStatusExtractor = StandardClinicalStatusExtractor(),
            tumorDetailsExtractor = StandardTumorDetailsExtractor(
                curationDatabase.primaryTumorCuration,
                curationDatabase.lesionLocationCuration,
                TumorStageDeriver.create(doidModel)
            ),
            secondPrimaryExtractor = StandardPriorPrimariesExtractor(curationDatabase.priorPrimaryCuration),

            patientDetailsExtractor = StandardPatientDetailsExtractor(),
            bodyWeightExtractor = StandardBodyWeightExtractor(),
            bodyHeightExtractor = StandardBodyHeightExtractor(),
            ihcTestExtractor = StandardIhcTestExtractor(curationDatabase.molecularTestIhcCuration),
            sequencingTestExtractor = StandardSequencingTestExtractor(
                curationDatabase.sequencingTestCuration,
                curationDatabase.sequencingTestResultCuration
            ),
            pathologyReportsExtractor = PathologyReportsExtractor()
        )
        val expected = ClinicalRecordJson.read(OUTPUT_RECORD_JSON)
        val result = feed.ingest()

        assertThat(curationDatabase.validate()).isEmpty()

        assertThat(result.size).isEqualTo(1)
        val patientResult = result.first()
        assertThat(patientResult.first).isEqualTo(expected)
        assertThat(patientResult.second.status).isEqualTo(PatientIngestionStatus.WARN)
        assertThat(patientResult.second.curationResults).containsExactlyInAnyOrder(
            CurationResult(
                category = CurationCategory.NON_ONCOLOGICAL_HISTORY,
                requirements = listOf(
                    CurationRequirement(
                        feedInput = "aandoening van mitralis-, aorta- en tricuspidalisklep",
                        message = "Could not find non-oncological history config for input 'aandoening van mitralis-, aorta- en tricuspidalisklep'"
                    )
                )
            ),
            CurationResult(
                category = CurationCategory.COMPLICATION,
                requirements = listOf(
                    CurationRequirement(
                        feedInput = "Uncurateable",
                        message = "Could not find complication config for input 'Uncurateable'"
                    )
                )
            ),
            CurationResult(
                category = CurationCategory.TOXICITY,
                requirements = listOf(CurationRequirement(feedInput = "Pain", message = "Could not find toxicity config for input 'Pain'"))
            ),
            CurationResult(
                category = CurationCategory.LAB_MEASUREMENT,
                requirements = listOf(
                    CurationRequirement(
                        feedInput = "dc_NeutrGran | Neutrof. granulocyten",
                        message = "Could not find lab measurement config for input 'dc_NeutrGran | Neutrof. granulocyten'"
                    )
                ),
            ),
            CurationResult(
                category = CurationCategory.SURGERY_NAME,
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