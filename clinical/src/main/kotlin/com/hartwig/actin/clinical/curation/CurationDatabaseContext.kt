package com.hartwig.actin.clinical.curation

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.UnusedCurationConfig
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.curation.config.ComplicationConfigFactory
import com.hartwig.actin.clinical.curation.config.ECGConfig
import com.hartwig.actin.clinical.curation.config.ECGConfigFactory
import com.hartwig.actin.clinical.curation.config.IHCTestConfig
import com.hartwig.actin.clinical.curation.config.IHCTestConfigFactory
import com.hartwig.actin.clinical.curation.config.InfectionConfig
import com.hartwig.actin.clinical.curation.config.InfectionConfigFactory
import com.hartwig.actin.clinical.curation.config.IntoleranceConfigFactory
import com.hartwig.actin.clinical.curation.config.LabMeasurementConfig
import com.hartwig.actin.clinical.curation.config.LabMeasurementConfigFactory
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.LesionLocationConfigFactory
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfigFactory
import com.hartwig.actin.clinical.curation.config.MedicationNameConfig
import com.hartwig.actin.clinical.curation.config.MedicationNameConfigFactory
import com.hartwig.actin.clinical.curation.config.OtherConditionConfigFactory
import com.hartwig.actin.clinical.curation.config.PeriodBetweenUnitConfig
import com.hartwig.actin.clinical.curation.config.PeriodBetweenUnitConfigFactory
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfigFactory
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfigFactory
import com.hartwig.actin.clinical.curation.config.SequencingTestConfig
import com.hartwig.actin.clinical.curation.config.SequencingTestConfigFactory
import com.hartwig.actin.clinical.curation.config.SurgeryNameConfig
import com.hartwig.actin.clinical.curation.config.SurgeryNameConfigFactory
import com.hartwig.actin.clinical.curation.config.ToxicityConfigFactory
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfigFactory
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.AdministrationRouteTranslationFactory
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslationFactory
import com.hartwig.actin.clinical.curation.translation.DosageUnitTranslationFactory
import com.hartwig.actin.clinical.curation.translation.ToxicityTranslationFactory
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.curation.translation.TranslationDatabaseReader
import com.hartwig.actin.icd.IcdModel

