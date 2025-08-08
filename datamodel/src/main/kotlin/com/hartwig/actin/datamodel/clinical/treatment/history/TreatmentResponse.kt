package com.hartwig.actin.datamodel.clinical.treatment.history

import com.hartwig.actin.datamodel.Displayable

enum class TreatmentResponse : Displayable {
    PROGRESSIVE_DISEASE,
    STABLE_DISEASE,
    MIXED,
    PARTIAL_RESPONSE,
    NEAR_COMPLETE_RESPONSE,
    COMPLETE_RESPONSE,
    REMISSION;

    override fun display(): String {
        return this.toString().replace("_", " ").lowercase()
    }

    companion object {
        val BENEFIT_RESPONSES = setOf(PARTIAL_RESPONSE, NEAR_COMPLETE_RESPONSE, COMPLETE_RESPONSE, REMISSION)

        fun fromString(string: String): TreatmentResponse {
            return TreatmentResponse.valueOf(
                string.trim { it <= ' ' }.replace(" ".toRegex(), "_").uppercase()
            )
        }
    }
}
