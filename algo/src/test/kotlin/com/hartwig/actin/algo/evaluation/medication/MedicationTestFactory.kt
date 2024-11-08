package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.clinical.AtcClassification
import com.hartwig.actin.datamodel.clinical.DrugInteraction
import com.hartwig.actin.datamodel.clinical.Dosage
import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.QTProlongatingRisk
import com.hartwig.actin.datamodel.clinical.TestMedicationFactory
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
        return TestPatientFactory.createMinimalTestWGSPatientRecord().copy(medications = medications)
    }

    fun withCypInteraction(cyp: String, type: DrugInteraction.Type, strength: DrugInteraction.Strength): PatientRecord {
        return withMedications(listOf(medicationWithCypInteraction(cyp, type, strength)))
    }

    fun withTransporterInteraction(transporter: String, type: DrugInteraction.Type, strength: DrugInteraction.Strength): PatientRecord {
        return withMedications(listOf(medicationWithTransporterInteraction(transporter, type, strength)))
    }

    fun medicationWithCypInteraction(
        cyp: String, type: DrugInteraction.Type, strength: DrugInteraction.Strength, stopDate: LocalDate? = null, name: String = ""
    ): Medication {
        return TestMedicationFactory.createMinimal().copy(
            cypInteractions = listOf(DrugInteraction(name = cyp, type = type, strength = strength)), stopDate = stopDate, name = name
        )
    }

    fun medicationWithTransporterInteraction(
        transporter: String, type: DrugInteraction.Type, strength: DrugInteraction.Strength, stopDate: LocalDate? = null, name: String = ""
    ): Medication {
        return TestMedicationFactory.createMinimal().copy(
            transporterInteractions = listOf(DrugInteraction(name = transporter, type = type, strength = strength)),
            stopDate = stopDate,
            name = name
        )
    }

    fun medication(
        name: String = "",
        dosage: Dosage = Dosage(),
        startDate: LocalDate? = null,
        stopDate: LocalDate? = null,
        qtProlongatingRisk: QTProlongatingRisk = QTProlongatingRisk.NONE,
        atc: AtcClassification? = null,
        isSelfCare: Boolean = false
    ): Medication {
        return TestMedicationFactory.createMinimal().copy(
            name = name, dosage = dosage, startDate = startDate, stopDate = stopDate, qtProlongatingRisk = qtProlongatingRisk, atc = atc, isSelfCare = isSelfCare
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