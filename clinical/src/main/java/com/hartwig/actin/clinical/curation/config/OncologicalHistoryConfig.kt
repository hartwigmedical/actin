package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class OncologicalHistoryConfig : CurationConfig {
    abstract override fun input(): String
    abstract override fun ignore(): Boolean
    abstract fun curated(): PriorTumorTreatment?
}