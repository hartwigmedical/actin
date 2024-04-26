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

        val orangeMolecular = record.molecularHistory.latestOrangeMolecularRecord()
        if (orangeMolecular != null && orangeMolecular.type == ExperimentType.WHOLE_GENOME && orangeMolecular.containsTumorCells) {
            return EvaluationFactory.pass(
                "WGS has successfully been performed so molecular results are available for gene $gene",
                "WGS results available for $gene"
            )
        }

        if (orangeMolecular != null && orangeMolecular.type == ExperimentType.TARGETED && orangeMolecular.containsTumorCells) {
            val geneIsTested = orangeMolecular.drivers.copyNumbers
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

        if (isGeneTestedInPanel(ExperimentType.ARCHER, record.molecularHistory)) {
            return EvaluationFactory.pass(
                "Archer panel has been performed and molecular results are available for gene $gene",
                "Archer panel results available for $gene"
            )
        }

        if (isGeneTestedInPanel(ExperimentType.GENERIC_PANEL, record.molecularHistory)) {
            return EvaluationFactory.pass(
                "Panel has been performed and molecular results are available for gene $gene",
                "Panel results available for $gene"
            )
        }

        val (indeterminatePriorIHCTestsForGene, conclusivePriorIHCTestsForGene) = record.molecularHistory.allIHCTests()
            .filter { it.item == gene }
            .partition(PriorMolecularTest::impliesPotentialIndeterminateStatus)

        return when {
            conclusivePriorIHCTestsForGene.isNotEmpty() -> {
                EvaluationFactory.pass("$gene has been tested in a prior IHC test",
                    "$gene tested before")
            }

            orangeMolecular != null && orangeMolecular.type == ExperimentType.WHOLE_GENOME && !orangeMolecular.containsTumorCells -> {
                EvaluationFactory.undetermined(
                    "Patient has had WGS but biopsy contained no tumor cells",
                    "WGS performed containing $gene, but sample purity was too low"
                )
            }

            orangeMolecular != null && orangeMolecular.type == ExperimentType.TARGETED && !orangeMolecular.containsTumorCells -> {
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

    private fun isGeneTestedInPanel(type: ExperimentType, molecularHistory: MolecularHistory): Boolean {
        return when (type) {
            ExperimentType.ARCHER -> molecularHistory.allArcherPanels().any { gene in it.testedGenes() }
            ExperimentType.GENERIC_PANEL -> molecularHistory.allGenericPanels().any { gene in it.testedGenes() }
            else -> throw IllegalStateException("Unexpected experiment type $type")
        }
    }
}