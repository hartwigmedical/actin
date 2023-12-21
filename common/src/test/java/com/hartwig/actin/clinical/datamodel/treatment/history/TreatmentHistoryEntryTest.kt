package com.hartwig.actin.clinical.datamodel.treatment.history

import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableRadiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.Radiotherapy
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.assertj.core.api.Assertions
import org.junit.Test

class TreatmentHistoryEntryTest {
    @Test
    fun shouldExtractNamesFromTreatmentHistory() {
        val treatmentHistoryEntry: TreatmentHistoryEntry = ImmutableTreatmentHistoryEntry.copyOf(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE)
            .withTreatments(chemotherapy(emptySet(), TREATMENT_1), chemotherapy(emptySet(), TREATMENT_2))
        Assertions.assertThat(treatmentHistoryEntry.treatmentName()).isEqualTo(TREATMENT_1 + ";" + TREATMENT_2)
    }

    @Test
    fun shouldExtractTreatmentDisplayStringsFromTreatmentHistory() {
        val treatmentHistoryEntry: TreatmentHistoryEntry = ImmutableTreatmentHistoryEntry.copyOf(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE)
            .withTreatments(chemotherapy(emptySet(), TREATMENT_1), chemotherapy(emptySet(), TREATMENT_2))
        Assertions.assertThat(treatmentHistoryEntry.treatmentDisplay()).isEqualTo("Treatment 1;Treatment 2")
    }

    @Test
    fun shouldDisplayChemoradiationWhenOnlyComponentsAreChemotherapyAndRadiation() {
        val treatmentHistoryEntry: TreatmentHistoryEntry = ImmutableTreatmentHistoryEntry.copyOf(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE)
            .withTreatments(chemotherapy(emptySet(), "chemotherapy"), RADIOTHERAPY)
        Assertions.assertThat(treatmentHistoryEntry.treatmentDisplay()).isEqualTo("Chemoradiation")
    }

    @Test
    fun shouldDisplayChemoradiationAndOtherTreatmentWhenComponentsAreChemotherapyAndRadiationAndOtherTreatment() {
        val treatmentHistoryEntry: TreatmentHistoryEntry = ImmutableTreatmentHistoryEntry.copyOf(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE)
            .withTreatments(
                chemotherapy(emptySet(), "chemotherapy"),
                RADIOTHERAPY,
                ImmutableOtherTreatment.builder()
                    .name("ablation")
                    .addCategories(TreatmentCategory.ABLATION)
                    .synonyms(emptySet())
                    .isSystemic(false)
                    .build()
            )
        Assertions.assertThat(treatmentHistoryEntry.treatmentDisplay()).isEqualTo("Chemoradiation and Ablation")
    }

    @Test
    fun shouldDisplayChemoradiationWithChemoDrugWhenComponentsAreChemotherapyAndRadiationAndChemoDrugTreatment() {
        val treatmentHistoryEntry: TreatmentHistoryEntry = ImmutableTreatmentHistoryEntry.copyOf(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE)
            .withTreatments(
                chemotherapy(emptySet(), "chemotherapy"),
                RADIOTHERAPY,
                chemotherapy(emptySet(), "chemo drug")
            )
        Assertions.assertThat(treatmentHistoryEntry.treatmentDisplay()).isEqualTo("Chemoradiation (with Chemo drug)")
    }

    @Test
    fun shouldDisplayNormallyWhenComponentsAreChemotherapyAndRadiationAndMultipleAdditionalTreatments() {
        val treatmentHistoryEntry: TreatmentHistoryEntry = ImmutableTreatmentHistoryEntry.copyOf(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE)
            .withTreatments(
                chemotherapy(emptySet(), "chemotherapy"),
                RADIOTHERAPY,
                chemotherapy(emptySet(), "chemo drug"),
                ImmutableOtherTreatment.builder()
                    .name("ablation")
                    .addCategories(TreatmentCategory.ABLATION)
                    .synonyms(emptySet())
                    .isSystemic(false)
                    .build()
            )
        Assertions.assertThat(treatmentHistoryEntry.treatmentDisplay()).isEqualTo("Chemotherapy;Radiotherapy;Chemo drug;Ablation")
    }

    @Test
    fun shouldIncludeSwitchAndMaintenanceTreatmentsWhenPresent() {
        Assertions.assertThat(TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.allTreatments()).isEqualTo(
            java.util.Set.of(
                TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.treatments().iterator().next(),
                SWITCH_TREATMENT_STAGE.treatment(),
                MAINTENANCE_TREATMENT_STAGE.treatment()
            )
        )
    }

    @Test
    fun shouldDisplayBaseNameWithoutSwitchAndMaintenanceTreatments() {
        Assertions.assertThat(TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.treatmentDisplay()).isEqualTo("Test treatment")
    }

    @Test
    fun shouldReturnTreatmentCategories() {
        Assertions.assertThat(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.categories()).containsExactly(TreatmentCategory.CHEMOTHERAPY)
    }

    @Test
    fun shouldIncludeSwitchAndMaintenanceTreatmentCategoriesInTreatmentCategories() {
        Assertions.assertThat(TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.categories()).containsExactlyInAnyOrder(
            TreatmentCategory.CHEMOTHERAPY,
            TreatmentCategory.TARGETED_THERAPY,
            TreatmentCategory.SUPPORTIVE_TREATMENT
        )
    }

