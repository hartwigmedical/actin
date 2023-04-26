package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class TreatmentTypeResolver {

    private TreatmentTypeResolver() {
    }

    public static boolean isOfType(@NotNull PriorTumorTreatment treatment, @NotNull TreatmentCategory category,
            @NotNull String typeToFind) {
        String type = resolveType(treatment, category);

        return type != null && type.toLowerCase().contains(typeToFind.toLowerCase());
    }

    public static boolean hasTypeConfigured(@NotNull PriorTumorTreatment treatment, @NotNull TreatmentCategory category) {
        String type = resolveType(treatment, category);
        return type != null && !type.isEmpty();
    }

    @Nullable
    private static String resolveType(@NotNull PriorTumorTreatment treatment, @NotNull TreatmentCategory category) {
        switch (category) {
            case CHEMOTHERAPY: {
                return treatment.chemoType();
            }
            case IMMUNOTHERAPY: {
                return treatment.immunoType();
            }
            case TARGETED_THERAPY: {
                return treatment.targetedType();
            }
            case HORMONE_THERAPY: {
                return treatment.hormoneType();
            }
            case RADIOTHERAPY: {
                return treatment.radioType();
            }
            case TRANSPLANTATION: {
                return treatment.transplantType();
            }
            case SUPPORTIVE_TREATMENT: {
                return treatment.supportiveType();
            }
            case CAR_T: {
                return treatment.carTType();
            }
            case TRIAL: {
                return treatment.trialAcronym();
            }
            default: {
                return null;
            }
        }
    }
}
