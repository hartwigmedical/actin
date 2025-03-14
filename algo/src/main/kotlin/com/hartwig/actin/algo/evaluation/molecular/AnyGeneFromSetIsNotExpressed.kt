package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import java.time.LocalDate

class AnyGeneFromSetIsNotExpressed(maxTestAge: LocalDate? = null, private val genes: Set<String>) : MolecularEvaluationFunction(maxTestAge){

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        return EvaluationFactory.undetermined(
            "Non-expression of ${concat(genes)} in RNA undetermined",
            isMissingMolecularResultForEvaluation = true
        )
    }

    override fun noMolecularRecordEvaluation(): Evaluation {
        return EvaluationFactory.undetermined(
            "No molecular data to determine non-expression of ${concat(genes)} in RNA",
            isMissingMolecularResultForEvaluation = true
        )
    }
}