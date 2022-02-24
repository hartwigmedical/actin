package com.hartwig.actin.algo.evaluation.treatment;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.NotNull;

public class HasHadFluoropyrimidineTreatment implements PassOrFailEvaluator {

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

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (FLUOROPYRIMIDINE_TREATMENTS.contains(treatment.name())) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient has received fluoropyrimidine treatment";
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient has not received fluoropyrimidine treatment";
    }
}
