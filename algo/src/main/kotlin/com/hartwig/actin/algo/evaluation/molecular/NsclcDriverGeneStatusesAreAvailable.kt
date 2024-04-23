package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.molecular.datamodel.panel.Panel

class NsclcDriverGeneStatusesAreAvailable : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val molecularHistory = record.molecularHistory
        val (validOncoPanelOrWGSList, invalidOncoPanelOrWGSList) = molecularHistory.allMolecularRecords()
            .partition { it.containsTumorCells }
        val panelGenes = (molecularHistory.allArcherPanels() + molecularHistory.allGenericPanels()).flatMap(Panel::testedGenes).toSet()

        return when {
            validOncoPanelOrWGSList.isNotEmpty() || panelGenes.containsAll(NSCLC_DRIVER_GENE_SET) -> {
                EvaluationFactory.pass("NSCLC driver gene statuses are available")
            }

            invalidOncoPanelOrWGSList.isNotEmpty() -> {
                EvaluationFactory.recoverableFail("NSCLC driver gene statuses unknown (sequencing data of insufficient quality)")
            }

            else -> {
                val missingGenes = NSCLC_DRIVER_GENE_SET.filterNot { it in panelGenes }.joinToString(", ")
                EvaluationFactory.recoverableFail(
                    "NSCLC driver gene statuses not available (missing: $missingGenes)"
                )
            }
        }
    }

    companion object {
        internal val NSCLC_DRIVER_GENE_SET = setOf("EGFR", "MET", "BRAF", "ALK", "ROS1", "RET", "NTRK1", "NTRK2", "NTRK3")
    }
}