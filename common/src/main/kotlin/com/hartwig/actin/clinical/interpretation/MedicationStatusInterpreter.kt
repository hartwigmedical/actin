package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.clinical.datamodel.Medication

interface MedicationStatusInterpreter {

    fun interpret(medication: Medication): MedicationStatusInterpretation
}