package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.CypInteraction
import java.time.LocalDate

class MedicationSelector(private val interpreter: MedicationStatusInterpreter) {
    fun active(medications: List<Medication>): List<Medication> {
        return medications.filter(::isActive)
    }

    fun activeWithAnyTermInName(medications: List<Medication>, termsToFind: Set<String>): List<Medication> {
        return active(medications).filter { stringCaseInsensitivelyMatchesQueryCollection(it.name(), termsToFind) }
    }

    fun activeWithExactCategory(medications: List<Medication>, categoryToFind: String): List<Medication> {
        return activeWithAnyExactCategory(medications, setOf(categoryToFind))
    }

    fun activeWithAnyExactCategory(medications: List<Medication>, categoriesToFind: Set<String>): List<Medication> {
        val lowercaseCategoriesToFind = categoriesToFind.map { it.lowercase() }.toSet()
        return active(medications).filter { medication ->
            medication.categories().any { lowercaseCategoriesToFind.contains(it.lowercase()) }
        }
    }

    fun activeOrRecentlyStoppedWithCategory(
        medications: List<Medication>, categoryToFind: String, minStopDate: LocalDate
    ): List<Medication> {
        return medications.filter { medication ->
            medication.categories().any { it.equals(categoryToFind, ignoreCase = true) }
        }
            .filter { isActive(it) || isRecentlyStopped(it, minStopDate) }
    }

    fun activeWithCYPInteraction(
        medications: List<Medication>,
        interactionToFind: String?,
        typeOfCYP: CypInteraction.Type
    ): List<Medication> {
        return active(medications).filter { medication ->
            medication.cypInteractions().any { (interactionToFind == null || interactionToFind == it.cyp()) && typeOfCYP == it.type() }
        }
    }

    fun activeOrRecentlyStoppedWithCYPInteraction(
        medications: List<Medication>, interactionToFind: String?, typeOfCYP: CypInteraction.Type, minStopDate: LocalDate
    ): List<Medication> {
        return medications.filter { medication ->
            medication.cypInteractions().any { (interactionToFind == null || interactionToFind == it.cyp()) && typeOfCYP == it.type() }
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