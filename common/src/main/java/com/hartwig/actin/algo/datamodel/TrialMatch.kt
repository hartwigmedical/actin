package com.hartwig.actin.algo.datamodel

import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.TrialIdentification
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class TrialMatch {
    abstract fun identification(): TrialIdentification

    @JvmField
    abstract val isPotentiallyEligible: Boolean
    abstract fun evaluations(): Map<Eligibility?, Evaluation>
    abstract fun cohorts(): List<CohortMatch?>
}
