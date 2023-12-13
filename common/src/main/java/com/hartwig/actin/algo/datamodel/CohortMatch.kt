package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.treatment.datamodel.CohortMetadata
import com.hartwig.actin.treatment.datamodel.Eligibility
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class CohortMatch {
    abstract fun metadata(): CohortMetadata

    @JvmField
    abstract val isPotentiallyEligible: Boolean
    abstract fun evaluations(): Map<Eligibility?, Evaluation>
}
