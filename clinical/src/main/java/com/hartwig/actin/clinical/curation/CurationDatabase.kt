package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.ComplicationConfig
import com.hartwig.actin.clinical.curation.config.ECGConfig
import com.hartwig.actin.clinical.curation.config.InfectionConfig
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig
import com.hartwig.actin.clinical.curation.config.LesionLocationConfig
import com.hartwig.actin.clinical.curation.config.MedicationCategoryConfig
import com.hartwig.actin.clinical.curation.config.MedicationDosageConfig
import com.hartwig.actin.clinical.curation.config.MedicationNameConfig
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.config.SecondPrimaryConfig
import com.hartwig.actin.clinical.curation.config.ToxicityConfig
import com.hartwig.actin.clinical.curation.config.TreatmentHistoryEntryConfig
import com.hartwig.actin.clinical.curation.translation.AdministrationRouteTranslation
import com.hartwig.actin.clinical.curation.translation.BloodTransfusionTranslation
import com.hartwig.actin.clinical.curation.translation.LaboratoryTranslation
import com.hartwig.actin.clinical.curation.translation.ToxicityTranslation
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class CurationDatabase {
    abstract fun primaryTumorConfigs(): List<PrimaryTumorConfig?>
    abstract fun oncologicalHistoryConfigs(): List<OncologicalHistoryConfig?>
    abstract fun treatmentHistoryEntryConfigs(): List<TreatmentHistoryEntryConfig?>
    abstract fun secondPrimaryConfigs(): List<SecondPrimaryConfig?>
    abstract fun lesionLocationConfigs(): List<LesionLocationConfig?>
    abstract fun nonOncologicalHistoryConfigs(): List<NonOncologicalHistoryConfig?>
    abstract fun ecgConfigs(): List<ECGConfig?>
    abstract fun infectionConfigs(): List<InfectionConfig?>
    abstract fun complicationConfigs(): List<ComplicationConfig?>
    abstract fun toxicityConfigs(): List<ToxicityConfig?>
    abstract fun molecularTestConfigs(): List<MolecularTestConfig?>
    abstract fun medicationNameConfigs(): List<MedicationNameConfig?>
    abstract fun medicationDosageConfigs(): List<MedicationDosageConfig?>
    abstract fun medicationCategoryConfigs(): List<MedicationCategoryConfig?>
    abstract fun intoleranceConfigs(): List<IntoleranceConfig?>
    abstract fun administrationRouteTranslations(): List<AdministrationRouteTranslation?>
    abstract fun laboratoryTranslations(): List<LaboratoryTranslation?>
    abstract fun toxicityTranslations(): List<ToxicityTranslation?>
    abstract fun bloodTransfusionTranslations(): List<BloodTransfusionTranslation?>
}