package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import java.time.LocalDate

class GeneIsOverexpressed(maxTestAge: LocalDate? = null) : MolecularEvaluationFunction(maxTestAge, false) {

    override fun evaluate(molecular: MolecularRecord): Evaluation {
        return EvaluationFactory.undetermined("Overexpression of genes in RNA undetermined", missingGenesForEvaluation = true)
    }
}