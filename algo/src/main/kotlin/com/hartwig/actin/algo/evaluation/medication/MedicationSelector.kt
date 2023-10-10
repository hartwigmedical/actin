package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter
import com.hartwig.actin.clinical.datamodel.CypInteraction
import com.hartwig.actin.clinical.datamodel.Medication
import java.time.LocalDate

class MedicationSelector(private val interpreter: MedicationStatusInterpreter) {
    fun active(medications: List<Medication>): List<Medication> {
        return medications.filter(::isActive)
    }

    fun activeOrRecentlyStopped(medications: List<Medication>, minStopDate: LocalDate): List<Medication> {
        return medications.filter { isActive(it) || isRecentlyStopped(it, minStopDate) }
    }

    fun activeWithAnyTermInName(medications: List<Medication>, termsToFind: Set<String>): List<Medication> {
        return active(medications).filter { stringCaseInsensitivelyMatchesQueryCollection(it.name(), termsToFind) }
    }

    fun activeWithCypInteraction(
        medications: List<Medication>,
        interactionToFind: String?,
        typeOfCyp: CypInteraction.Type
    ): List<Medication> {
        return active(medications).filter { medication ->
            medication.cypInteractions().any { (interactionToFind == null || interactionToFind == it.cyp()) && typeOfCyp == it.type() }
        }
    }

    fun activeOrRecentlyStoppedWithCypInteraction(
        medications: List<Medication>, interactionToFind: String?, typeOfCyp: CypInteraction.Type, minStopDate: LocalDate
    ): List<Medication> {
        return medications.filter { medication ->
            medication.cypInteractions().any { (interactionToFind == null || interactionToFind == it.cyp()) && typeOfCyp == it.type() }
        }
            .filter { isActive(it) || isRecentlyStopped(it, minStopDate) }
    }

    private fun isRecentlyStopped(medication: Medication, minStopDate: LocalDate): Boolean {
        return medication.stopDate()?.isAfter(minStopDate) ?: false
    }

    private fun isActive(medication: Medication): Boolean {
        return interpreter.interpret(medication) == MedicationStatusInterpretation.ACTIVE
    }
}