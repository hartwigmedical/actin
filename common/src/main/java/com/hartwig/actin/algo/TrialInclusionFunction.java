package com.hartwig.actin.algo;

import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.treatment.datamodel.InclusionCriterion;

import org.jetbrains.annotations.NotNull;

public interface TrialInclusionFunction {

    @NotNull
    InclusionCriterion criterion();

    boolean pass(@NotNull PatientRecord record);
}
