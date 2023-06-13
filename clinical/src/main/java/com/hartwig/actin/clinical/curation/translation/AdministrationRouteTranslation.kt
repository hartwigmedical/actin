package com.hartwig.actin.clinical.curation.translation

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class AdministrationRouteTranslation : Translation {
    abstract fun administrationRoute(): String
    abstract fun translatedAdministrationRoute(): String
}