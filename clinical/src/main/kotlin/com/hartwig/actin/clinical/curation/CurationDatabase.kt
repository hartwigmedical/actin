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

typealias InputText = String

data class CurationDatabase(
    val primaryTumorConfigs: Map<InputText, Set<PrimaryTumorConfig>>,
    val treatmentHistoryEntryConfigs: Map<InputText, Set<TreatmentHistoryEntryConfig>>,
    val secondPrimaryConfigs: Map<InputText, Set<SecondPrimaryConfig>>,
    val lesionLocationConfigs: Map<InputText, Set<LesionLocationConfig>>,
    val nonOncologicalHistoryConfigs: Map<InputText, Set<NonOncologicalHistoryConfig>>,
    val ecgConfigs: Map<InputText, Set<ECGConfig>>,
    val infectionConfigs: Map<InputText, Set<InfectionConfig>>,
    val periodBetweenUnitConfigs: Map<InputText, Set<PeriodBetweenUnitConfig>>,
    val complicationConfigs: Map<InputText, Set<ComplicationConfig>>,
    val toxicityConfigs: Map<InputText, Set<ToxicityConfig>>,
    val molecularTestConfigs: Map<InputText, Set<MolecularTestConfig>>,
    val medicationNameConfigs: Map<InputText, Set<MedicationNameConfig>>,
    val medicationDosageConfigs: Map<InputText, Set<MedicationDosageConfig>>,
    val intoleranceConfigs: Map<InputText, Set<IntoleranceConfig>>,
    val cypInteractionConfigs: Map<InputText, Set<CypInteractionConfig>>,
    val qtProlongingConfigs: Map<InputText, Set<QTProlongatingConfig>>,
    val administrationRouteTranslations: Map<InputText, Translation>,
    val laboratoryTranslations: Map<Pair<InputText, InputText>, LaboratoryTranslation>,
    val toxicityTranslations: Map<InputText, Translation>,
    val bloodTransfusionTranslations: Map<InputText, Translation>,
    val dosageUnitTranslations: Map<InputText, Translation>
)