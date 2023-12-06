package com.hartwig.actin.clinical.datamodel.treatment.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Set;

import com.hartwig.actin.clinical.datamodel.treatment.DrugType;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrugTreatment;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTreatment;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableRadiotherapy;
import com.hartwig.actin.clinical.datamodel.treatment.Radiotherapy;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TreatmentHistoryEntryTest {
    private static final ImmutableTreatmentHistoryEntry TREATMENT_HISTORY_ENTRY_WITHOUT_TYPE =
            treatmentHistoryEntryWithDrugTypes(Collections.emptySet());
    private static final ImmutableTreatmentHistoryEntry TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE =
            treatmentHistoryEntryWithDrugTypes(Set.of(DrugType.PLATINUM_COMPOUND));
    public static final ImmutableTreatmentStage SWITCH_TREATMENT_STAGE =
            treatmentStage("SWITCH TREATMENT", TreatmentCategory.TARGETED_THERAPY, DrugType.ALK_INHIBITOR);
    public static final ImmutableTreatmentStage MAINTENANCE_TREATMENT_STAGE =
            treatmentStage("MAINTENANCE TREATMENT", TreatmentCategory.SUPPORTIVE_TREATMENT, DrugType.STEROID);
    public static final ImmutableTreatmentHistoryEntry TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE =
            ImmutableTreatmentHistoryEntry.copyOf(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE)
                    .withTreatmentHistoryDetails(ImmutableTreatmentHistoryDetails.builder()
                            .addSwitchToTreatments(SWITCH_TREATMENT_STAGE)
                            .maintenanceTreatment(MAINTENANCE_TREATMENT_STAGE)
                            .build());
    
    private static final Radiotherapy RADIOTHERAPY = ImmutableRadiotherapy.builder().name("radiotherapy").build();
    private static final String TREATMENT_1 = "TREATMENT_1";
    private static final String TREATMENT_2 = "TREATMENT_2";

    @Test
    public void shouldExtractNamesFromTreatmentHistory() {
        TreatmentHistoryEntry treatmentHistoryEntry = ImmutableTreatmentHistoryEntry.copyOf(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE)
                .withTreatments(chemotherapy(Collections.emptySet(), TREATMENT_1), chemotherapy(Collections.emptySet(), TREATMENT_2));
        assertThat(treatmentHistoryEntry.treatmentName()).isEqualTo(TREATMENT_1 + ";" + TREATMENT_2);
    }

    @Test
    public void shouldExtractTreatmentDisplayStringsFromTreatmentHistory() {
        TreatmentHistoryEntry treatmentHistoryEntry = ImmutableTreatmentHistoryEntry.copyOf(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE)
                .withTreatments(chemotherapy(Collections.emptySet(), TREATMENT_1), chemotherapy(Collections.emptySet(), TREATMENT_2));
        assertThat(treatmentHistoryEntry.treatmentDisplay()).isEqualTo("Treatment 1;Treatment 2");
    }

    @Test
    public void shouldDisplayChemoradiationWhenOnlyComponentsAreChemotherapyAndRadiation() {
        TreatmentHistoryEntry treatmentHistoryEntry = ImmutableTreatmentHistoryEntry.copyOf(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE)
                .withTreatments(chemotherapy(Collections.emptySet(), "chemotherapy"), RADIOTHERAPY);
        assertThat(treatmentHistoryEntry.treatmentDisplay()).isEqualTo("Chemoradiation");
    }

    @Test
    public void shouldDisplayChemoradiationAndOtherTreatmentWhenComponentsAreChemotherapyAndRadiationAndOtherTreatment() {
        TreatmentHistoryEntry treatmentHistoryEntry = ImmutableTreatmentHistoryEntry.copyOf(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE)
                .withTreatments(chemotherapy(Collections.emptySet(), "chemotherapy"),
                        RADIOTHERAPY,
                        ImmutableOtherTreatment.builder()
                                .name("ablation")
                                .addCategories(TreatmentCategory.ABLATION)
                                .synonyms(Collections.emptySet())
                                .isSystemic(false)
                                .build());
        assertThat(treatmentHistoryEntry.treatmentDisplay()).isEqualTo("Chemoradiation and Ablation");
    }

    @Test
    public void shouldDisplayChemoradiationWithChemoDrugWhenComponentsAreChemotherapyAndRadiationAndChemoDrugTreatment() {
        TreatmentHistoryEntry treatmentHistoryEntry = ImmutableTreatmentHistoryEntry.copyOf(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE)
                .withTreatments(chemotherapy(Collections.emptySet(), "chemotherapy"),
                        RADIOTHERAPY,
                        chemotherapy(Collections.emptySet(), "chemo drug"));
        assertThat(treatmentHistoryEntry.treatmentDisplay()).isEqualTo("Chemoradiation (with Chemo drug)");
    }

    @Test
    public void shouldDisplayNormallyWhenComponentsAreChemotherapyAndRadiationAndMultipleAdditionalTreatments() {
        TreatmentHistoryEntry treatmentHistoryEntry = ImmutableTreatmentHistoryEntry.copyOf(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE)
                .withTreatments(chemotherapy(Collections.emptySet(), "chemotherapy"),
                        RADIOTHERAPY,
                        chemotherapy(Collections.emptySet(), "chemo drug"),
                        ImmutableOtherTreatment.builder()
                                .name("ablation")
                                .addCategories(TreatmentCategory.ABLATION)
                                .synonyms(Collections.emptySet())
                                .isSystemic(false)
                                .build());
        assertThat(treatmentHistoryEntry.treatmentDisplay()).isEqualTo("Chemotherapy;Radiotherapy;Chemo drug;Ablation");
    }

    @Test
    public void shouldIncludeSwitchAndMaintenanceTreatmentsWhenPresent() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.allTreatments()).isEqualTo(Set.of(
                TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.treatments().iterator().next(),
                SWITCH_TREATMENT_STAGE.treatment(),
                MAINTENANCE_TREATMENT_STAGE.treatment()));
    }

    @Test
    public void shouldDisplaySwitchAndMaintenanceTreatmentsWhenPresent() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.treatmentDisplay()).isEqualTo(
                "Test treatment with switch to Switch treatment continued with Maintenance treatment maintenance");
    }

    @Test
    public void shouldDisplayBaseNameWithoutSwitchAndMaintenanceTreatments() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.baseTreatmentDisplay()).isEqualTo("Test treatment");
    }
    
    @Test
    public void shouldReturnTreatmentCategories() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.categories()).containsExactly(TreatmentCategory.CHEMOTHERAPY);
    }

    @Test
    public void shouldIncludeSwitchAndMaintenanceTreatmentCategoriesInTreatmentCategories() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.categories()).containsExactlyInAnyOrder(TreatmentCategory.CHEMOTHERAPY,
                TreatmentCategory.TARGETED_THERAPY,
                TreatmentCategory.SUPPORTIVE_TREATMENT);
    }

    @NotNull
    private static ImmutableTreatmentStage treatmentStage(String name, TreatmentCategory category, DrugType drugType) {
        return ImmutableTreatmentStage.builder()
                .treatment(ImmutableDrugTreatment.builder()
                        .name(name)
                        .addDrugs(ImmutableDrug.builder().name(name + " drug").category(category).addDrugTypes(drugType).build())
                        .build())
                .build();
    }

    @Test
    public void shouldReturnNullForTypeNotConfiguredWhenMatchingAgainstType() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITHOUT_TYPE.isOfType(DrugType.PLATINUM_COMPOUND)).isNull();
    }

    @Test
    public void shouldReturnTrueForMatchingType() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.isOfType(DrugType.PLATINUM_COMPOUND)).isTrue();
    }

    @Test
    public void shouldReturnFalseForTypeThatDoesNotMatch() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.isOfType(DrugType.ANTIMETABOLITE)).isFalse();
    }

    @Test
    public void shouldReturnTrueForTypeThatMatchesSwitchTreatment() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.isOfType(DrugType.ALK_INHIBITOR)).isTrue();
    }

    @Test
    public void shouldReturnTrueForTypeThatMatchesMaintenanceTreatment() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.isOfType(DrugType.STEROID)).isTrue();
    }

    @Test
    public void shouldReturnTrueWhenTypeConfigured() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.hasTypeConfigured()).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenTypeNotConfigured() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITHOUT_TYPE.hasTypeConfigured()).isFalse();
    }

    @Test
    public void shouldReturnNullForTypeNotConfiguredWhenMatchingAgainstSetOfTypes() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITHOUT_TYPE.matchesTypeFromSet(Set.of(DrugType.ANTIMETABOLITE,
                DrugType.PLATINUM_COMPOUND))).isNull();
    }

    @Test
    public void shouldReturnTrueForSwitchTypeThatMatchesSetOfTypes() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.matchesTypeFromSet(Set.of(DrugType.ANTIMETABOLITE,
                DrugType.ALK_INHIBITOR))).isTrue();
    }

    @Test
    public void shouldReturnTrueForMaintenanceTypeThatMatchesSetOfTypes() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_SWITCH_AND_MAINTENANCE.matchesTypeFromSet(Set.of(DrugType.ANTIMETABOLITE,
                DrugType.STEROID))).isTrue();
    }

    @Test
    public void shouldReturnTrueForTypeThatMatchesSetOfTypes() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.matchesTypeFromSet(Set.of(DrugType.ANTIMETABOLITE,
                DrugType.PLATINUM_COMPOUND))).isTrue();
    }

    @Test
    public void shouldReturnFalseForTypeThatDoesNotMatchSetOfTypes() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.matchesTypeFromSet(Set.of(DrugType.ANTIMETABOLITE,
                DrugType.ANTHRACYCLINE))).isFalse();
    }

    @NotNull
    private static ImmutableTreatmentHistoryEntry treatmentHistoryEntryWithDrugTypes(@NotNull Set<DrugType> types) {
        return ImmutableTreatmentHistoryEntry.builder().addTreatments(chemotherapy(types, "test treatment")).build();
    }

    @NotNull
    private static ImmutableDrugTreatment chemotherapy(@NotNull Set<DrugType> types, @NotNull String name) {
        return ImmutableDrugTreatment.builder()
                .addDrugs(ImmutableDrug.builder().name("test drug").category(TreatmentCategory.CHEMOTHERAPY).drugTypes(types).build())
                .name(name)
                .build();
    }
}