package com.hartwig.actin.clinical.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class AtcClassification {
    abstract fun anatomicalMainGroup(): AtcLevel
    abstract fun therapeuticSubGroup(): AtcLevel
    abstract fun pharmacologicalSubGroup(): AtcLevel
    abstract fun chemicalSubGroup(): AtcLevel
    abstract fun chemicalSubstance(): AtcLevel?
    fun allLevels(): Set<AtcLevel> {
        return Stream.concat(
            Optional.ofNullable(chemicalSubstance()).stream(),
            Stream.of(anatomicalMainGroup(), therapeuticSubGroup(), pharmacologicalSubGroup(), chemicalSubGroup())
        )
            .collect(Collectors.toSet())
    }
}
