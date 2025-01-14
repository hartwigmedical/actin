package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.PriorIHCTest
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularHistory

class MolecularResultsAreAvailableForGene(private val gene: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        if (record.molecularHistory.molecularTests.isEmpty()) {
            return EvaluationFactory.undetermined("No molecular data")
        }

        val orangeMolecular = record.molecularHistory.latestOrangeMolecularRecord()
        if (orangeMolecular != null && orangeMolecular.experimentType == ExperimentType.HARTWIG_WHOLE_GENOME && orangeMolecular.containsTumorCells) {
            return EvaluationFactory.pass("WGS results available for $gene")
        }

        if (orangeMolecular != null && orangeMolecular.experimentType == ExperimentType.HARTWIG_TARGETED && orangeMolecular.containsTumorCells) {
            val geneIsTested = orangeMolecular.drivers.copyNumbers
                .any { it.gene == gene }
            return if (geneIsTested) {
                EvaluationFactory.pass("OncoAct tumor NGS panel results available for $gene")
            } else {
                EvaluationFactory.warn("Unsure if gene $gene results are available within performed OncoAct tumor NGS panel")
            }
        }

        if (isGeneTestedInPanel(record.molecularHistory)) {
            return EvaluationFactory.pass("Panel results available for $gene")
        }

        val (indeterminatePriorIHCTestsForGene, conclusivePriorIHCTestsForGene) = record.priorIHCTests
            .filter { it.item == gene }
            .partition(PriorIHCTest::impliesPotentialIndeterminateStatus)

        return when {
            conclusivePriorIHCTestsForGene.isNotEmpty() -> {
                EvaluationFactory.pass("$gene tested before in IHC test")
            }

            orangeMolecular != null && orangeMolecular.experimentType == ExperimentType.HARTWIG_WHOLE_GENOME -> {
                EvaluationFactory.undetermined(
                    "WGS performed containing $gene but biopsy contained insufficient tumor cells for analysis"
                )
            }

            orangeMolecular != null && orangeMolecular.experimentType == ExperimentType.HARTWIG_TARGETED -> {
                EvaluationFactory.undetermined("OncoAct tumor NGS panel performed containing $gene but biopsy contained " +
                        "insufficient tumor cells for analysis")
            }

            indeterminatePriorIHCTestsForGene.isNotEmpty() -> {
                EvaluationFactory.undetermined("$gene tested before in IHC test but indeterminate status")
            }

            else -> {
                EvaluationFactory.fail("$gene not tested")
            }
        }
    }

    private fun isGeneTestedInPanel(molecularHistory: MolecularHistory): Boolean {
        return molecularHistory.allPanels().any { it.testsGene(gene) }
    }
}