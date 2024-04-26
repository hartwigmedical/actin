package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.clinical.CurationRequirement
import com.hartwig.actin.clinical.CurationResult
import com.hartwig.actin.clinical.PatientIngestionStatus
import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.feed.tumor.TumorStageDeriver
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.doid.config.DoidManualConfig
import com.hartwig.actin.testutil.ResourceLocator.resourceOnClasspath
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val INPUT_JSON = resourceOnClasspath("feed/standard/input")
private val OUTPUT_RECORD_JSON = resourceOnClasspath("feed/standard/output/ACTN01029999.clinical.json")

class StandardEhrIngestionTest {

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
                    "0081062" to CurationDoidValidator.DISEASE_DOID
                )
            )
        )
        val curationDatabase = CurationDatabaseContext.create(
            CURATION_DIRECTORY,
            CurationDoidValidator(doidModel),
            TestTreatmentDatabaseFactory.createProper()
        )
        val feed = StandardEhrIngestion(
            directory = INPUT_JSON,
            medicationExtractor = EhrMedicationExtractor(
                atcModel = TestAtcFactory.createProperAtcModel(),
                qtProlongatingRiskCuration = curationDatabase.qtProlongingCuration,
                cypInteractionCuration = curationDatabase.cypInteractionCuration
            ),
            surgeryExtractor = EhrSurgeryExtractor(),
            toxicityExtractor = EhrToxicityExtractor(curationDatabase.toxicityCuration),
            vitalFunctionsExtractor = EhrVitalFunctionsExtractor(),
            priorOtherConditionsExtractor = EhrPriorOtherConditionsExtractor(
                curationDatabase.nonOncologicalHistoryCuration,
                curationDatabase.treatmentHistoryEntryCuration
            ),
            intolerancesExtractor = EhrIntolerancesExtractor(
                TestAtcFactory.createProperAtcModel(),
                curationDatabase.intoleranceCuration
            ),
            complicationExtractor = EhrComplicationExtractor(curationDatabase.complicationCuration),
            treatmentHistoryExtractor = EhrTreatmentHistoryExtractor(
                curationDatabase.treatmentHistoryEntryCuration,
                curationDatabase.nonOncologicalHistoryCuration
            ),
            secondPrimaryExtractor = EhrPriorPrimariesExtractor(curationDatabase.secondPrimaryCuration),

            patientDetailsExtractor = EhrPatientDetailsExtractor(),
            tumorDetailsExtractor = EhrTumorDetailsExtractor(
                curationDatabase.primaryTumorCuration,
                curationDatabase.lesionLocationCuration,
                TumorStageDeriver.create(doidModel)
            ),
            labValuesExtractor = EhrLabValuesExtractor(curationDatabase.laboratoryTranslation),
            clinicalStatusExtractor = EhrClinicalStatusExtractor(),
            bodyWeightExtractor = EhrBodyWeightExtractor(),
            bodyHeightExtractor = EhrBodyHeightExtractor(),
            bloodTransfusionExtractor = EhrBloodTransfusionExtractor(),
            molecularTestExtractor = EhrMolecularTestExtractor(curationDatabase.molecularTestIhcCuration),
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
                categoryName = "Intolerance",
                requirements = listOf(
                    CurationRequirement(
                        feedInput = "MORFINE",
                        message = "Could not find intolerance config for input 'MORFINE'"
                    ), CurationRequirement(feedInput = "Nikkel", message = "Could not find intolerance config for input 'Nikkel'")
                )
            ),
            CurationResult(
                categoryName = "Oncological History",
                requirements = listOf(
                    CurationRequirement(
                        feedInput = "aandoening van mitralis-, aorta- en tricuspidalisklep",
                        message = "Could not find treatment history config for input 'aandoening van mitralis-, aorta- en tricuspidalisklep'"
                    )
                )
            )
        )
    }
}