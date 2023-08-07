package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat

//TODO: Update according to README
class CurrentlyGetsMedicationOfApproximateCategory internal constructor(
    private val selector: MedicationSelector,
    private val categoryTermToFind: String
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val lowercaseTermToFind = categoryTermToFind.lowercase()
        val medications = selector.active(record.clinical().medications())
            .filter { medication ->
                (medication.atc()!!.anatomicalMainGroup().name().lowercase().contains(lowercaseTermToFind) || medication.atc()!!
                    .chemicalSubGroup().name().lowercase().contains(lowercaseTermToFind) || medication.atc()!!.therapeuticSubGroup().name()
                    .lowercase().contains(lowercaseTermToFind) || medication.atc()!!.pharmacologicalSubGroup().name().lowercase()
                    .contains(lowercaseTermToFind))
            }
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