package com.hartwig.actin.clinical.datamodel

object TestMedicationFactory {

    fun createMinimal(): Medication {
        return Medication(
            name = "",
            qtProlongatingRisk = QTProlongatingRisk.NONE,
            dosage = Dosage(),
            isSelfCare = false,
            isTrialMedication = false
        )
    }
}
