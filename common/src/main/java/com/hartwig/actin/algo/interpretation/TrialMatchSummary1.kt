package com.hartwig.actin.algo.interpretation

import com.hartwig.actin.treatment.datamodel.CohortMetadata
import com.hartwig.actin.treatment.datamodel.TrialIdentification
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class TrialMatchSummary {
    abstract fun trialCount(): Int
    abstract fun cohortCount(): Int
    abstract fun eligibleTrialMap(): Map<TrialIdentification?, List<CohortMetadata?>?>
}
