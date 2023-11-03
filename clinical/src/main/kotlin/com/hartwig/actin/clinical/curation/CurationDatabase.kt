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
import com.hartwig.actin.clinical.curation.config.PeriodBetweenUnitConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig
import com.hartwig.actin.clinical.curation.config.ToxicityConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation
import com.hartwig.actin.clinical.curation.translation.Translation

data class CurationDatabase(
    val primaryTumorConfigs: Map<String, Set<PrimaryTumorConfig>>,
    val treatmentHistoryEntryConfigs: Map<String, Set<TreatmentHistoryEntryConfig>>,
    val secondPrimaryConfigs: Map<String, Set<SecondPrimaryConfig>>,
    val lesionLocationConfigs: Map<String, Set<LesionLocationConfig>>,
    val nonOncologicalHistoryConfigs: Map<String, Set<NonOncologicalHistoryConfig>>,
    val ecgConfigs: Map<String, Set<ECGConfig>>,
    val infectionConfigs: Map<String, Set<InfectionConfig>>,
    val periodBetweenUnitConfigs: Map<String, Set<PeriodBetweenUnitConfig>>,
    val complicationConfigs: Map<String, Set<ComplicationConfig>>,
    val toxicityConfigs: Map<String, Set<ToxicityConfig>>,
    val molecularTestConfigs: Map<String, Set<MolecularTestConfig>>,
    val medicationNameConfigs: Map<String, Set<MedicationNameConfig>>,
    val medicationDosageConfigs: Map<String, Set<MedicationDosageConfig>>,
    val intoleranceConfigs: Map<String, Set<IntoleranceConfig>>,
    val cypInteractionConfigs: Map<String, Set<CypInteractionConfig>>,
    val qtProlongingConfigs: Map<String, Set<QTProlongatingConfig>>,
    val administrationRouteTranslations: Map<String, Translation>,
    val laboratoryTranslations: Map<Pair<String, String>, LaboratoryTranslation>,
    val toxicityTranslations: Map<String, Translation>,
    val bloodTransfusionTranslations: Map<String, Translation>,
    val dosageUnitTranslations: Map<String, Translation>
)