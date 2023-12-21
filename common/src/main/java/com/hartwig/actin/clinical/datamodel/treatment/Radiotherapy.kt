package com.hartwig.actin.clinical.datamodel.treatment

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class Radiotherapy : Treatment {
    val treatmentClass = TreatmentClass.RADIOTHERAPY

    @get:Value.Default
    override val isSystemic: Boolean
        get() = false

    override fun types(): Set<TreatmentType?> {
        val radiotherapyType = radioType()
        return if (radiotherapyType == null) emptySet<TreatmentType>() else java.util.Set.of<TreatmentType>(radiotherapyType)
    }

    override fun categories(): Set<TreatmentCategory> {
        return java.util.Set.of(TreatmentCategory.RADIOTHERAPY)
    }

    abstract fun radioType(): RadiotherapyType?

    @JvmField
    abstract val isInternal: Boolean?
}
