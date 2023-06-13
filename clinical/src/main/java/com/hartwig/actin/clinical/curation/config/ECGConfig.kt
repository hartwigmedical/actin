package com.hartwig.actin.clinical.curation.config

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class ECGConfig : CurationConfig {
    abstract override fun input(): String
    abstract override fun ignore(): Boolean
    abstract fun interpretation(): String

    @JvmField
    abstract val isQTCF: Boolean
    abstract fun qtcfValue(): Int?
    abstract fun qtcfUnit(): String?

    @JvmField
    abstract val isJTC: Boolean
    abstract fun jtcValue(): Int?
    abstract fun jtcUnit(): String?
}