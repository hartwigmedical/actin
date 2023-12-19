package com.hartwig.actin.molecular.datamodel.driver

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class MolecularDrivers {
    abstract fun variants(): Set<Variant?>
    abstract fun copyNumbers(): Set<CopyNumber?>
    abstract fun homozygousDisruptions(): Set<HomozygousDisruption?>
    abstract fun disruptions(): Set<Disruption?>
    abstract fun fusions(): Set<Fusion?>
    abstract fun viruses(): Set<Virus?>
}
