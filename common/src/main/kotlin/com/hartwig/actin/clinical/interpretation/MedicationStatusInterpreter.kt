package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.datamodel.clinical.Medication

interface MedicationStatusInterpreter {

    fun interpret(medication: Medication): MedicationStatusInterpretation
}