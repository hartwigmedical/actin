package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.ComplicationConfig
import com.hartwig.actin.clinical.curation.config.CypInteractionConfig
import com.hartwig.actin.clinical.curation.config.ECGConfig
import com.hartwig.actin.clinical.curation.config.InfectionConfig
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig
import com.hartwig.actin.clinical.curation.config.MedicationNameConfig
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.PeriodBetweenUnitConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig
import com.hartwig.actin.clinical.curation.config.ToxicityConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.curation.translation.AdministrationRouteTranslation
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslation
import com.hartwig.actin.clinical.curation.translation.DosageUnitTranslation
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation
import com.hartwig.actin.clinical.curation.translation.ToxicityTranslation

data class CurationDatabase(
    val primaryTumorConfigs: List<PrimaryTumorConfig>,
    val treatmentHistoryEntryConfigs: List<TreatmentHistoryEntryConfig>,
    val oncologicalHistoryConfigs: List<OncologicalHistoryConfig>,
    val secondPrimaryConfigs: List<SecondPrimaryConfig>,
    val lesionLocationConfigs: List<LesionLocationConfig>,
    val nonOncologicalHistoryConfigs: List<NonOncologicalHistoryConfig>,
    val ecgConfigs: List<ECGConfig>,
    val infectionConfigs: List<InfectionConfig>,
    val periodBetweenUnitConfigs: List<PeriodBetweenUnitConfig>,
    val complicationConfigs: List<ComplicationConfig>,
    val toxicityConfigs: List<ToxicityConfig>,
    val molecularTestConfigs: List<MolecularTestConfig>,
    val medicationNameConfigs: List<MedicationNameConfig>,
    val medicationDosageConfigs: List<MedicationDosageConfig>,
    val intoleranceConfigs: List<IntoleranceConfig>,
    val cypInteractionConfigs: List<CypInteractionConfig>,
    val qtProlongingConfigs: List<QTProlongatingConfig>,
    val administrationRouteTranslations: List<AdministrationRouteTranslation>,
    val laboratoryTranslations: List<LaboratoryTranslation>,
    val toxicityTranslations: List<ToxicityTranslation>,
    val bloodTransfusionTranslations: List<BloodTransfusionTranslation>,
    val dosageUnitTranslations: List<DosageUnitTranslation>
)