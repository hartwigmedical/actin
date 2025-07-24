package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.driver.VirusType

class HasKnownHPVStatus : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (indeterminateIhcTestsForHpv, determinateIhcTestsForHpv) = record.ihcTests
            .filter { (it.item.contains("HPV") || it.item.contains("Human papillomavirus")) }
            .partition(IhcTest::impliesPotentialIndeterminateStatus)
        val molecularRecords = record.molecularHistory.allOrangeMolecularRecords()
        val molecularTests = record.molecularHistory.molecularTests

        return when {
            molecularRecords.any { it.experimentType == ExperimentType.HARTWIG_WHOLE_GENOME && it.containsTumorCells } -> {
                EvaluationFactory.pass("HPV status available by WGS")
            }

            molecularTests.any { it.drivers.viruses.any { it.type == VirusType.HPV } } -> {
                EvaluationFactory.pass("HPV status known")
            }

            determinateIhcTestsForHpv.isNotEmpty() -> EvaluationFactory.pass("HPV status available by HPV test")

            indeterminateIhcTestsForHpv.isNotEmpty() -> EvaluationFactory.warn("HPV tested before but indeterminate status")

            molecularRecords.any { it.experimentType == ExperimentType.HARTWIG_WHOLE_GENOME } -> {
                EvaluationFactory.undetermined(
                    "HPV status undetermined (WGS contained no tumor cells)",
                    isMissingMolecularResultForEvaluation = true
                )
            }

            else -> EvaluationFactory.recoverableFail("HPV status not known", isMissingMolecularResultForEvaluation = true)
        }
    }
}