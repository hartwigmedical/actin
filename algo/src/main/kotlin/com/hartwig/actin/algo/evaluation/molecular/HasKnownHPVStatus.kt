package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.molecular.ExperimentType

class HasKnownHPVStatus : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (indeterminatePriorTestsForHPV, conclusivePriorTestsForHPV) = record.ihcTests
            .filter { (it.item?.contains("HPV") ?: false) }
            .partition(IhcTest::impliesPotentialIndeterminateStatus)

        val molecularRecords = record.molecularHistory.allOrangeMolecularRecords()

        return when {
            molecularRecords.any { it.experimentType == ExperimentType.HARTWIG_WHOLE_GENOME && it.containsTumorCells } -> {
                return EvaluationFactory.pass("HPV status available by WGS")
            }

            conclusivePriorTestsForHPV.isNotEmpty() -> {
                EvaluationFactory.pass("HPV status available by HPV test")
            }

            molecularRecords.any { it.experimentType == ExperimentType.HARTWIG_WHOLE_GENOME } -> {
                EvaluationFactory.undetermined("HPV status undetermined (low purity in WGS)")
            }

            indeterminatePriorTestsForHPV.isNotEmpty() -> {
                EvaluationFactory.undetermined("HPV tested before but indeterminate status")
            }

            record.molecularHistory.allOrangeMolecularRecords().isEmpty() -> {
                EvaluationFactory.undetermined("No HPV status result (no molecular data)", isMissingMolecularResultForEvaluation = true)
            }

            else -> {
                EvaluationFactory.recoverableFail("No HPV status result", isMissingMolecularResultForEvaluation = true)
            }
        }
    }
}