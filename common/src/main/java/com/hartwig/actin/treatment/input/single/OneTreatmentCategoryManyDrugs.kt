package com.hartwig.actin.treatment.input.single

import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class OneTreatmentCategoryManyDrugs() {
    abstract fun category(): TreatmentCategory
    abstract fun drugs(): Set<Drug?>
}
