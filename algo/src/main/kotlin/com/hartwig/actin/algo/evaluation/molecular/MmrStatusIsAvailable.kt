package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class MmrStatusIsAvailable(private val labels: EvaluationLabels.Molecular) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcResultAvailable = IhcTestEvaluation.create("MMR", record.ihcTests).filteredTests.isNotEmpty()
        val molecularResultAvailable = record.molecularTests.any { it.characteristics.microsatelliteStability != null }

        return if (ihcResultAvailable || molecularResultAvailable) {
            EvaluationFactory.pass(labels.mmrStatusIsAvailablePass())
        } else {
            EvaluationFactory.recoverableFail(labels.mmrStatusIsAvailableRecoverableFail())
        }
    }
}