package com.hartwig.actin.clinical.datamodel.treatment.history

import java.util.*

enum class StopReason {
    PROGRESSIVE_DISEASE,
    TOXICITY;

    companion object {
        fun createFromString(input: String): StopReason? {
            val uppercase = input.uppercase(Locale.getDefault())
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
