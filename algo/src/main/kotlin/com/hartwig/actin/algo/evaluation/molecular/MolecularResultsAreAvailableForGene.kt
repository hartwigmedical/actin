package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularRecord

class MolecularResultsAreAvailableForGene(private val gene: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return (record.molecular?.let { evaluate(it, record.clinical) })
            ?: MolecularEventUtil.noMolecularEvaluation()
    }

    private fun evaluate(molecular: MolecularRecord, clinical: ClinicalRecord): Evaluation {
        if (molecular.type == ExperimentType.WHOLE_GENOME && molecular.containsTumorCells) {
            return EvaluationFactory.pass(
                "WGS has successfully been performed so molecular results are available for gene $gene", "WGS results available for $gene"
            )
        }
        val (indeterminatePriorTestsForGene, passPriorTestsForGene) = clinical.priorMolecularTests
            .filter { it.item == gene }
            .partition(PriorMolecularTest::impliesPotentialIndeterminateStatus)

        return when {
            passPriorTestsForGene.isNotEmpty() -> {
                EvaluationFactory.pass("$gene has been tested in a prior molecular test", "$gene tested before")
            }

            molecular.type == ExperimentType.WHOLE_GENOME && !molecular.containsTumorCells -> {
                EvaluationFactory.undetermined(
                    "Patient has had WGS but biopsy contained no tumor cells", "$gene tested but sample purity is too low"
                )
            }

            indeterminatePriorTestsForGene.isNotEmpty() -> {
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
}