package com.hartwig.actin.datamodel.trial

enum class TrialSource(val description: String) {
    EMC("Erasmus MC"),
    NKI("NKI-Avl"),
    LKO("Longkankeronderzoek");

    companion object {
        fun fromDescription(description: String?): TrialSource? {
            return TrialSource.entries.find { it.description == description }
        }
    }
}