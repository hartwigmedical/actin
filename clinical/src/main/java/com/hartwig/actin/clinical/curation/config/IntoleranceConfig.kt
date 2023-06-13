package com.hartwig.actin.clinical.curation.config

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class IntoleranceConfig : CurationConfig {
    abstract override fun input(): String
    override fun ignore(): Boolean {
        return false
    }

    abstract fun name(): String
    abstract fun doids(): Set<String?>
}