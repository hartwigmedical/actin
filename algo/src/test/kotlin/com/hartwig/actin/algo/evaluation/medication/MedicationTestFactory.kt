package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory

internal object MedicationTestFactory {
    fun alwaysActive(): MedicationSelector {
        return createConstantSelector(MedicationStatusInterpretation.ACTIVE)
    }

    fun alwaysStopped(): MedicationSelector {
        return createConstantSelector(MedicationStatusInterpretation.STOPPED)
    }

    fun alwaysInactive(): MedicationSelector {
        return createConstantSelector(MedicationStatusInterpretation.CANCELLED)
    }

    fun alwaysPlanned(): MedicationSelector {
        return createConstantSelector(MedicationStatusInterpretation.PLANNED)
    }

    fun withMedications(medications: List<Medication>): PatientRecord {
        return ImmutablePatientRecord.builder()
            .from(TestDataFactory.createMinimalTestPatientRecord())
            .clinical(
                ImmutableClinicalRecord.builder()
                    .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                    .medications(medications)
                    .build()
            )
            .build()
    }

    private fun createConstantSelector(medicationStatusInterpretation: MedicationStatusInterpretation): MedicationSelector {
        return MedicationSelector(object : MedicationStatusInterpreter {
            override fun interpret(medication: Medication): MedicationStatusInterpretation {
                return medicationStatusInterpretation
            }
        })
    }
}