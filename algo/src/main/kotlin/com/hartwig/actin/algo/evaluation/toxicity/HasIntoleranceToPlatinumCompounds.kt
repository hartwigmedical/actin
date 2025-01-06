package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Intolerance

class HasIntoleranceToPlatinumCompounds : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val platinumAllergies = record.intolerances
            .filter { stringCaseInsensitivelyMatchesQueryCollection(it.name, PLATINUM_COMPOUNDS) }
            .map(Intolerance::name)
            .toSet()

        return if (platinumAllergies.isNotEmpty()) {
            EvaluationFactory.pass("Platinum compounds allergy: " + Format.concat(platinumAllergies))
        } else
            EvaluationFactory.fail("No known platinum compounds allergy")
    }

    companion object {
        val PLATINUM_COMPOUNDS =
            setOf("oxaliplatin", "eloxatin", "carboplatin", "paraplatin", "cisplatin", "platinol", "imifolatin", "nedaplatin", "NC-6004")
    }
}