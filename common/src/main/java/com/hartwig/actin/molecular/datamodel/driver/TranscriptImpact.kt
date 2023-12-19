package com.hartwig.actin.molecular.datamodel.driver

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class TranscriptImpact {
    abstract fun transcriptId(): String
    abstract fun hgvsCodingImpact(): String
    abstract fun hgvsProteinImpact(): String
    abstract fun affectedCodon(): Int?
    abstract fun affectedExon(): Int?

    @JvmField
    abstract val isSpliceRegion: Boolean
    abstract fun effects(): Set<VariantEffect?>
    abstract fun codingEffect(): CodingEffect?
}
