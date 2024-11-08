package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.clinical.DrugInteraction
import com.hartwig.actin.datamodel.clinical.Medication
import java.time.LocalDate

class MedicationSelector(private val interpreter: MedicationStatusInterpreter) {
    fun active(medications: List<Medication>): List<Medication> {
        return medications.filter(::isActive)
    }

    fun planned(medications: List<Medication>): List<Medication> {
        return medications.filter(::isPlanned)
    }

    fun activeOrRecentlyStopped(medications: List<Medication>, minStopDate: LocalDate): List<Medication> {
        return medications.filter { isActive(it) || isRecentlyStopped(it, minStopDate) }
    }

    fun activeWithAnyTermInName(medications: List<Medication>, termsToFind: Set<String>): List<Medication> {
        return active(medications).filter { stringCaseInsensitivelyMatchesQueryCollection(it.name, termsToFind) }
    }

    fun plannedWithAnyTermInName(medications: List<Medication>, termsToFind: Set<String>): List<Medication> {
        return planned(medications).filter { stringCaseInsensitivelyMatchesQueryCollection(it.name, termsToFind) }
    }

    fun activeWithInteraction(
        medications: List<Medication>,
        interactionToFind: String?,
        typeOfInteraction: DrugInteraction.Type,
        name: String
    ): List<Medication> {
        return active(medications).filter { medication ->
            when (name) {
                "CYP" -> medication.cypInteractions.any { (interactionToFind == null || interactionToFind == it.name) && typeOfInteraction == it.type }
                "transporter" -> medication.transporterInteractions.any { (interactionToFind == null || interactionToFind == it.name) && typeOfInteraction == it.type }
                else -> throw IllegalArgumentException("Unknown interaction name: $name")
            }
        }
    }

    fun plannedWithInteraction(
        medications: List<Medication>,
        interactionToFind: String?,
        typeOfInteraction: DrugInteraction.Type,
        name: String
    ): List<Medication> {
        return planned(medications).filter { medication ->
            when (name) {
                "CYP" -> medication.cypInteractions.any { (interactionToFind == null || interactionToFind == it.name) && typeOfInteraction == it.type }
                "transporter" -> medication.transporterInteractions.any { (interactionToFind == it.name) && typeOfInteraction == it.type }
                else -> throw IllegalArgumentException("Unknown interaction name: $name")
            }
        }
    }

    fun activeOrRecentlyStoppedWithCypInteraction(
        medications: List<Medication>, interactionToFind: String?, typeOfCyp: DrugInteraction.Type, minStopDate: LocalDate
    ): List<Medication> {
        return medications.filter { medication ->
            medication.cypInteractions.any { (interactionToFind == null || interactionToFind == it.name) && typeOfCyp == it.type }
        }
            .filter { isActive(it) || isRecentlyStopped(it, minStopDate) }
    }

    private fun isRecentlyStopped(medication: Medication, minStopDate: LocalDate): Boolean {
        return medication.stopDate?.isAfter(minStopDate) ?: false
    }

    fun isActive(medication: Medication): Boolean {
        return interpreter.interpret(medication) == MedicationStatusInterpretation.ACTIVE
    }

    fun isPlanned(medication: Medication): Boolean {
        return interpreter.interpret(medication) == MedicationStatusInterpretation.PLANNED
    }
}