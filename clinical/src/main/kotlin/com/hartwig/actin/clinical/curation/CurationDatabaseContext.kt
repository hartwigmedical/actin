package com.hartwig.actin.clinical.curation

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.UnusedCurationConfig
import com.hartwig.actin.clinical.curation.config.ComplicationConfig
import com.hartwig.actin.clinical.curation.config.ComplicationConfigFactory
import com.hartwig.actin.clinical.curation.config.DrugInteractionConfig
import com.hartwig.actin.clinical.curation.config.DrugInteractionConfigFactory
import com.hartwig.actin.clinical.curation.config.ECGConfig
import com.hartwig.actin.clinical.curation.config.ECGConfigFactory
import com.hartwig.actin.clinical.curation.config.IHCTestConfig
import com.hartwig.actin.clinical.curation.config.IHCTestConfigFactory
import com.hartwig.actin.clinical.curation.config.InfectionConfig
import com.hartwig.actin.clinical.curation.config.InfectionConfigFactory
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig
import com.hartwig.actin.clinical.curation.config.IntoleranceConfigFactory
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.LesionLocationConfigFactory
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfigFactory
import com.hartwig.actin.clinical.curation.config.MedicationNameConfig
import com.hartwig.actin.clinical.curation.config.MedicationNameConfigFactory
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfigFactory
import com.hartwig.actin.clinical.curation.config.PeriodBetweenUnitConfig
import com.hartwig.actin.clinical.curation.config.PeriodBetweenUnitConfigFactory
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfigFactory
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfigFactory
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfigFactory
import com.hartwig.actin.clinical.curation.config.SequencingTestConfig
import com.hartwig.actin.clinical.curation.config.SequencingTestConfigFactory
import com.hartwig.actin.clinical.curation.config.SurgeryNameConfig
import com.hartwig.actin.clinical.curation.config.SurgeryNameConfigFactory
import com.hartwig.actin.clinical.curation.config.ToxicityConfig
import com.hartwig.actin.clinical.curation.config.ToxicityConfigFactory
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfigFactory
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.AdministrationRouteTranslationFactory
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslationFactory
import com.hartwig.actin.clinical.curation.translation.DosageUnitTranslationFactory
import com.hartwig.actin.clinical.curation.translation.LaboratoryIdentifiers
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslationFactory
import com.hartwig.actin.clinical.curation.translation.ToxicityTranslationFactory
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.curation.translation.TranslationDatabaseReader

