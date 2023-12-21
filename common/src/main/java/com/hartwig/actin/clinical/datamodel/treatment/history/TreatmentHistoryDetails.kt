package com.hartwig.actin.clinical.datamodel.treatment.history

import com.hartwig.actin.clinical.datamodel.BodyLocationCategory
import com.hartwig.actin.clinical.datamodel.ObservedToxicity
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.time.LocalDate

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class TreatmentHistoryDetails {
    abstract fun stopYear(): Int?
    abstract fun stopMonth(): Int?
    abstract fun ongoingAsOf(): LocalDate?
    abstract fun cycles(): Int?
    abstract fun bestResponse(): TreatmentResponse?
    abstract fun stopReason(): StopReason?
    abstract fun stopReasonDetail(): String?
    abstract fun toxicities(): Set<ObservedToxicity?>?
    abstract fun bodyLocationCategories(): Set<BodyLocationCategory?>?
    abstract fun bodyLocations(): Set<String?>?
    abstract fun switchToTreatments(): List<TreatmentStage?>?
    abstract fun maintenanceTreatment(): TreatmentStage?
}
