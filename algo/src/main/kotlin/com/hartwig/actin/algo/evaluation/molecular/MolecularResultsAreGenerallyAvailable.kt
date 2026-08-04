package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class MolecularResultsAreGenerallyAvailable(private val labels: EvaluationLabels.Molecular) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when {
            record.molecularTests.isNotEmpty() -> EvaluationFactory.recoverablePass(labels.molecularResultsAreGenerallyAvailablePass())
            else -> EvaluationFactory.recoverableFail(labels.molecularResultsAreGenerallyAvailableRecoverableFail())
        }
    }
}