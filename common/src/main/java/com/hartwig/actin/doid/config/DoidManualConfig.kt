package com.hartwig.actin.doid.config

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class DoidManualConfig {
    abstract fun mainCancerDoids(): Set<String?>
    abstract fun adenoSquamousMappings(): Set<AdenoSquamousMapping?>
    abstract fun additionalDoidsPerDoid(): Map<String?, String?>
}
