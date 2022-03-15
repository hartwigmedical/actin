package com.hartwig.actin.treatment.input.datamodel;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum TreatmentInput {
    CHEMOTHERAPY(TreatmentCategory.CHEMOTHERAPY),
    RADIOTHERAPY(TreatmentCategory.RADIOTHERAPY),
    CHEMORADIOTHERAPY((TreatmentCategory.CHEMORADIOTHERAPY)),
    TARGETED_THERAPY(TreatmentCategory.TARGETED_THERAPY),
    IMMUNOTHERAPY(TreatmentCategory.IMMUNOTHERAPY),
    HORMONE_THERAPY(TreatmentCategory.HORMONE_THERAPY),
    ANTIVIRAL_THERAPY(TreatmentCategory.ANTIVIRAL_THERAPY),
    SUPPORTIVE_TREATMENT(TreatmentCategory.SUPPORTIVE_TREATMENT),
    SURGERY(TreatmentCategory.SURGERY),
    TRANSPLANTATION(TreatmentCategory.TRANSPLANTATION),
    TRIAL(TreatmentCategory.TRIAL),
    VACCINE(TreatmentCategory.VACCINE),
    CAR_T(TreatmentCategory.CAR_T),
    TCR_T(TreatmentCategory.TCR_T),
    GENE_THERAPY(TreatmentCategory.GENE_THERAPY),
    TAXANE(Sets.newHashSet("Paclitaxel", "Docetaxel", "Cabazitaxel")),
    FLUOROPYRIMIDINE(Sets.newHashSet("Capecitabine", "Carmofur", "Doxifluridine", "Fluorouracil", "Tegafur")),
    TYROSINE_KINASE_INHIBITOR(TreatmentCategory.TARGETED_THERAPY);

    @Nullable
    private final TreatmentCategory mappedCategory;
    @Nullable
    private final Set<String> mappedNames;

    TreatmentInput(@NotNull TreatmentCategory mappedCategory) {
        this(mappedCategory, null);
    }

    TreatmentInput(@NotNull Set<String> mappedNames) {
        this(null, mappedNames);
    }

    TreatmentInput(@Nullable final TreatmentCategory mappedCategory, @Nullable final Set<String> mappedNames) {
        this.mappedCategory = mappedCategory;
        this.mappedNames = mappedNames;
    }

    @Nullable
    public TreatmentCategory mappedCategory() {
        return mappedCategory;
    }

    @Nullable
    public Set<String> mappedNames() {
        return mappedNames;
    }

    @NotNull
    public String display() {
        return this.toString().replaceAll("_", " ").toLowerCase();
    }

    @NotNull
    public static TreatmentInput fromString(@NotNull String string) {
        return TreatmentInput.valueOf(string.trim().replaceAll(" ", "_").toUpperCase());
    }
}
