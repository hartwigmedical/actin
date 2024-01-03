package com.hartwig.actin.clinical.datamodel.treatment.history

import com.hartwig.actin.clinical.datamodel.BodyLocationCategory
import com.hartwig.actin.clinical.datamodel.ObservedToxicity
import java.time.LocalDate

data class TreatmentHistoryDetails(
    val stopYear: Int?,
    val stopMonth: Int?,
    val ongoingAsOf: LocalDate?,
    val cycles: Int?,
    val bestResponse: TreatmentResponse?,
    val stopReason: StopReason?,
    val stopReasonDetail: String?,
    val switchToTreatments: List<TreatmentStage>?,
    val maintenanceTreatment: TreatmentStage?,
    val toxicities: Set<ObservedToxicity>? = null,
    val bodyLocationCategories: Set<BodyLocationCategory>? = null,
    val bodyLocations: Set<String>? = null
)
