package com.hartwig.actin.treatment.input.single

import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class OneTreatmentCategoryManyTypesOneInteger() {
    abstract fun category(): TreatmentCategory
    abstract fun types(): Set<TreatmentType?>
    abstract fun integer(): Int
}
