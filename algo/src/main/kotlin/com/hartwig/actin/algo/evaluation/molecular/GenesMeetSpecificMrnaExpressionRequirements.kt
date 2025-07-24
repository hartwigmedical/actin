package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class GenesMeetSpecificMrnaExpressionRequirements(private val genes: Set<String>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Specific mRNA expression requirements for gene(s) ${Format.concat(genes)} undetermined",
            isMissingMolecularResultForEvaluation = true
        )
    }
}