data class CurationDatabaseContext(
    val primaryTumorCuration: CurationDatabase<PrimaryTumorConfig>,
    val treatmentHistoryEntryCuration: CurationDatabase<TreatmentHistoryEntryConfig>,
    val secondPrimaryCuration: CurationDatabase<SecondPrimaryConfig>,
    val lesionLocationCuration: CurationDatabase<LesionLocationConfig>,
    val nonOncologicalHistoryCuration: CurationDatabase<NonOncologicalHistoryConfig>,
    val ecgCuration: CurationDatabase<ECGConfig>,
    val infectionCuration: CurationDatabase<InfectionConfig>,
    val periodBetweenUnitCuration: CurationDatabase<PeriodBetweenUnitConfig>,
    val complicationCuration: CurationDatabase<ComplicationConfig>,
    val toxicityCuration: CurationDatabase<ToxicityConfig>,
    val molecularTestIhcCuration: CurationDatabase<IHCTestConfig>,
    val molecularTestPdl1Curation: CurationDatabase<IHCTestConfig>,
    val sequencingTestCuration: CurationDatabase<SequencingTestConfig>,
    val medicationNameCuration: CurationDatabase<MedicationNameConfig>,
    val medicationDosageCuration: CurationDatabase<MedicationDosageConfig>,
    val intoleranceCuration: CurationDatabase<IntoleranceConfig>,
    val drugInteractionCuration: CurationDatabase<DrugInteractionConfig>,
    val qtProlongingCuration: CurationDatabase<QTProlongatingConfig>,
    val administrationRouteTranslation: TranslationDatabase<String>,
    val laboratoryTranslation: TranslationDatabase<LaboratoryIdentifiers>,
    val toxicityTranslation: TranslationDatabase<String>,
    val bloodTransfusionTranslation: TranslationDatabase<String>,
    val dosageUnitTranslation: TranslationDatabase<String>,
    val surgeryNameCuration: CurationDatabase<SurgeryNameConfig>,
) {
    fun allUnusedConfig(extractionEvaluations: List<CurationExtractionEvaluation>): Set<UnusedCurationConfig> =
        setOf(
            primaryTumorCuration,
            treatmentHistoryEntryCuration,
            secondPrimaryCuration,
            lesionLocationCuration,
            nonOncologicalHistoryCuration,
            ecgCuration,
            infectionCuration,
            periodBetweenUnitCuration,
            complicationCuration,
            toxicityCuration,
            molecularTestIhcCuration,
            molecularTestPdl1Curation,
            sequencingTestCuration,
            medicationNameCuration,
            medicationDosageCuration,
            intoleranceCuration,
            surgeryNameCuration
        ).flatMap { it.reportUnusedConfig(extractionEvaluations) }.toSet() + listOf(
            laboratoryTranslation,
            administrationRouteTranslation,
            toxicityTranslation,
            dosageUnitTranslation
        ).flatMap { it.reportUnusedTranslations(extractionEvaluations) }

    fun validate() = (primaryTumorCuration.validationErrors +
            treatmentHistoryEntryCuration.validationErrors +
            secondPrimaryCuration.validationErrors +
            lesionLocationCuration.validationErrors +
            nonOncologicalHistoryCuration.validationErrors +
            ecgCuration.validationErrors +
            infectionCuration.validationErrors +
            periodBetweenUnitCuration.validationErrors +
            complicationCuration.validationErrors +
            toxicityCuration.validationErrors +
            molecularTestIhcCuration.validationErrors +
            molecularTestPdl1Curation.validationErrors +
            sequencingTestCuration.validationErrors +
            medicationNameCuration.validationErrors +
            medicationDosageCuration.validationErrors +
            intoleranceCuration.validationErrors +
            drugInteractionCuration.validationErrors +
            qtProlongingCuration.validationErrors +
            surgeryNameCuration.validationErrors).toSet()


    companion object {
        fun create(
            curationDir: String,
            curationDoidValidator: CurationDoidValidator,
            curationIcdValidator: CurationIcdValidator,
            treatmentDatabase: TreatmentDatabase
        ) = CurationDatabaseContext(
            ecgCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.ECG_TSV,
                ECGConfigFactory(),
                CurationCategory.ECG
            ) { it.ecgEvaluatedInputs },
            infectionCuration = CurationDatabaseReader.read(
                curationDir, CurationDatabaseReader.INFECTION_TSV, InfectionConfigFactory(), CurationCategory.INFECTION
            ) { it.infectionEvaluatedInputs },
            nonOncologicalHistoryCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.NON_ONCOLOGICAL_HISTORY_TSV,
                NonOncologicalHistoryConfigFactory(curationDoidValidator),
                CurationCategory.NON_ONCOLOGICAL_HISTORY
            ) { it.nonOncologicalHistoryEvaluatedInputs },
            complicationCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.COMPLICATION_TSV,
                ComplicationConfigFactory(),
                CurationCategory.COMPLICATION
            ) { it.complicationEvaluatedInputs },
            intoleranceCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.INTOLERANCE_TSV,
                IntoleranceConfigFactory(curationDoidValidator),
                CurationCategory.INTOLERANCE
            ) { it.intoleranceEvaluatedInputs },
            secondPrimaryCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.SECOND_PRIMARY_TSV,
                SecondPrimaryConfigFactory(curationDoidValidator),
                CurationCategory.SECOND_PRIMARY
            ) { it.secondPrimaryEvaluatedInputs },
            treatmentHistoryEntryCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.ONCOLOGICAL_HISTORY_TSV,
                TreatmentHistoryEntryConfigFactory(treatmentDatabase),
                CurationCategory.ONCOLOGICAL_HISTORY
            ) { it.treatmentHistoryEntryEvaluatedInputs },
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
            toxicityCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.TOXICITY_TSV,
                ToxicityConfigFactory(curationIcdValidator),
                CurationCategory.TOXICITY
            ) { it.toxicityEvaluatedInputs },
            lesionLocationCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.LESION_LOCATION_TSV,
                LesionLocationConfigFactory(),
                CurationCategory.LESION_LOCATION
            ) { it.lesionLocationEvaluatedInputs },
            primaryTumorCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.PRIMARY_TUMOR_TSV,
                PrimaryTumorConfigFactory(curationDoidValidator),
                CurationCategory.PRIMARY_TUMOR
            ) { it.primaryTumorEvaluatedInputs },
            qtProlongingCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.QT_PROLONGATING_TSV,
                QTProlongatingConfigFactory(),
                CurationCategory.QT_PROLONGATING
            ) { emptySet() },
            drugInteractionCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.DRUG_INTERACTIONS_TSV,
                DrugInteractionConfigFactory(),
                CurationCategory.DRUG_INTERACTIONS
            ) { emptySet() },
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
            periodBetweenUnitCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.PERIOD_BETWEEN_UNIT_TSV,
                PeriodBetweenUnitConfigFactory(),
                CurationCategory.PERIOD_BETWEEN_UNIT_INTERPRETATION
            ) { it.periodBetweenUnitEvaluatedInputs },
            administrationRouteTranslation = TranslationDatabaseReader.read(
                curationDir,
                TranslationDatabaseReader.ADMINISTRATION_ROUTE_TRANSLATION_TSV,
                AdministrationRouteTranslationFactory(),
                CurationCategory.ADMINISTRATION_ROUTE_TRANSLATION
            ) { it.administrationRouteEvaluatedInputs },
            dosageUnitTranslation = TranslationDatabaseReader.read(
                curationDir,
                TranslationDatabaseReader.DOSAGE_UNIT_TRANSLATION_TSV,
                DosageUnitTranslationFactory(),
                CurationCategory.DOSAGE_UNIT_TRANSLATION
            ) { it.dosageUnitEvaluatedInputs },
            bloodTransfusionTranslation = TranslationDatabaseReader.read(
                curationDir,
                TranslationDatabaseReader.BLOOD_TRANSFUSION_TRANSLATION_TSV,
                BloodTransfusionTranslationFactory(),
                CurationCategory.BLOOD_TRANSFUSION_TRANSLATION
            ) { emptySet() },
            toxicityTranslation = TranslationDatabaseReader.read(
                curationDir,
                TranslationDatabaseReader.TOXICITY_TRANSLATION_TSV,
                ToxicityTranslationFactory(),
                CurationCategory.TOXICITY_TRANSLATION,
            ) { it.toxicityTranslationEvaluatedInputs },
            laboratoryTranslation = TranslationDatabaseReader.read(
                curationDir,
                TranslationDatabaseReader.LABORATORY_TRANSLATION_TSV,
                LaboratoryTranslationFactory(),
                CurationCategory.LABORATORY_TRANSLATION
            ) { it.laboratoryEvaluatedInputs },
            surgeryNameCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.SURGERY_NAME_TSV,
                SurgeryNameConfigFactory(),
                CurationCategory.SURGERY_NAME
            ) { it.surgeryTranslationEvaluatedInputs }
        )
    }

}