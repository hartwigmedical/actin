package com.hartwig.actin.clinical.datamodel.treatment

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class OtherTreatment : Treatment {
    val treatmentClass = TreatmentClass.OTHER_TREATMENT

    @Value.Default
    override fun types(): Set<TreatmentType?> {
        return emptySet<TreatmentType>()
    }
}
