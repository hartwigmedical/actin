package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.molecular.datamodel.MolecularRecord

interface MolecularEvaluationFunction : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return if (!record.molecularHistory.hasMolecularData()) {
            EvaluationFactory.undetermined("No molecular data", "No molecular data")
        } else {
            record.molecularHistory.latestOrangeMolecularRecord()?.let(::evaluate)
                ?: EvaluationFactory.undetermined("Insufficient molecular data", "Insufficient molecular data")
        }
    }

    fun evaluate(molecular: MolecularRecord): Evaluation
}