data class CurationDatabaseContext(
    val primaryTumorCuration: CurationDatabase<PrimaryTumorConfig>,
    val treatmentHistoryEntryCuration: CurationDatabase<TreatmentHistoryEntryConfig>,
    val secondPrimaryCuration: CurationDatabase<SecondPrimaryConfig>,
    val lesionLocationCuration: CurationDatabase<LesionLocationConfig>,
    val comorbidityCuration: CurationDatabase<ComorbidityConfig>,
    val ecgCuration: CurationDatabase<ECGConfig>,
    val infectionCuration: CurationDatabase<InfectionConfig>,
    val periodBetweenUnitCuration: CurationDatabase<PeriodBetweenUnitConfig>,
    val molecularTestIhcCuration: CurationDatabase<IHCTestConfig>,
    val molecularTestPdl1Curation: CurationDatabase<IHCTestConfig>,
    val sequencingTestCuration: CurationDatabase<SequencingTestConfig>,
    val medicationNameCuration: CurationDatabase<MedicationNameConfig>,
    val medicationDosageCuration: CurationDatabase<MedicationDosageConfig>,
    val administrationRouteTranslation: TranslationDatabase<String>,
    val laboratoryCuration: CurationDatabase<LabMeasurementConfig>,
    val toxicityTranslation: TranslationDatabase<String>,
    val bloodTransfusionTranslation: TranslationDatabase<String>,
    val dosageUnitTranslation: TranslationDatabase<String>,
    val surgeryNameCuration: CurationDatabase<SurgeryNameConfig>,
) {
    fun allUnusedConfig(extractionEvaluations: List<CurationExtractionEvaluation>): Set<UnusedCurationConfig> {
        val unusedCurationConfigs = listOf(
            primaryTumorCuration,
            treatmentHistoryEntryCuration,
            secondPrimaryCuration,
            lesionLocationCuration,
            comorbidityCuration,
            ecgCuration,
            infectionCuration,
            periodBetweenUnitCuration,
            molecularTestIhcCuration,
            molecularTestPdl1Curation,
            sequencingTestCuration,
            medicationNameCuration,
            medicationDosageCuration,
            surgeryNameCuration,
            laboratoryCuration
        ).flatMap { it.reportUnusedConfig(extractionEvaluations) }.toSet()

        val unusedTranslations = listOf(
            administrationRouteTranslation,
            toxicityTranslation,
            dosageUnitTranslation
        ).flatMap { it.reportUnusedTranslations(extractionEvaluations) }

        return unusedCurationConfigs + unusedTranslations
    }

    fun validate() = listOf(
        primaryTumorCuration.validationErrors,
        treatmentHistoryEntryCuration.validationErrors,
        secondPrimaryCuration.validationErrors,
        lesionLocationCuration.validationErrors,
        comorbidityCuration.validationErrors,
        ecgCuration.validationErrors,
        infectionCuration.validationErrors,
        periodBetweenUnitCuration.validationErrors,
        molecularTestIhcCuration.validationErrors,
        molecularTestPdl1Curation.validationErrors,
        sequencingTestCuration.validationErrors,
        medicationNameCuration.validationErrors,
        medicationDosageCuration.validationErrors,
        surgeryNameCuration.validationErrors,
        laboratoryCuration.validationErrors
    ).flatten().toSet()


    companion object {
        fun create(
            curationDir: String,
            curationDoidValidator: CurationDoidValidator,
            icdModel: IcdModel,
            treatmentDatabase: TreatmentDatabase
        ) = CurationDatabaseContext(
            primaryTumorCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.PRIMARY_TUMOR_TSV,
                PrimaryTumorConfigFactory(curationDoidValidator),
                CurationCategory.PRIMARY_TUMOR
            ) { it.primaryTumorEvaluatedInputs },
            treatmentHistoryEntryCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.ONCOLOGICAL_HISTORY_TSV,
                TreatmentHistoryEntryConfigFactory(treatmentDatabase),
                CurationCategory.ONCOLOGICAL_HISTORY
            ) { it.treatmentHistoryEntryEvaluatedInputs },
            secondPrimaryCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.SECOND_PRIMARY_TSV,
                SecondPrimaryConfigFactory(curationDoidValidator),
                CurationCategory.SECOND_PRIMARY
            ) { it.secondPrimaryEvaluatedInputs },
            lesionLocationCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.LESION_LOCATION_TSV,
                LesionLocationConfigFactory(),
                CurationCategory.LESION_LOCATION
            ) { it.lesionLocationEvaluatedInputs },
            comorbidityCuration = createComorbidityCurationDatabase(curationDir, icdModel),
            ecgCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.ECG_TSV,
                ECGConfigFactory(),
                CurationCategory.ECG
            ) { it.ecgEvaluatedInputs },
            infectionCuration = CurationDatabaseReader.read(
                curationDir, CurationDatabaseReader.INFECTION_TSV, InfectionConfigFactory(), CurationCategory.INFECTION
            ) { it.infectionEvaluatedInputs },
            periodBetweenUnitCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.PERIOD_BETWEEN_UNIT_TSV,
                PeriodBetweenUnitConfigFactory(),
                CurationCategory.PERIOD_BETWEEN_UNIT_INTERPRETATION
            ) { it.periodBetweenUnitEvaluatedInputs },
            molecularTestIhcCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.MOLECULAR_TEST_IHC_TSV,
                IHCTestConfigFactory(CurationCategory.MOLECULAR_TEST_IHC),
                CurationCategory.MOLECULAR_TEST_IHC
            ) { it.molecularTestEvaluatedInputs },
            molecularTestPdl1Curation = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.MOLECULAR_TEST_PDL1_TSV,
                IHCTestConfigFactory(CurationCategory.MOLECULAR_TEST_PDL1),
                CurationCategory.MOLECULAR_TEST_PDL1
            ) { it.molecularTestEvaluatedInputs },
            sequencingTestCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.SEQUENCING_TEST_TSV,
                SequencingTestConfigFactory(),
                CurationCategory.SEQUENCING_TEST
            ) { it.sequencingTestEvaluatedInputs },
            medicationNameCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.MEDICATION_NAME_TSV,
                MedicationNameConfigFactory(),
                CurationCategory.MEDICATION_NAME
            ) { it.medicationNameEvaluatedInputs },
            medicationDosageCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.MEDICATION_DOSAGE_TSV,
                MedicationDosageConfigFactory(),
                CurationCategory.MEDICATION_DOSAGE
            ) { it.medicationDosageEvaluatedInputs },
            administrationRouteTranslation = TranslationDatabaseReader.read(
                curationDir,
                TranslationDatabaseReader.ADMINISTRATION_ROUTE_TRANSLATION_TSV,
                AdministrationRouteTranslationFactory(),
                CurationCategory.ADMINISTRATION_ROUTE_TRANSLATION
            ) { it.administrationRouteEvaluatedInputs },
            laboratoryCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.LABORATORY_TSV,
                LabMeasurementConfigFactory(),
                CurationCategory.LABORATORY
            ) { it.laboratoryEvaluatedInputs },
            toxicityTranslation = TranslationDatabaseReader.read(
                curationDir,
                TranslationDatabaseReader.TOXICITY_TRANSLATION_TSV,
                ToxicityTranslationFactory(),
                CurationCategory.TOXICITY_TRANSLATION,
            ) { it.toxicityTranslationEvaluatedInputs },
            bloodTransfusionTranslation = TranslationDatabaseReader.read(
                curationDir,
                TranslationDatabaseReader.BLOOD_TRANSFUSION_TRANSLATION_TSV,
                BloodTransfusionTranslationFactory(),
                CurationCategory.BLOOD_TRANSFUSION_TRANSLATION
            ) { emptySet() },
            dosageUnitTranslation = TranslationDatabaseReader.read(
                curationDir,
                TranslationDatabaseReader.DOSAGE_UNIT_TRANSLATION_TSV,
                DosageUnitTranslationFactory(),
                CurationCategory.DOSAGE_UNIT_TRANSLATION
            ) { it.dosageUnitEvaluatedInputs },
            surgeryNameCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.SURGERY_NAME_TSV,
                SurgeryNameConfigFactory(),
                CurationCategory.SURGERY_NAME
            ) { it.surgeryTranslationEvaluatedInputs }
        )

        private fun createComorbidityCurationDatabase(curationDir: String, icdModel: IcdModel): CurationDatabase<ComorbidityConfig> {
            val otherConditionDatabase = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.NON_ONCOLOGICAL_HISTORY_TSV,
                OtherConditionConfigFactory(icdModel),
                CurationCategory.COMORBIDITY
            ) { it.comorbidityEvaluatedInputs }

            val complicationCurationDatabase = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.COMPLICATION_TSV,
                ComplicationConfigFactory(icdModel),
                CurationCategory.COMORBIDITY
            ) { it.comorbidityEvaluatedInputs }

            val intoleranceCurationDatabase = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.INTOLERANCE_TSV,
                IntoleranceConfigFactory(icdModel),
                CurationCategory.COMORBIDITY
            ) { it.comorbidityEvaluatedInputs }

            val toxicityCurationDatabase = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.TOXICITY_TSV,
                ToxicityConfigFactory(icdModel),
                CurationCategory.COMORBIDITY
            ) { it.comorbidityEvaluatedInputs }

            return otherConditionDatabase + complicationCurationDatabase + intoleranceCurationDatabase + toxicityCurationDatabase
        }
    }
}