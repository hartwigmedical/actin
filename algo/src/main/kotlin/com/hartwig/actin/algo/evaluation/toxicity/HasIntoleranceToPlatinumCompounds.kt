package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format

class HasIntoleranceToPlatinumCompounds : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val allergies = record.clinical().intolerances()
            .filter { PLATINUM_COMPOUNDS.contains(it.name().lowercase()) }
            .map { it.name() }
            .toSet()

        return if (allergies.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient has allergy to a platinum compounds: " + Format.concat(allergies),
                "Platinum compounds allergy: " + Format.concat(allergies)
            )
        } else
            EvaluationFactory.fail(
                "Patient has no known allergy to platinum compounds",
                "No known platinum compounds allergy"
            )
    }

    companion object {
        val PLATINUM_COMPOUNDS =
            setOf("oxaliplatin", "eloxatin", "carboplatin", "paraplatin", "cisplatin", "platinol", "imifolatin", "nedaplatin", "NC-6004")
    }
}