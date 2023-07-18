package com.hartwig.actin.clinical.datamodel.treatment.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Set;

import com.hartwig.actin.clinical.datamodel.treatment.DrugType;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrug;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableDrugTherapy;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class TreatmentHistoryEntryTest {

    public static final ImmutableTreatmentHistoryEntry TREATMENT_HISTORY_ENTRY_WITHOUT_TYPE =
            treatmentHistoryEntryWithDrugTypes(Collections.emptySet());
    public static final ImmutableTreatmentHistoryEntry TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE =
            treatmentHistoryEntryWithDrugTypes(Set.of(DrugType.PLATINUM_COMPOUND));

    @Test
    public void shouldExtractNamesFromTreatments() {
        TreatmentHistoryEntry treatmentHistoryEntry = ImmutableTreatmentHistoryEntry.copyOf(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE)
                .withTreatments(drugTherapy(Collections.emptySet(), "t1"), drugTherapy(Collections.emptySet(), "t2"));
        assertThat(treatmentHistoryEntry.treatmentName()).isEqualTo("t1;t2");
    }

    @Test
    public void shouldReturnTreatmentCategories() {
        assertThat(TREATMENT_HISTORY_ENTRY_WITH_DRUG_TYPE.categories()).containsExactly(TreatmentCategory.CHEMOTHERAPY);
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
        return ImmutableTreatmentHistoryEntry.builder().addTreatments(drugTherapy(types, "test treatment")).build();
    }

    @NotNull
    private static ImmutableDrugTherapy drugTherapy(@NotNull Set<DrugType> types, @NotNull String name) {
        return ImmutableDrugTherapy.builder()
                .addDrugs(ImmutableDrug.builder().name("test drug").category(TreatmentCategory.CHEMOTHERAPY).drugTypes(types).build())
                .name(name)
                .build();
    }
}