package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.clinical.datamodel.Intolerance

class HasIntoleranceToPlatinumCompounds : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val platinumAllergies = record.clinical.intolerances
            .filter { stringCaseInsensitivelyMatchesQueryCollection(it.name, PLATINUM_COMPOUNDS) }
            .map(Intolerance::name)
            .toSet()

        return if (platinumAllergies.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient has allergy to a platinum compounds: " + Format.concat(platinumAllergies),
                "Platinum compounds allergy: " + Format.concat(platinumAllergies)
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