package com.hartwig.actin.molecular.datamodel.characteristics

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class PredictedTumorOrigin {
    @Value.Derived
    fun cancerType(): String {
        return bestPrediction().cancerType()
    }

    @Value.Derived
    fun likelihood(): Double {
        return bestPrediction().likelihood()
    }

    abstract fun predictions(): List<CupPrediction>
    private fun bestPrediction(): CupPrediction {
        return predictions().stream().max(Comparator.comparing { obj: CupPrediction -> obj.likelihood() }).orElseThrow()
    }
}
