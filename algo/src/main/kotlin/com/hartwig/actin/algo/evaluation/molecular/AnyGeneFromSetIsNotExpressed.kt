package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concatWithCommaAndAnd
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import java.time.LocalDate

//TODO (CB): only 1 message
class AnyGeneFromSetIsNotExpressed(maxTestAge: LocalDate? = null, private val genes: Set<String>) : MolecularEvaluationFunction(maxTestAge){

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Non-expression of ${concatWithCommaAndAnd(genes)} in RNA undetermined",
            missingGenesForEvaluation = true
        )
    }
}