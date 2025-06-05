package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class NsclcDriverGeneStatusesAreAvailable : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val molecularHistory = record.molecularHistory
        val (validOncoPanelOrWGSList, invalidOncoPanelOrWGSList) = molecularHistory.allOrangeMolecularRecords()
            .partition { it.containsTumorCells }

        val missing = molecularHistory.allPanels().let { panels ->
            NSCLC_DRIVER_GENE_SET.filterNot { gene -> panels.any { it.testsGene(gene, any("")) } }
        }

        return when {
            validOncoPanelOrWGSList.isNotEmpty() || missing.isEmpty() -> {
                EvaluationFactory.pass("NSCLC driver gene statuses are available")
            }

            invalidOncoPanelOrWGSList.isNotEmpty() -> {
                EvaluationFactory.recoverableFail(
                    "NSCLC driver gene statuses unknown (sequencing data of insufficient quality)",
                    isMissingMolecularResultForEvaluation = true
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "NSCLC driver gene statuses not available (missing: ${missing.joinToString()})",
                    isMissingMolecularResultForEvaluation = true
                )
            }
        }
    }

    companion object {
        internal val NSCLC_DRIVER_GENE_SET = setOf("EGFR", "MET", "BRAF", "ALK", "ROS1", "RET", "NTRK1", "NTRK2", "NTRK3")
    }
}