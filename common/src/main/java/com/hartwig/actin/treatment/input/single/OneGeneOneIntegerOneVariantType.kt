package com.hartwig.actin.treatment.input.single

import com.hartwig.actin.treatment.input.datamodel.VariantTypeInput
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class])
abstract class OneGeneOneIntegerOneVariantType() {
    abstract fun geneName(): String
    abstract fun integer(): Int
    abstract fun variantType(): VariantTypeInput
}
