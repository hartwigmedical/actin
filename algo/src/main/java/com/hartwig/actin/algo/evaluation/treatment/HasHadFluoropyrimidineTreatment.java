package com.hartwig.actin.algo.evaluation.treatment;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.evaluation.EvaluationFunction;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.NotNull;

public class HasHadFluoropyrimidineTreatment implements EvaluationFunction {

    static final Set<String> FLUOROPYRIMIDINE_TREATMENTS = Sets.newHashSet();

    static {
        FLUOROPYRIMIDINE_TREATMENTS.add("Capecitabine");
        FLUOROPYRIMIDINE_TREATMENTS.add("Carmofur");
        FLUOROPYRIMIDINE_TREATMENTS.add("Doxifluridine");
        FLUOROPYRIMIDINE_TREATMENTS.add("Fluorouracil");
        FLUOROPYRIMIDINE_TREATMENTS.add("Tegafur");
    }

    HasHadFluoropyrimidineTreatment() {
    }

    @NotNull
    @Override
    public Evaluation evaluate(@NotNull PatientRecord record) {
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (FLUOROPYRIMIDINE_TREATMENTS.contains(treatment.name())) {
                return Evaluation.PASS;
            }
        }

        return Evaluation.FAIL;
    }
}
