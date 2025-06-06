package com.hartwig.actin.datamodel.clinical.treatment.history

import com.hartwig.actin.datamodel.clinical.BodyLocationCategory
import com.hartwig.actin.datamodel.clinical.ObservedToxicity

data class TreatmentHistoryDetails(
    val stopYear: TreatmentYear,
    val stopMonth: TreatmentMonth,
    val cycles: Int? = null,
    val bestResponse: TreatmentResponse? = null,
    val stopReason: StopReason? = null,
    val stopReasonDetail: String? = null,
    val switchToTreatments: List<TreatmentStage>? = null,
    val maintenanceTreatment: TreatmentStage? = null,
    val toxicities: Set<ObservedToxicity>? = null,
    val bodyLocationCategories: Set<BodyLocationCategory>? = null,
    val bodyLocations: Set<String>? = null
)
