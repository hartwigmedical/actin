package com.hartwig.actin.clinical.curation.config

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class ToxicityConfig : CurationConfig {
    abstract override fun input(): String
    abstract override fun ignore(): Boolean
    abstract fun name(): String
    abstract fun categories(): Set<String?>
    abstract fun grade(): Int?
}