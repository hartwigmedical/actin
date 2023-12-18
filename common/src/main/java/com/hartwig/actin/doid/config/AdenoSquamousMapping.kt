package com.hartwig.actin.doid.config

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class AdenoSquamousMapping {
    abstract fun adenoSquamousDoid(): String
    abstract fun squamousDoid(): String
    abstract fun adenoDoid(): String
}
