package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class MolecularResultsAreGenerallyAvailable : EvaluationFunction {
    
    override fun evaluate(record: PatientRecord): Evaluation {
        return when {
            record.molecularHistory.molecularTests.isEmpty() -> EvaluationFactory.pass("There are molecular results available")
            else -> EvaluationFactory.fail("No molecular results available")
        }
    }
}