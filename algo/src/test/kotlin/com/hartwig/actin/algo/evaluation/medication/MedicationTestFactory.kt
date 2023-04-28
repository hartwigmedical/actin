package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory

internal object MedicationTestFactory {
    fun alwaysActive(): MedicationSelector {
        return MedicationSelector { MedicationStatusInterpretation.ACTIVE }
    }

    fun alwaysStopped(): MedicationSelector {
        return MedicationSelector { MedicationStatusInterpretation.STOPPED }
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
}