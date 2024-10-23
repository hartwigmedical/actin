package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import java.time.LocalDate

class GeneIsNotExpressed(maxTestAge: LocalDate? = null) : MolecularEvaluationFunction(maxTestAge){
    
    override fun evaluate(molecular: MolecularRecord): Evaluation {
        return EvaluationFactory.fail("RNA gene expression not yet evaluated")
    }
}