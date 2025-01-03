package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Intolerance

class HasIntoleranceToTaxanes : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val taxaneAllergies = record.intolerances.map(Intolerance::name)
            .filter { stringCaseInsensitivelyMatchesQueryCollection(it, TAXANES) }
            .toSet()

        return if (taxaneAllergies.isNotEmpty()) {
            EvaluationFactory.pass("Has taxane allergy: " + concat(taxaneAllergies))
        } else
            EvaluationFactory.fail("No known taxane allergy")
    }

    companion object {
        val TAXANES = setOf("paclitaxel", "docetaxel", "cabazitaxel", "nab-paclitaxel", "Abraxane", "Jevtana", "Tesetaxel")
    }
}