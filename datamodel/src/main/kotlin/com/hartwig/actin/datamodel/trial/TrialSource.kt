package com.hartwig.actin.datamodel.trial

enum class TrialSource(val description: String) {
    EXAMPLE("Example"),
    EMC("Erasmus MC"),
    NKI("NKI-AvL"),
    LKO("Longkankeronderzoek");

    companion object {
        fun fromDescription(description: String?): TrialSource? {
            return TrialSource.entries.find { it.description == description }
        }
    }
}