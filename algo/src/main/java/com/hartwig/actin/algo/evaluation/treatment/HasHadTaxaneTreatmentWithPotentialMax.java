package com.hartwig.actin.algo.evaluation.treatment;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.common.collect.Sets;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.evaluation.util.PassOrFailEvaluator;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;

import org.jetbrains.annotations.NotNull;

public class HasHadTaxaneTreatmentWithPotentialMax implements PassOrFailEvaluator {

    static final Set<String> TAXANE_TREATMENTS = Sets.newHashSet();

    static {
        TAXANE_TREATMENTS.add("Paclitaxel");
        TAXANE_TREATMENTS.add("Docetaxel");
        TAXANE_TREATMENTS.add("Cabazitaxel");
    }

    @Nullable
    private final Integer maxTaxaneTreatments;

    HasHadTaxaneTreatmentWithPotentialMax(@Nullable final Integer maxTaxaneTreatments) {
        this.maxTaxaneTreatments = maxTaxaneTreatments;
    }

    @Override
    public boolean isPass(@NotNull PatientRecord record) {
        int numTaxaneTreatments = 0;
        for (PriorTumorTreatment treatment : record.clinical().priorTumorTreatments()) {
            if (TAXANE_TREATMENTS.contains(treatment.name())) {
                numTaxaneTreatments++;
            }
        }

        if (maxTaxaneTreatments == null) {
            return numTaxaneTreatments > 0;
        } else {
            return numTaxaneTreatments > 0 && numTaxaneTreatments <= maxTaxaneTreatments;
        }
    }

    @NotNull
    @Override
    public String passMessage() {
        if (maxTaxaneTreatments == null) {
            return "Patient has received taxane treatment";
        } else {
            return "Patient has received taxane treatment but not more than " + maxTaxaneTreatments + " lines";
        }
    }

    @NotNull
    @Override
    public String failMessage() {
        if (maxTaxaneTreatments == null) {
            return "Patient has not received taxane treatment";
        } else {
            return "Patient has not received taxane treatment or more than " + maxTaxaneTreatments + " lines";
        }
    }
}
