package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection

//TODO: Update according to README
class CurrentlyGetsMedicationOfApproximateCategory internal constructor(
    private val selector: MedicationSelector,
    private val categoriesToFind: Set<String>
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val lowercaseCategoriesToFind = categoriesToFind.map { it.lowercase() }.toSet()
        val medications = selector.active(record.clinical().medications())
            .filter { medication ->
                (stringCaseInsensitivelyMatchesQueryCollection(
                    medication.atc()!!.therapeuticSubGroup().name().lowercase(),
                    lowercaseCategoriesToFind
                ) || stringCaseInsensitivelyMatchesQueryCollection(
                    medication.atc()!!.chemicalSubGroup().name().lowercase(),
                    lowercaseCategoriesToFind
                ) || stringCaseInsensitivelyMatchesQueryCollection(
                    medication.atc()!!.anatomicalMainGroup().name().lowercase(),
                    lowercaseCategoriesToFind
                ) || stringCaseInsensitivelyMatchesQueryCollection(
                    medication.atc()!!.pharmacologicalSubGroup().name().lowercase(),
                    lowercaseCategoriesToFind
                ))
            }

        val foundCategories = medications.map { it.atc()!!.therapeuticSubGroup().name() }.distinct()
        val foundMedicationNames = medications.map { it.name() }.filter { it.isNotEmpty() }.distinct()

        return if (medications.isNotEmpty()) {
            val foundMedicationString = if (foundMedicationNames.isNotEmpty()) ": ${concat(foundMedicationNames)}" else ""
            EvaluationFactory.pass(
                "Patient currently gets medication " + foundMedicationString + ", which belong(s) to category "
                        + concat(foundCategories),
                concat(foundCategories) + "medication use"
            )
        } else
            EvaluationFactory.fail(
                "Patient currently does not get medication of category ${concat(foundCategories)}",
                "No ${concat(foundCategories)} medication use"
            )
    }
}