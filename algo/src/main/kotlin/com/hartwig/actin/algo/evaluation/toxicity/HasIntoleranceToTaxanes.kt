package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat

class HasIntoleranceToTaxanes internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val allergies = record.clinical().intolerances()
            .filter { TAXANES.contains(it.name().lowercase()) }
            .map { it.name() }
            .toSet()

        return if (allergies.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient has allergy to a taxane: " + concat(allergies),
                "Taxane allergy: " + concat(allergies)
            )
        } else
            EvaluationFactory.fail(
                "Patient has no known allergy to taxanes",
                "No known taxane allergy"
            )
    }

    companion object {
        val TAXANES = setOf("paclitaxel", "docetaxel", "cabazitaxel")
    }
}