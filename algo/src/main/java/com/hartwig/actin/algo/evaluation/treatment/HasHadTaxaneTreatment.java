package com.hartwig.actin.algo.evaluation.treatment;

import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.NotNull;

public class HasHadTaxaneTreatment implements PassOrFailEvaluator {

    static final Set<String> TAXANE_TREATMENTS = Sets.newHashSet();

    static {
        TAXANE_TREATMENTS.add("Paclitaxel");
        TAXANE_TREATMENTS.add("Docetaxel");
        TAXANE_TREATMENTS.add("Cabazitaxel");
    }

    HasHadTaxaneTreatment() {
    }

    @Override
    public boolean isPass(@NotNull final PatientRecord record) {
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (TAXANE_TREATMENTS.contains(treatment.name())) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    @Override
    public String passMessage() {
        return "Patient has received taxane treatment";
    }

    @NotNull
    @Override
    public String failMessage() {
        return "Patient has not received taxane treatment";
    }
}
