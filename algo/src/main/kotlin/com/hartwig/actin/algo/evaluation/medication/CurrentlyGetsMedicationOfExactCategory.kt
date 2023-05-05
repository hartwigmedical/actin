package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat

class CurrentlyGetsMedicationOfExactCategory internal constructor(
    private val selector: MedicationSelector,
    private val categoriesToFind: Set<String>
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = selector.activeWithAnyExactCategory(record.clinical().medications(), categoriesToFind)
        if (medications.isNotEmpty()) {
            val names = medications.map { it.name() }
            return EvaluationFactory.pass(
                "Patient currently gets medication " + concat(names) + ", which belong(s) to category "
                        + concat(categoriesToFind), concat(categoriesToFind) + " medication use"
            )
        }
        return EvaluationFactory.fail(
            "Patient currently does not get medication of category " + concat(categoriesToFind),
            "No " + concat(categoriesToFind) + " medication use"
        )
    }
}