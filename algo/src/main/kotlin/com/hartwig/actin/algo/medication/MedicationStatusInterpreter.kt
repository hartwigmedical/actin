package com.hartwig.actin.algo.medication

import com.hartwig.actin.clinical.datamodel.Medication

interface MedicationStatusInterpreter {
    fun interpret(medication: Medication): MedicationStatusInterpretation
}