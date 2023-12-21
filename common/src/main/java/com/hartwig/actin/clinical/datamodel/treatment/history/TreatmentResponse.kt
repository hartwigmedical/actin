package com.hartwig.actin.clinical.datamodel.treatment.history

import java.util.*

enum class TreatmentResponse {
    PROGRESSIVE_DISEASE,
    STABLE_DISEASE,
    MIXED,
    PARTIAL_RESPONSE,
    COMPLETE_RESPONSE,
    REMISSION;

    companion object {
        fun createFromString(input: String): TreatmentResponse? {
            return when (input.uppercase(Locale.getDefault())) {
                "PD" -> PROGRESSIVE_DISEASE
                "SD" -> STABLE_DISEASE
                "MIXED" -> MIXED
                "PR" -> PARTIAL_RESPONSE
                "CR" -> COMPLETE_RESPONSE
                "REMISSION" -> REMISSION
                else -> null
            }
        }
    }
}
