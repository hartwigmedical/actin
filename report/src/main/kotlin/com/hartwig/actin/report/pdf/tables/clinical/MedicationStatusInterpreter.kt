package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.clinical.datamodel.Medication

// TODO (CB): move this class and the copy in algo and the test fils to common module once common is converted to kotlin
interface MedicationStatusInterpreter {
    fun interpret(medication: Medication): MedicationStatusInterpretation
}