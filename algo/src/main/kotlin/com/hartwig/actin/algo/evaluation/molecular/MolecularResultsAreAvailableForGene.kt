package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.molecular.datamodel.ExperimentType

class MolecularResultsAreAvailableForGene internal constructor(private val gene: String) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        if (record.molecular().type() == ExperimentType.WHOLE_GENOME && record.molecular().containsTumorCells()) {
            return unrecoverable()
                .result(EvaluationResult.PASS)
                .addPassSpecificMessages("WGS has successfully been performed so molecular results are available for gene $gene")
                .addPassGeneralMessages("WGS results available for $gene")
                .build()
        }
        var hasPassPriorTestForGene = false
        var hasIndeterminatePriorTestForGene = false
        for (priorMolecularTest in record.clinical().priorMolecularTests()) {
            if (priorMolecularTest.item() == gene) {
                if (priorMolecularTest.impliesPotentialIndeterminateStatus()) {
                    hasIndeterminatePriorTestForGene = true
                } else {
                    hasPassPriorTestForGene = true
                }
            }
        }
        if (hasPassPriorTestForGene) {
            return unrecoverable()
                .result(EvaluationResult.PASS)
                .addPassSpecificMessages("$gene has been tested in a prior molecular test")
                .addPassGeneralMessages("$gene tested before")
                .build()
        } else if (record.molecular().type() == ExperimentType.WHOLE_GENOME && !record.molecular().containsTumorCells()) {
            return unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Patient has had WGS but biopsy contained no tumor cells")
                .addUndeterminedGeneralMessages("$gene tested but sample purity is too low")
                .build()
        } else if (hasIndeterminatePriorTestForGene) {
            return unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("$gene has been tested in a prior molecular test but with indeterminate status")
                .addUndeterminedGeneralMessages("$gene tested before but indeterminate status")
                .build()
        }
        return unrecoverable()
            .result(EvaluationResult.FAIL)
            .addFailSpecificMessages("$gene has not been tested")
            .addFailGeneralMessages("$gene not tested")
            .build()
    }
}