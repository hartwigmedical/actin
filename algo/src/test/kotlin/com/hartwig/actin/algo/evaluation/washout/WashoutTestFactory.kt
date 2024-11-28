package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.AtcClassification
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.MedicationStatus
import com.hartwig.actin.datamodel.clinical.TestMedicationFactory
import java.time.LocalDate

internal object WashoutTestFactory {

    private val base = TestPatientFactory.createMinimalTestWGSPatientRecord()

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
        atc: AtcClassification? = null,
        stopDate: LocalDate? = null,
        name: String = "",
        isTrialMedication: Boolean = false
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
        return base.copy(medications = medications)
    }
}