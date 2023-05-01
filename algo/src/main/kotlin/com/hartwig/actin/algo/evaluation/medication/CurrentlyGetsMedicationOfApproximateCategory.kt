package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.util.ApplicationConfig

//TODO: Update according to README
class CurrentlyGetsMedicationOfApproximateCategory internal constructor(
    private val selector: MedicationSelector,
    private val categoryTermToFind: String
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val lowercaseTermToFind = categoryTermToFind.lowercase(ApplicationConfig.LOCALE)
        val medications = selector.active(record.clinical().medications())
            .filter { medication -> medication.categories().any { it.lowercase(ApplicationConfig.LOCALE).contains(lowercaseTermToFind) } }
            .map { it.name() }

        return if (medications.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient currently gets medication " + concat(medications) + ", which belong(s) to category "
                        + categoryTermToFind, "$categoryTermToFind medication use"
            )
        } else
            EvaluationFactory.fail(
                "Patient currently does not get medication of category $categoryTermToFind",
                "No $categoryTermToFind medication use"
            )
    }
}