    @Test
    fun shouldReturnNullForTypeNotConfiguredWhenMatchingAgainstType() {
        Assertions.assertThat(TREATMENT_HISTORY_ENTRY_WITHOUT_TYPE.isOfType(DrugType.PLATINUM_COMPOUND)).isNull()
    }

    @Test
    fun shouldReturnTrueForMatchingType() {
        Assertions.assertThat(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.isOfType(DrugType.PLATINUM_COMPOUND)).isTrue()
    }

    @Test
    fun shouldReturnFalseForTypeThatDoesNotMatch() {
        Assertions.assertThat(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.isOfType(DrugType.ANTIMETABOLITE)).isFalse()
    }

    @Test
    fun shouldReturnTrueForTypeThatMatchesSwitchTreatment() {
        Assertions.assertThat(TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.isOfType(DrugType.ALK_INHIBITOR)).isTrue()
    }

    @Test
    fun shouldReturnTrueForTypeThatMatchesMaintenanceTreatment() {
        Assertions.assertThat(TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.isOfType(DrugType.STEROID)).isTrue()
    }

    @Test
    fun shouldReturnTrueWhenTypeConfigured() {
        Assertions.assertThat(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.hasTypeConfigured()).isTrue()
    }

    @Test
    fun shouldReturnFalseWhenTypeNotConfigured() {
        Assertions.assertThat(TREATMENT_HISTORY_ENTRY_WITHOUT_TYPE.hasTypeConfigured()).isFalse()
    }

    @Test
    fun shouldReturnNullForTypeNotConfiguredWhenMatchingAgainstSetOfTypes() {
        Assertions.assertThat(
            TREATMENT_HISTORY_ENTRY_WITHOUT_TYPE.matchesTypeFromSet(
                java.util.Set.of(
                    DrugType.ANTIMETABOLITE,
                    DrugType.PLATINUM_COMPOUND
                )
            )
        ).isNull()
    }

    @Test
    fun shouldReturnTrueForSwitchTypeThatMatchesSetOfTypes() {
        Assertions.assertThat(
            TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.matchesTypeFromSet(
                java.util.Set.of(
                    DrugType.ANTIMETABOLITE,
                    DrugType.ALK_INHIBITOR
                )
            )
        ).isTrue()
    }

    @Test
    fun shouldReturnTrueForMaintenanceTypeThatMatchesSetOfTypes() {
        Assertions.assertThat(
            TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.matchesTypeFromSet(
                java.util.Set.of(
                    DrugType.ANTIMETABOLITE,
                    DrugType.STEROID
                )
            )
        ).isTrue()
    }

    @Test
    fun shouldReturnTrueForTypeThatMatchesSetOfTypes() {
        Assertions.assertThat(
            TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.matchesTypeFromSet(
                java.util.Set.of(
                    DrugType.ANTIMETABOLITE,
                    DrugType.PLATINUM_COMPOUND
                )
            )
        ).isTrue()
    }

    @Test
    fun shouldReturnFalseForTypeThatDoesNotMatchSetOfTypes() {
        Assertions.assertThat(
            TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.matchesTypeFromSet(
                java.util.Set.of(
                    DrugType.ANTIMETABOLITE,
                    DrugType.ANTHRACYCLINE
                )
            )
        ).isFalse()
    }

    companion object {
        private val TREATMENT_HISTORY_ENTRY_WITHOUT_TYPE = treatmentHistoryEntryWithDrugTypes(emptySet())
        private val TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE =
            treatmentHistoryEntryWithDrugTypes(java.util.Set.of(DrugType.PLATINUM_COMPOUND))
        val SWITCH_TREATMENT_STAGE = treatmentStage("SWITCH TREATMENT", TreatmentCategory.TARGETED_THERAPY, DrugType.ALK_INHIBITOR)
        val MAINTENANCE_TREATMENT_STAGE = treatmentStage("MAINTENANCE TREATMENT", TreatmentCategory.SUPPORTIVE_TREATMENT, DrugType.STEROID)
        val TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE = ImmutableTreatmentHistoryEntry.copyOf(
            TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE
        )
            .withTreatmentHistoryDetails(
                ImmutableTreatmentHistoryDetails.builder()
                    .addSwitchToTreatments(SWITCH_TREATMENT_STAGE)
                    .maintenanceTreatment(MAINTENANCE_TREATMENT_STAGE)
                    .build()
            )
        private val RADIOTHERAPY: Radiotherapy = ImmutableRadiotherapy.builder().name("radiotherapy").build()
        private const val TREATMENT_1 = "TREATMENT_1"
        private const val TREATMENT_2 = "TREATMENT_2"
        private fun treatmentStage(name: String, category: TreatmentCategory, drugType: DrugType): ImmutableTreatmentStage {
            return ImmutableTreatmentStage.builder()
                .treatment(
                    ImmutableDrugTreatment.builder()
                        .name(name)
                        .addDrugs(ImmutableDrug.builder().name("$name drug").category(category).addDrugTypes(drugType).build())
                        .build()
                )
                .build()
        }

        private fun treatmentHistoryEntryWithDrugTypes(types: Set<DrugType>): ImmutableTreatmentHistoryEntry {
            return ImmutableTreatmentHistoryEntry.builder().addTreatments(chemotherapy(types, "test treatment")).build()
        }

        private fun chemotherapy(types: Set<DrugType>, name: String): ImmutableDrugTreatment {
            return ImmutableDrugTreatment.builder()
                .addDrugs(ImmutableDrug.builder().name("test drug").category(TreatmentCategory.CHEMOTHERAPY).drugTypes(types).build())
                .name(name)
                .build()
        }
    }
}