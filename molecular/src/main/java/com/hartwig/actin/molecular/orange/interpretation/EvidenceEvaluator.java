package com.hartwig.actin.molecular.orange.interpretation;

import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;

import org.jetbrains.annotations.NotNull;

public interface EvidenceEvaluator {

    boolean isPotentiallyForTrialInclusion(@NotNull ProtectEvidence evidence);
}
