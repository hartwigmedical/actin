package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.molecular.MolecularHistory

class NsclcDriverGeneStatusesAreAvailable(private val labels: EvaluationLabels.Molecular) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val molecularHistory = MolecularHistory(record.molecularTests)
        val (validOncoPanelOrWGSList, invalidOncoPanelOrWGSList) = molecularHistory.allOrangeMolecularRecords()
            .partition { it.hasSufficientQuality }

        val missing = molecularHistory.allPanelsAndIhc().let { panels ->
            NSCLC_DRIVER_GENE_SET.filterNot { gene -> panels.any { it.testsGene(gene, any("")) } }
        }

        return when {
            validOncoPanelOrWGSList.isNotEmpty() || missing.isEmpty() -> {
                EvaluationFactory.pass(labels.nsclcDriverGeneStatusesAreAvailablePass())
            }

            invalidOncoPanelOrWGSList.isNotEmpty() -> {
                EvaluationFactory.recoverableFail(
                    labels.nsclcDriverGeneStatusesAreAvailableFailInsufficientQuality(),
                    isMissingMolecularResultForEvaluation = true
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    labels.nsclcDriverGeneStatusesAreAvailableFailMissing(missing.joinToString()),
                    isMissingMolecularResultForEvaluation = true
                )
            }
        }
    }

    companion object {
        internal val NSCLC_DRIVER_GENE_SET = setOf("EGFR", "MET", "BRAF", "ALK", "ROS1", "RET", "NTRK1", "NTRK2", "NTRK3")
    }
}