package com.hartwig.actin.clinical.datamodel.treatment

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.util.stream.Collectors

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class DrugTreatment : Treatment {
    val treatmentClass = TreatmentClass.DRUG_TREATMENT

    @get:Value.Default
    override val isSystemic: Boolean
        get() = true

    abstract fun drugs(): Set<Drug>
    override fun categories(): Set<TreatmentCategory> {
        return drugs().stream().map { obj: Drug -> obj.category() }.collect(Collectors.toSet())
    }

    override fun types(): Set<TreatmentType?> {
        return drugs().stream().flatMap { drug: Drug -> drug.drugTypes().stream() }.collect(Collectors.toSet())
    }

    abstract fun maxCycles(): Int?
}
