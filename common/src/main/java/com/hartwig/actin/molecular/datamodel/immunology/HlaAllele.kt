package com.hartwig.actin.molecular.datamodel.immunology

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class HlaAllele {
    abstract fun name(): String
    abstract fun tumorCopyNumber(): Double
    abstract fun hasSomaticMutations(): Boolean
}
