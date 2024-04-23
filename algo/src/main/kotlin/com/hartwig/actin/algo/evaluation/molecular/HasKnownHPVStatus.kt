package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.ExperimentType

class HasKnownHPVStatus : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (indeterminatePriorTestsForHPV, conclusivePriorTestsForHPV) = record.molecularHistory.allIHCTests()
            .filter { (it.item?.contains("HPV") ?: false) }
            .partition(PriorMolecularTest::impliesPotentialIndeterminateStatus)

        val molecularRecords = record.molecularHistory.allOrangeMolecularRecords()

        return when {
            molecularRecords.any { it.type == ExperimentType.WHOLE_GENOME && it.containsTumorCells } -> {
                return EvaluationFactory.pass(
                    "WGS has successfully been performed so molecular results are available for HPV",
                    "WGS results available for HPV"
                )
            }

            conclusivePriorTestsForHPV.isNotEmpty() -> {
                EvaluationFactory.pass("HPV has been tested in a prior molecular test",
                    "HPV result available")
            }

            molecularRecords.any { it.type == ExperimentType.WHOLE_GENOME } -> {
                EvaluationFactory.undetermined(
                    "Undetermined HPV status due to low purity in WGS",
                    "Undetermined HPV status due to low purity in WGS"
                )
            }

            indeterminatePriorTestsForHPV.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "HPV has been tested in a prior molecular test but with indeterminate status",
                    "HPV tested before but indeterminate status"
                )
            }

            record.molecularHistory.allOrangeMolecularRecords().isEmpty() -> {
                EvaluationFactory.undetermined("HPV status not available (no molecular data)", "Undetermined HPV status (no molecular data)")
            }

            else -> {
                EvaluationFactory.fail("HPV has not been tested", "HPV not tested")
            }
        }
    }
}