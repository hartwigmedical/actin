package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator.geneIsInactivatedForPatient
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasCancerWithNeuroendocrineComponent(
    private val doidModel: DoidModel, private val molecularLabels: EvaluationLabels.Molecular, private val tumorLabels: EvaluationLabels.Tumor
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(tumorLabels.hasCancerWithNeuroendocrineComponentUndeterminedTumorTypeMissing())
        }
        val (hasNeuroendocrineProfile, inactivatedGenes) = hasNeuroendocrineMolecularProfile(record)

        return when {
            TumorEvaluationFunctions.hasTumorWithNeuroendocrineComponent(doidModel, tumorDoids, record.tumor.name) -> {
                EvaluationFactory.pass(tumorLabels.hasCancerWithNeuroendocrineComponentPass())
            }

            TumorEvaluationFunctions.hasTumorWithSmallCellComponent(doidModel, tumorDoids, record.tumor.name) -> {
                EvaluationFactory.undetermined(tumorLabels.hasCancerWithNeuroendocrineComponentUndeterminedSmallCellComponent())
            }

            hasNeuroendocrineProfile -> {
                EvaluationFactory.undetermined(
                    tumorLabels.hasCancerWithNeuroendocrineComponentUndeterminedMolecularProfile(inactivatedGenes)
                )
            }

            else -> EvaluationFactory.fail(tumorLabels.hasCancerWithNeuroendocrineComponentFail())
        }
    }

    private fun hasNeuroendocrineMolecularProfile(record: PatientRecord): Pair<Boolean, List<String>> {
        val genes = listOf("TP53", "PTEN", "RB1")
        val inactivatedGenes = genes.filter { geneIsInactivatedForPatient(it, record, molecularLabels) }
        return Pair(inactivatedGenes.size >= 2, inactivatedGenes)
    }
}