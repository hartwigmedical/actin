package com.hartwig.actin.clinical.feed.standard

import com.google.common.io.Resources
import com.hartwig.actin.TestTreatmentDatabaseFactory
import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.doid.TestDoidModelFactory
import com.hartwig.actin.doid.config.DoidManualConfig
import org.junit.Test

@Suppress("UnstableApiUsage")
val INPUT_JSON: String = Resources.getResource("feed.standard/input").path

class StandardEhrIngestionTest {

    @Test
    fun `Should load EHR data from json and convert to clinical record`() {
        val curationDatabase = CurationDatabaseContext.create(
            CURATION_DIRECTORY,
            CurationDoidValidator(
                TestDoidModelFactory.createWithDoidManualConfig(
                    DoidManualConfig(emptySet(), emptySet(), emptyMap())
                )
            ),
            TestTreatmentDatabaseFactory.createProper()
        )
        val feed = StandardEhrIngestion(
            directory = INPUT_JSON,
            medicationExtractor = EhrMedicationExtractor(
                atcModel = TestAtcFactory.createProperAtcModel(),
                qtPrologatingRiskCuration = curationDatabase.qtProlongingCuration,
                cypInteractionCuration = curationDatabase.cypInteractionCuration,
                dosageCuration = curationDatabase.medicationDosageCuration
            ),
            surgeryExtractor = EhrSurgeryExtractor(),
            toxicityExtractor = EhrToxicityExtractor(curationDatabase.toxicityCuration),
            vitalFunctionsExtractor = EhrVitalFunctionsExtractor(),
            priorOtherConditionsExtractor = EhrPriorOtherConditionsExtractor(curationDatabase.nonOncologicalHistoryCuration),
            intolerancesExtractor = EhrIntolerancesExtractor(TestAtcFactory.createProperAtcModel(), curationDatabase.intoleranceCuration),
            complicationExtractor = EhrComplicationExtractor(curationDatabase.complicationCuration),
            treatmentHistoryExtractor = EhrTreatmentHistoryExtractor(TestTreatmentDatabaseFactory.createProper()),
            secondPrimaryExtractor = EhrSecondPrimariesExtractor(),
            patientDetailsExtractor = EhrPatientDetailsExtractor(),
            tumorDetailsExtractor = EhrTumorDetailsExtractor(curationDatabase.primaryTumorCuration),
            labValuesExtractor = EhrLabValuesExtractor(curationDatabase.laboratoryTranslation),
            clinicalStatusExtractor = EhrClinicalStatusExtractor(),
            bodyWeightExtractor = EhrBodyWeightExtractor(),
            bloodTransfusionExtractor = EhrBloodTransfusionExtractor()
        )
        ClinicalRecordJson.write(
            listOf( feed.ingest()[0].first.clinicalRecord),
            "/Users/pwolfe/Code/actin/clinical/src/test/resources/feed.standard/output"
        )
    }
}