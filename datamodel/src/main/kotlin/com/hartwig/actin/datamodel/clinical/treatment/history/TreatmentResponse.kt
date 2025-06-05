package com.hartwig.actin.datamodel.clinical.treatment.history

enum class TreatmentResponse {
    PROGRESSIVE_DISEASE,
    STABLE_DISEASE,
    MIXED,
    PARTIAL_RESPONSE,
    NEAR_COMPLETE_RESPONSE,
    COMPLETE_RESPONSE,
    REMISSION;

    companion object {
        fun createFromString(input: String): TreatmentResponse? {
            return when (input.uppercase()) {
                "PD" -> PROGRESSIVE_DISEASE
                "SD" -> STABLE_DISEASE
                "MIXED" -> MIXED
                "PR" -> PARTIAL_RESPONSE
                "NEAR CR" -> NEAR_COMPLETE_RESPONSE
                "CR" -> COMPLETE_RESPONSE
                "REMISSION" -> REMISSION
                else -> null
            }
        }
    }
}
