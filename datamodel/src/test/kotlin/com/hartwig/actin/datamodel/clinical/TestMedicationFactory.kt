package com.hartwig.actin.datamodel.clinical

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
