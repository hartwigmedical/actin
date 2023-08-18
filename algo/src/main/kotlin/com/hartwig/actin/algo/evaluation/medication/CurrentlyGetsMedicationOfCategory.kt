package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat

class CurrentlyGetsMedicationOfCategory(
    private val selector: MedicationSelector,
    private val categories: Map<String, Set<String>?>
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val categoriesToFind = categories.values.first()
        val categoryName = categories.keys.first()
        val medications = selector.activeWithAnyExactCategory(record.clinical().medications(), categoriesToFind!!)

        val foundMedicationNames = medications.map { it.name() }.filter { it.isNotEmpty() }.distinct()

        return if (medications.isNotEmpty()) {
            val foundMedicationString = if (foundMedicationNames.isNotEmpty()) ": ${concat(foundMedicationNames)}" else ""
            EvaluationFactory.pass(
                "Patient currently gets medication $foundMedicationString, which belong(s) to category $categoryName",
                "$categoryName medication use"
            )
        } else
            EvaluationFactory.fail(
                "Patient currently does not get medication of category $categoryName",
                "No $categoryName medication use"
            )
    }
}