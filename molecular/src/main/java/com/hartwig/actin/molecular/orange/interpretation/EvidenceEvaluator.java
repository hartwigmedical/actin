package com.hartwig.actin.molecular.orange.interpretation;

import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;

import org.jetbrains.annotations.NotNull;

public interface EvidenceEvaluator {

    boolean isPotentiallyForTrialInclusion(@NotNull TreatmentEvidence evidence);
}
