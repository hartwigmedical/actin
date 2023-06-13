package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.util.*

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class NonOncologicalHistoryConfig : CurationConfig {
    abstract override fun input(): String
    abstract override fun ignore(): Boolean
    abstract fun lvef(): Optional<Double?>?
    abstract fun priorOtherCondition(): Optional<PriorOtherCondition?>?
}