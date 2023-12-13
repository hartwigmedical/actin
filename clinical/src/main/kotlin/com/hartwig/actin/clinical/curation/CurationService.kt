package com.hartwig.actin.clinical.curation

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.clinical.curation.config.ComplicationConfig
import com.hartwig.actin.clinical.curation.config.ComplicationConfigFactory
import com.hartwig.actin.clinical.curation.config.CurationConfigValidationError
import com.hartwig.actin.clinical.curation.config.CypInteractionConfig
import com.hartwig.actin.clinical.curation.config.CypInteractionConfigFactory
import com.hartwig.actin.clinical.curation.config.ECGConfig
import com.hartwig.actin.clinical.curation.config.ECGConfigFactory
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
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.curation.config.MolecularTestConfigFactory
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
import com.hartwig.actin.clinical.curation.config.ToxicityConfig
import com.hartwig.actin.clinical.curation.config.ToxicityConfigFactory
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfigFactory
import com.hartwig.actin.clinical.curation.extraction.ExtractionEvaluation
import com.hartwig.actin.clinical.curation.translation.AdministrationRouteTranslationFactory
import com.hartwig.actin.clinical.curation.translation.LaboratoryIdentifiers
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslationFactory
import com.hartwig.actin.clinical.curation.translation.ToxicityTranslationFactory
import com.hartwig.actin.clinical.curation.translation.TranslationDatabase
import com.hartwig.actin.clinical.curation.translation.TranslationDatabaseReader

data class CurationService(
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
    val molecularTestCuration: CurationDatabase<MolecularTestConfig>,
    val medicationNameCuration: CurationDatabase<MedicationNameConfig>,
    val medicationDosageCuration: CurationDatabase<MedicationDosageConfig>,
    val intoleranceCuration: CurationDatabase<IntoleranceConfig>,
    val cypInteractionCuration: CurationDatabase<CypInteractionConfig>,
    val qtProlongingCuration: CurationDatabase<QTProlongatingConfig>,
    val administrationRouteTranslation: TranslationDatabase<String>,
    val laboratoryTranslation: TranslationDatabase<LaboratoryIdentifiers>,
    val toxicityTranslation: TranslationDatabase<String>,
    val bloodTransfusionTranslation: TranslationDatabase<String>,
    val dosageUnitTranslation: TranslationDatabase<String>,
) {
    fun validate(extractionEvaluations: List<ExtractionEvaluation>): Set<CurationConfigValidationError> =
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
            molecularTestCuration,
            medicationNameCuration,
            medicationDosageCuration,
            intoleranceCuration,
            cypInteractionCuration,
            qtProlongingCuration
        ).flatMap { it.validate(extractionEvaluations) }.toSet()

    companion object {
        fun create(
            curationDir: String,
            curationDoidValidator: CurationDoidValidator,
            treatmentDatabase: TreatmentDatabase
        ) = CurationService(
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
                CurationCategory.NON_ONCOLOGICAL_HISTORY
            ) { it.nonOncologicalHistoryEvaluatedInputs },
            intoleranceCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.INTOLERANCE_TSV,
                IntoleranceConfigFactory(curationDoidValidator),
                CurationCategory.INTOLERANCE
            ) { it.intoleranceEvaluatedInputs },

            laboratoryTranslation = TranslationDatabaseReader.read(
                curationDir,
                TranslationDatabaseReader.LABORATORY_TRANSLATION_TSV,
                LaboratoryTranslationFactory()
            ),
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
            molecularTestCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.MOLECULAR_TEST_TSV,
                MolecularTestConfigFactory(),
                CurationCategory.MOLECULAR_TEST
            ) { it.molecularTestEvaluatedInputs },
            toxicityCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.TOXICITY_TSV,
                ToxicityConfigFactory(),
                CurationCategory.TOXICITY
            ) { it.toxicityEvaluatedInputs },
            toxicityTranslation = TranslationDatabaseReader.read(
                curationDir,
                TranslationDatabaseReader.TOXICITY_TRANSLATION_TSV,
                ToxicityTranslationFactory()
            ),
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
                CurationCategory.QT_PROLONGATION
            ) { emptySet() },
            cypInteractionCuration = CurationDatabaseReader.read(
                curationDir,
                CurationDatabaseReader.CYP_INTERACTIONS_TSV,
                CypInteractionConfigFactory(),
                CurationCategory.CYP_INTERACTION
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
                AdministrationRouteTranslationFactory()
            ),
            dosageUnitTranslation = TranslationDatabaseReader.read(
                curationDir,
                TranslationDatabaseReader.ADMINISTRATION_ROUTE_TRANSLATION_TSV,
                AdministrationRouteTranslationFactory()
            ),
            bloodTransfusionTranslation = TranslationDatabaseReader.read(
                curationDir,
                TranslationDatabaseReader.ADMINISTRATION_ROUTE_TRANSLATION_TSV,
                AdministrationRouteTranslationFactory()
            )
        )
    }

}