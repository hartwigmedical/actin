package com.hartwig.actin.clinical.curation.config

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class PrimaryTumorConfig : CurationConfig {
    abstract override fun input(): String
    override fun ignore(): Boolean {
        return false
    }

    abstract fun primaryTumorLocation(): String
    abstract fun primaryTumorSubLocation(): String
    abstract fun primaryTumorType(): String
    abstract fun primaryTumorSubType(): String
    abstract fun primaryTumorExtraDetails(): String
    abstract fun doids(): Set<String?>
}