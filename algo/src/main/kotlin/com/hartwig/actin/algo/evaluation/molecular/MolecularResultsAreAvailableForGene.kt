package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularHistory

class MolecularResultsAreAvailableForGene(private val gene: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        if (!record.molecularHistory.hasMolecularData()) {
            return EvaluationFactory.undetermined("No molecular data", "No molecular data")
        }

        val molecular = record.molecularHistory.latestMolecularRecord()
        if (molecular != null && molecular.type == ExperimentType.WHOLE_GENOME && molecular.containsTumorCells) {
            return EvaluationFactory.pass(
                "WGS has successfully been performed so molecular results are available for gene $gene",
                "WGS results available for $gene"
            )
        }

        if (molecular != null && molecular.type == ExperimentType.TARGETED && molecular.containsTumorCells) {
            val geneIsTested = molecular.drivers.copyNumbers
                .any { it.gene == gene }
            return if (geneIsTested) {
                EvaluationFactory.pass(
                    "OncoAct tumor NGS panel has been successfully performed so molecular results are available for gene $gene",
                    "OncoAct tumor NGS panel results available for $gene"
                )
            } else {
                EvaluationFactory.warn("OncoAct tumor NGS panel has been successfully performed but cannot verify that gene $gene was included",
                    "Unsure if gene $gene results are available within performed OncoAct tumor NGS panel")
            }
        }

        if (isGeneTestedInArcher(record.molecularHistory)) {
            return EvaluationFactory.pass(
                "Archer panel has been performed and molecular results are available for gene $gene",
                "Archer panel results available for $gene"
            )
        }

        if (isGeneTestedInGenericPanel(record.molecularHistory)) {
            return EvaluationFactory.pass(
                "Panel has been performed and molecular results are available for gene $gene",
                "Panel results available for $gene"
            )
        }

        val (indeterminatePriorIHCTestsForGene, passPriorIHCTestsForGene) = record.molecularHistory.allIHCTests()
            .filter { it.item == gene }
            .partition(PriorMolecularTest::impliesPotentialIndeterminateStatus)

        return when {
            passPriorIHCTestsForGene.isNotEmpty() -> {
                EvaluationFactory.pass("$gene has been tested in a prior IHC test",
                    "$gene tested before")
            }

            molecular != null && molecular.type == ExperimentType.WHOLE_GENOME && !molecular.containsTumorCells -> {
                EvaluationFactory.undetermined(
                    "Patient has had WGS but biopsy contained no tumor cells",
                    "WGS performed containing $gene, but sample purity was too low"
                )
            }

            molecular != null && molecular.type == ExperimentType.TARGETED && !molecular.containsTumorCells -> {
                EvaluationFactory.undetermined(
                    "Patient has had OncoAct tumor NGS panel but biopsy contained too little tumor cells",
                    "OncoAct tumor NGS panel performed containing $gene, but sample purity was too low"
                )
            }

            indeterminatePriorIHCTestsForGene.isNotEmpty() -> {
                EvaluationFactory.undetermined(
                    "$gene has been tested in a prior molecular test but with indeterminate status",
                    "$gene tested before but indeterminate status"
                )
            }

            else -> {
                EvaluationFactory.fail("$gene has not been tested", "$gene not tested")
            }
        }
    }

    private fun isGeneTestedInArcher(molecularHistory: MolecularHistory): Boolean {
        return molecularHistory.allArcherPanels().any { gene in it.testedGenes() }
    }

    private fun isGeneTestedInGenericPanel(molecularHistory: MolecularHistory): Boolean {
        return molecularHistory.allGenericPanels().any { gene in it.testedGenes() }
    }
}