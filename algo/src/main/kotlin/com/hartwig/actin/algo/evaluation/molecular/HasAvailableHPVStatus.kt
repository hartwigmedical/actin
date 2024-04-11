package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.ExperimentType

class HasAvailableHPVStatus : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        if (record.molecularHistory.latestMolecularRecord() == null) {
            return EvaluationFactory.undetermined("HPV status not available (no molecular data)", "Undetermined HPV status (no molecular data)")
        }

        val molecular = record.molecularHistory.latestMolecularRecord()!!

        val (indeterminatePriorTestsForHPV, passPriorTestsForHPV) = record.molecularHistory.allPriorMolecularTests()
            .filter { (it.item?.contains("HPV") ?: false) }
            .partition(PriorMolecularTest::impliesPotentialIndeterminateStatus)

        return when {
            molecular.type == ExperimentType.WHOLE_GENOME && molecular.containsTumorCells -> {
                return EvaluationFactory.pass(
                    "WGS has successfully been performed so molecular results are available for HPV",
                    "WGS results available for HPV"
                )
            }

            passPriorTestsForHPV.isNotEmpty() -> {
                EvaluationFactory.pass("HPV has been tested in a prior molecular test",
                    "HPV result available")
            }

            molecular.type == ExperimentType.WHOLE_GENOME -> {
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

            else -> {
                EvaluationFactory.fail("HPV has not been tested", "HPV not tested")
            }
        }
    }
}