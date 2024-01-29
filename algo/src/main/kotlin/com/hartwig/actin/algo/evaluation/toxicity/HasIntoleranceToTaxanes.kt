package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.clinical.datamodel.Intolerance

class HasIntoleranceToTaxanes : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val taxaneAllergies = record.clinical.intolerances.map(Intolerance::name)
            .filter { stringCaseInsensitivelyMatchesQueryCollection(it, TAXANES) }
            .toSet()

        return if (taxaneAllergies.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient has allergy to a taxane: " + concat(taxaneAllergies),
                "Taxane allergy: " + concat(taxaneAllergies)
            )
        } else
            EvaluationFactory.fail(
                "Patient has no known allergy to taxanes",
                "No known taxane allergy"
            )
    }

    companion object {
        val TAXANES = setOf("paclitaxel", "docetaxel", "cabazitaxel", "nab-paclitaxel", "Abraxane", "Jevtana", "Tesetaxel")
    }
}