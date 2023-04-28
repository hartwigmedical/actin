package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.ImmutablePatientRecord
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableMedication
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import java.time.LocalDate

internal object WashoutTestFactory {
    fun activeFromDate(referenceDate: LocalDate): MedicationStatusInterpreter {
        return object : MedicationStatusInterpreter {
            override fun interpret(medication: Medication): MedicationStatusInterpretation {
                val stopDate = medication.stopDate()
                return if (stopDate == null || !referenceDate.isAfter(stopDate)) {
                    MedicationStatusInterpretation.ACTIVE
                } else {
                    MedicationStatusInterpretation.UNKNOWN
                }
            }
        }
    }

    fun builder(): ImmutableMedication.Builder {
        return TestMedicationFactory.builder().status(MedicationStatus.ACTIVE)
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