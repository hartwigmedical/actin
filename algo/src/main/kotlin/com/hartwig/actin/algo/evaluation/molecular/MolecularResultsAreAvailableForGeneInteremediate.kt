package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularHistory


class MolecularResultsAreAvailableForGeneInteremediate(private val gene: String) {

    fun evaluate(record: PatientRecord): IntermediateEvaluation {

        if (!record.molecularHistory.hasMolecularData()) {
            return IntermediateEvaluation(EvaluationResult.UNDETERMINED, "No molecular data", "No molecular data")
        }

        val orangeMolecular = record.molecularHistory.latestOrangeMolecularRecord()
        if (orangeMolecular != null && orangeMolecular.type == ExperimentType.WHOLE_GENOME && orangeMolecular.containsTumorCells) {
            return IntermediateEvaluation(
                EvaluationResult.PASS,
                "WGS has successfully been performed so molecular results are available for:",
                "WGS results available for:",
                gene
            )
        }

        if (orangeMolecular != null && orangeMolecular.type == ExperimentType.TARGETED && orangeMolecular.containsTumorCells) {
            val geneIsTested = orangeMolecular.drivers.copyNumbers
                .any { it.gene == gene }
            return if (geneIsTested) {
                IntermediateEvaluation(
                    EvaluationResult.PASS,
                    "OncoAct tumor NGS panel has been successfully performed so molecular results are available for:",
                    "OncoAct tumor NGS panel results available for:",
                    gene
                )
            } else {
                IntermediateEvaluation(
                    EvaluationResult.WARN,
                    "OncoAct tumor NGS panel has been successfully performed but cannot verify inclusion for:",
                    "Unsure if results are available within performed OncoAct tumor NGS panel for:",
                    gene
                )
            }
        }

        if (isGeneTestedInPanel(ExperimentType.ARCHER, record.molecularHistory)) {
            return IntermediateEvaluation(
                EvaluationResult.PASS,
                "Archer panel has been performed and molecular results are available for:",
                "Archer panel results available for:",
                gene
            )
        }

        if (isGeneTestedInPanel(ExperimentType.GENERIC_PANEL, record.molecularHistory)) {
            return IntermediateEvaluation(
                EvaluationResult.PASS,
                "Panel has been performed and molecular results are available for:",
                "Panel results available for:",
                gene
            )
        }

        val (indeterminatePriorIHCTestsForGene, conclusivePriorIHCTestsForGene) = record.molecularHistory.allIHCTests()
            .filter { it.item == gene }
            .partition(PriorMolecularTest::impliesPotentialIndeterminateStatus)

        return when {
            conclusivePriorIHCTestsForGene.isNotEmpty() -> {
                IntermediateEvaluation(
                    EvaluationResult.PASS,
                    "Gene has been tested in a prior IHC test:",
                    "Gene has been tested:",
                    gene
                )
            }

            orangeMolecular != null && orangeMolecular.type == ExperimentType.WHOLE_GENOME && !orangeMolecular.containsTumorCells -> {
                IntermediateEvaluation(
                    EvaluationResult.UNDETERMINED,
                    "Patient has had WGS but biopsy contained no tumor cells:",
                    "WGS performed but sample purity was too low containing:",
                    gene
                )
            }

            orangeMolecular != null && orangeMolecular.type == ExperimentType.TARGETED && !orangeMolecular.containsTumorCells -> {
                IntermediateEvaluation(
                    EvaluationResult.UNDETERMINED,
                    "Patient has had OncoAct tumor NGS panel but biopsy contained too little tumor cells:",
                    "OncoAct tumor NGS panel performed but sample purity was too low containing:",
                    gene
                )
            }

            indeterminatePriorIHCTestsForGene.isNotEmpty() -> {
                IntermediateEvaluation(
                    EvaluationResult.UNDETERMINED,
                    "Gene has been tested in a prior IHC test but with indeterminate status:",
                    "Gene has been tested before but indeterminate status:",
                    gene
                )
            }

            else -> {
                IntermediateEvaluation(
                    EvaluationResult.FAIL,
                    "Gene has not been tested:",
                    "Gene not tested:",
                    gene
                )
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