package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularHistory

class NsclcDriverGeneStatusesAreAvailable: EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val molecularHistory = record.molecularHistory
        val (validOncoPanelOrWGSList, nonValidOncoPanelOrWGSList) = molecularHistory.allMolecularRecords()
            .filter { it.type in listOf(ExperimentType.WHOLE_GENOME, ExperimentType.TARGETED) }
            .partition { it.containsTumorCells }
        val panelGenes = listOf(
            testedGenes(ExperimentType.GENERIC_PANEL, molecularHistory),
            testedGenes(ExperimentType.ARCHER, molecularHistory)
        ).flatten()

        return when {
            validOncoPanelOrWGSList.isNotEmpty() || panelGenes.containsAll(NSCLC_DRIVER_GENE_SET) -> {
                EvaluationFactory.pass("NSCLC driver gene statuses are available")
            }

            nonValidOncoPanelOrWGSList.isNotEmpty() -> {
                EvaluationFactory.undetermined("NSCLC driver gene statuses undetermined (sequencing data of insufficient quality)")
            }

            else -> {
                val missingGenes = NSCLC_DRIVER_GENE_SET.filterNot { it in panelGenes }.joinToString(", ")
                EvaluationFactory.fail(
                    "NSCLC driver gene statuses not available (missing: $missingGenes)"
                )
            }
        }
    }

    private fun testedGenes(type: ExperimentType, molecularHistory: MolecularHistory): List<String> {
        return when (type) {
            ExperimentType.ARCHER -> molecularHistory.allArcherPanels().flatMap { it.testedGenes() }
            ExperimentType.GENERIC_PANEL -> molecularHistory.allGenericPanels().flatMap { it.testedGenes() }
            else -> emptyList()
        }
    }

    companion object {
        internal val NSCLC_DRIVER_GENE_SET = setOf("EGFR", "MET", "BRAF", "ALK", "ROS1", "RET", "NTRK1", "NTRK2", "NTRK3")
    }
}