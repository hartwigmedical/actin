package com.hartwig.actin.algo.evaluation.treatment;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasHadTreatmentCategory implements EvaluationFunction {

    @NotNull
    private final TreatmentCategory category;
    @Nullable
    private final String type;

    HasHadTreatmentCategory(@NotNull final TreatmentCategory category, @Nullable final String type) {
        this.category = category;
        this.type = type;
    }

    @NotNull
    @Override
    public EvaluationResult evaluate(@NotNull PatientRecord record) {
        boolean hasHadDrugCategory = false;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (treatment.categories().contains(category)) {
                if (type == null || isOfType(treatment, category, type)) {
                    hasHadDrugCategory = true;
                }
            }
        }

        return hasHadDrugCategory ? EvaluationResult.PASS : EvaluationResult.FAIL;
    }

    private static boolean isOfType(@NotNull PriorTumorTreatment treatment, @NotNull TreatmentCategory category,
            @NotNull String termToFind) {
        String type = null;
        switch (category) {
            case CHEMOTHERAPY: {
                type = treatment.chemoType();
                break;
            }
            case IMMUNOTHERAPY: {
                type = treatment.immunoType();
                break;
            }
            case TARGETED_THERAPY: {
                type = treatment.targetedType();
                break;
            }
            case HORMONE_THERAPY: {
                type = treatment.hormoneType();
                break;
            }
            case TRANSPLANTATION: {
                type = treatment.stemCellTransType();
                break;
            }
        }

        return type != null && type.contains(termToFind);
    }
}
