package com.hartwig.actin.clinical.datamodel

import org.apache.logging.log4j.util.Strings

object TestMedicationFactory {
    @JvmStatic
    fun builder(): ImmutableMedication.Builder {
        return ImmutableMedication.builder()
            .name(Strings.EMPTY)
            .qtProlongatingRisk(QTProlongatingRisk.NONE)
            .dosage(ImmutableDosage.builder().build())
            .isSelfCare(false)
            .isTrialMedication(false)
    }
}
