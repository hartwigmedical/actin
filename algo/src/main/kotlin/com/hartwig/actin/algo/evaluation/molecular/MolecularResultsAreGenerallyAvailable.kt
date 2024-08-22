package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class MolecularResultsAreGenerallyAvailable : EvaluationFunction {
    
    override fun evaluate(record: PatientRecord): Evaluation {
        return when {
            record.molecularHistory.hasMolecularData() -> EvaluationFactory.pass("There are molecular results available")
            else -> EvaluationFactory.fail("No molecular results available")
        }
    }
}