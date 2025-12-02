package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator.geneIsInactivatedForPatient
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasCancerWithNeuroendocrineComponent(private val doidModel: DoidModel) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined("Neuroendocrine component undetermined (tumor type missing)")
        }
        val (hasNeuroendocrineProfile, inactivatedGenes) = hasNeuroendocrineMolecularProfile(record)

        return when {
            TumorEvaluationFunctions.hasTumorWithNeuroendocrineComponent(doidModel, tumorDoids, record.tumor.name) -> {
                EvaluationFactory.pass("Has cancer with neuroendocrine component")
            }

            TumorEvaluationFunctions.hasTumorWithSmallCellComponent(doidModel, tumorDoids, record.tumor.name) -> {
                EvaluationFactory.undetermined("Neuroendocrine component undetermined (small cell component present)")
            }

            hasNeuroendocrineProfile -> {
                EvaluationFactory.undetermined(
                    "Neuroendocrine molecular profile (inactivated genes: ${inactivatedGenes.joinToString(", ")}) -" +
                            " undetermined if may be considered cancer with neuroendocrine component"
                )
            }

            else -> EvaluationFactory.fail("Has no cancer with neuroendocrine component")
        }
    }

    private fun hasNeuroendocrineMolecularProfile(record: PatientRecord): Pair<Boolean, List<String>> {
        val genes = listOf("TP53", "PTEN", "RB1")
        val inactivatedGenes = genes.filter { geneIsInactivatedForPatient(it, record) }
        return Pair(inactivatedGenes.size >= 2, inactivatedGenes)
    }
}