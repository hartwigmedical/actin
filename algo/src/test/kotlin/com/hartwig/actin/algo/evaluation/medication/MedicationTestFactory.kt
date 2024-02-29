package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.TestDataFactory
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.clinical.datamodel.AtcClassification
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.Dosage
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import java.time.LocalDate

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
        return TestDataFactory.createMinimalTestPatientRecord().copy(
            clinical = TestClinicalFactory.createMinimalTestClinicalRecord().copy(medications = medications)
        )
    }

    fun withCypInteraction(cyp: String, type: CypInteraction.Type, strength: CypInteraction.Strength): PatientRecord {
        return withMedications(listOf(medicationWithCypInteraction(cyp, type, strength)))
    }

    fun medicationWithCypInteraction(
        cyp: String, type: CypInteraction.Type, strength: CypInteraction.Strength, stopDate: LocalDate? = null, name: String = ""
    ): Medication {
        return TestMedicationFactory.createMinimal().copy(
            cypInteractions = listOf(CypInteraction(cyp = cyp, type = type, strength = strength)), stopDate = stopDate, name = name
        )
    }

    fun medication(
        name: String = "",
        dosage: Dosage = Dosage(),
        stopDate: LocalDate? = null,
        qtProlongatingRisk: QTProlongatingRisk = QTProlongatingRisk.NONE,
        atc: AtcClassification? = null,
        isSelfCare: Boolean = false
    ): Medication {
        return TestMedicationFactory.createMinimal().copy(
            name = name, dosage = dosage, stopDate = stopDate, qtProlongatingRisk = qtProlongatingRisk, atc = atc, isSelfCare = isSelfCare
        )
    }

    private fun createConstantSelector(medicationStatusInterpretation: MedicationStatusInterpretation): MedicationSelector {
        return MedicationSelector(object : MedicationStatusInterpreter {
            override fun interpret(medication: Medication): MedicationStatusInterpretation {
                return medicationStatusInterpretation
            }
        })
    }
}