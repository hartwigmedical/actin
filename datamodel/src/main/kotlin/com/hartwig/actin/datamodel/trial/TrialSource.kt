package com.hartwig.actin.datamodel.trial

enum class TrialSource(val description: String, val isHospital: Boolean) {
    EXAMPLE("Example", true),
    EMC("Erasmus MC", true),
    NKI("NKI-AvL", true),
    LKO("Longkankeronderzoek", false);

    companion object {
        fun fromDescription(description: String?): TrialSource? {
            return TrialSource.entries.find { it.description == description }
        }
    }
}