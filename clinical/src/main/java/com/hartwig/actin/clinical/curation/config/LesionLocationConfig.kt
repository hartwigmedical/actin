package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.datamodel.LesionLocationCategory
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class LesionLocationConfig : CurationConfig {
    abstract override fun input(): String
    override fun ignore(): Boolean {
        return false
    }

    abstract fun location(): String
    abstract fun category(): LesionLocationCategory?
}