package com.hartwig.actin.clinical.curation.translation

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class LaboratoryTranslation : Translation {
    abstract fun code(): String
    abstract fun translatedCode(): String
    abstract fun name(): String
    abstract fun translatedName(): String
}