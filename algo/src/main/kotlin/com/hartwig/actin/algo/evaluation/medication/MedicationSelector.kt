package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter
import com.hartwig.actin.clinical.datamodel.Medication
import java.time.LocalDate

internal class MedicationSelector(private val interpreter: MedicationStatusInterpreter) {
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
            (
                    lowercaseCategoriesToFind.contains(medication.atc()!!.anatomicalMainGroup().name().lowercase()) ||
                            lowercaseCategoriesToFind.contains(medication.atc()!!.chemicalSubGroup().name().lowercase()) ||
                            lowercaseCategoriesToFind.contains(medication.atc()!!.pharmacologicalSubGroup().name().lowercase()) ||
                            lowercaseCategoriesToFind.contains(medication.atc()!!.therapeuticSubGroup().name().lowercase()))
        }
    }

    fun activeOrRecentlyStoppedWithCategory(
        medications: List<Medication>, categoriesToFind: Set<String>, minStopDate: LocalDate
    ): List<Medication> {
        return medications.filter { medication ->
            (stringCaseInsensitivelyMatchesQueryCollection(
                medication.atc()!!.therapeuticSubGroup().name().lowercase(),
                categoriesToFind
            ) || stringCaseInsensitivelyMatchesQueryCollection(
                medication.atc()!!.chemicalSubGroup().name().lowercase(),
                categoriesToFind
            ) || stringCaseInsensitivelyMatchesQueryCollection(
                medication.atc()!!.anatomicalMainGroup().name().lowercase(),
                categoriesToFind
            ) || stringCaseInsensitivelyMatchesQueryCollection(
                medication.atc()!!.pharmacologicalSubGroup().name().lowercase(),
                categoriesToFind
            ))
        }
            .filter { isActive(it) || isRecentlyStopped(it, minStopDate) }
    }

    fun hasATCLevelName(medication: Medication, categoriesToFind: Set<String>): Boolean {
        return stringCaseInsensitivelyMatchesQueryCollection(
            medication.atc()!!.therapeuticSubGroup().name().lowercase(),
            categoriesToFind
        ) || stringCaseInsensitivelyMatchesQueryCollection(
            medication.atc()!!.chemicalSubGroup().name().lowercase(),
            categoriesToFind
        ) || stringCaseInsensitivelyMatchesQueryCollection(
            medication.atc()!!.anatomicalMainGroup().name().lowercase(),
            categoriesToFind
        ) || stringCaseInsensitivelyMatchesQueryCollection(
            medication.atc()!!.pharmacologicalSubGroup().name().lowercase(),
            categoriesToFind
        )
    }

    private fun isRecentlyStopped(medication: Medication, minStopDate: LocalDate): Boolean {
        return medication.stopDate()?.isAfter(minStopDate) ?: false
    }

    private fun isActive(medication: Medication): Boolean {
        return interpreter.interpret(medication) == MedicationStatusInterpretation.ACTIVE
    }
}