package com.hartwig.actin.datamodel.clinical.treatment.history

enum class StopReason {
    PROGRESSIVE_DISEASE,
    TOXICITY;

    companion object {
        fun createFromString(input: String): StopReason? {
            val uppercase = input.uppercase()
            return if (uppercase.contains("PD") || uppercase.contains("PROGRESSIVE")) {
                PROGRESSIVE_DISEASE
            } else if (uppercase.contains("TOXICITY")) {
                TOXICITY
            } else {
                null
            }
        }
    }
}
