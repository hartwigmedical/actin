package com.hartwig.actin.molecular.orange.evidence.curation

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class ExternalTrialMapping {
    abstract fun externalTrial(): String
    abstract fun actinTrial(): String
}
