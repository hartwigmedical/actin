package com.hartwig.actin.algo.eligibility;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.algo.datamodel.EligibilityCriterion;

import org.jetbrains.annotations.NotNull;

public interface EligibilityFunction {

    @NotNull
    EligibilityCriterion criterion();

    boolean pass(@NotNull PatientRecord record);
}
