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

        if (!record.molecularHistory.hasMolecularData()) {
            return EvaluationFactory.undetermined("No molecular data", "No molecular data")
        }

        val orangeMolecular = record.molecularHistory.latestOrangeMolecularRecord()
        if (orangeMolecular != null && orangeMolecular.experimentType == ExperimentType.HARTWIG_WHOLE_GENOME && orangeMolecular.containsTumorCells) {
            return EvaluationFactory.pass(
                "WGS has successfully been performed so molecular results are available for gene $gene",
                "WGS results available for $gene"
            )
        }

        if (orangeMolecular != null && orangeMolecular.experimentType == ExperimentType.HARTWIG_TARGETED && orangeMolecular.containsTumorCells) {
            val geneIsTested = orangeMolecular.drivers.copyNumbers
                .any { it.gene == gene }
            return if (geneIsTested) {
                EvaluationFactory.pass(
                    "OncoAct tumor NGS panel has been successfully performed so molecular results are available for gene $gene",
                    "OncoAct tumor NGS panel results available for $gene"
                )
            } else {
                EvaluationFactory.warn(
                    "OncoAct tumor NGS panel has been successfully performed but cannot verify that gene $gene was included",
                    "Unsure if gene $gene results are available within performed OncoAct tumor NGS panel"
                )
            }
        }

        if (isGeneTestedInPanel(record.molecularHistory)) {
            return EvaluationFactory.pass(
                "Panel has been performed and molecular results are available for gene $gene",
                "Panel results available for $gene"
            )
        }

        val (indeterminatePriorIHCTestsForGene, conclusivePriorIHCTestsForGene) = record.priorIHCTests
            .filter { it.item == gene }
            .partition(PriorIHCTest::impliesPotentialIndeterminateStatus)

        return when {
            conclusivePriorIHCTestsForGene.isNotEmpty() -> {
                EvaluationFactory.pass(
                    "$gene has been tested in a prior IHC test",
                    "$gene tested before"
                )
            }

            orangeMolecular != null && orangeMolecular.experimentType == ExperimentType.HARTWIG_WHOLE_GENOME -> {
                EvaluationFactory.undetermined(
                    "Patient has had WGS but biopsy contained no tumor cells",
                    "WGS performed containing $gene, but sample purity was too low"
                )
            }

            orangeMolecular != null && orangeMolecular.experimentType == ExperimentType.HARTWIG_TARGETED -> {
                EvaluationFactory.undetermined(
                    "Patient has had OncoAct tumor NGS panel but biopsy contained too little tumor cells",
                    "OncoAct tumor NGS panel performed containing $gene, but sample purity was too low"
                )
            }

            indeterminatePriorIHCTestsForGene.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "$gene has been tested in a prior IHC test but with indeterminate status",
                    "$gene tested before but indeterminate status"
                )
            }

            else -> {
                EvaluationFactory.fail("$gene has not been tested", "$gene not tested")
            }
        }
    }

    private fun isGeneTestedInPanel(molecularHistory: MolecularHistory): Boolean {
        return molecularHistory.allPanels().any { it.testsGene(gene) }
    }
}