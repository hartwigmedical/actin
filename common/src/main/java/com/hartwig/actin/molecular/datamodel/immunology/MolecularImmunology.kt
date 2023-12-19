package com.hartwig.actin.molecular.datamodel.immunology

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class MolecularImmunology {
    abstract val isReliable: Boolean
    abstract fun hlaAlleles(): Set<HlaAllele?>
}
