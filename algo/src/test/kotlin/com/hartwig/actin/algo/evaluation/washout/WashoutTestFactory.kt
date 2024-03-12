package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.clinical.datamodel.AtcClassification
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import java.time.LocalDate

internal object WashoutTestFactory {
    private val base = TestDataFactory.createMinimalTestPatientRecord()
    
    fun activeFromDate(referenceDate: LocalDate): MedicationStatusInterpreter {
        return object : MedicationStatusInterpreter {
            override fun interpret(medication: Medication): MedicationStatusInterpretation {
                val stopDate = medication.stopDate
                return if (stopDate == null || !referenceDate.isAfter(stopDate)) {
                    MedicationStatusInterpretation.ACTIVE
                } else {
                    MedicationStatusInterpretation.UNKNOWN
                }
            }
        }
    }

    fun medication(
        atc: AtcClassification? = null, stopDate: LocalDate? = null, name: String = "", isTrialMedication: Boolean = false
    ): Medication {
        return TestMedicationFactory.createMinimal().copy(
            atc = atc,
            stopDate = stopDate,
            name = name,
            isTrialMedication = isTrialMedication,
            status = MedicationStatus.ACTIVE
        )
    }

    fun withMedications(medications: List<Medication>): PatientRecord {
        return base.copy(clinical = base.clinical.copy(medications = medications))
    }
}