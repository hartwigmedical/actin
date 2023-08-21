package com.hartwig.actin.report.interpretation

import com.google.common.collect.Multimap
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class PriorMolecularTestInterpretation {
    abstract fun textBasedPriorTests(): Multimap<PriorMolecularTestKey, PriorMolecularTest?>
    abstract fun valueBasedPriorTests(): Set<PriorMolecularTest?>
}