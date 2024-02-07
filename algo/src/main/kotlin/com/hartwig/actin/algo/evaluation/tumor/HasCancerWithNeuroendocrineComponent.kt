package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator.geneIsInactivatedForPatient
import com.hartwig.actin.doid.DoidModel

class HasCancerWithNeuroendocrineComponent (private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.clinical.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids) && record.clinical.tumor.primaryTumorExtraDetails == null) {
            return EvaluationFactory.undetermined(
                "Could not determine whether tumor of patient may have a neuroendocrine component",
                "Undetermined neuroendocrine component"
            )
        }
        val hasNeuroendocrineDoid = DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, tumorDoids, NEUROENDOCRINE_DOIDS)
        val hasNeuroendocrineTerm = DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(doidModel, tumorDoids, NEUROENDOCRINE_TERMS)
        val hasNeuroendocrineDetails =
            TumorTypeEvaluationFunctions.hasTumorWithDetails(record.clinical.tumor, NEUROENDOCRINE_EXTRA_DETAILS)
        if (hasNeuroendocrineDoid || hasNeuroendocrineTerm || hasNeuroendocrineDetails) {
            return EvaluationFactory.pass("Patient has cancer with neuroendocrine component", "Presence of neuroendocrine component")
        }
        val hasSmallCellDoid = DoidEvaluationFunctions.isOfAtLeastOneDoidType(
            doidModel, tumorDoids,
            HasCancerWithSmallCellComponent.SMALL_CELL_DOIDS
        )
        val hasSmallCellDoidTerm = DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(
            doidModel, tumorDoids,
            HasCancerWithSmallCellComponent.SMALL_CELL_DOID_TERMS
        )
        val hasSmallCellDetails = TumorTypeEvaluationFunctions.hasTumorWithDetails(
            record.clinical.tumor,
            HasCancerWithSmallCellComponent.SMALL_CELL_EXTRA_DETAILS
        )
        if (hasSmallCellDoid || hasSmallCellDoidTerm || hasSmallCellDetails) {
            return EvaluationFactory.undetermined(
                "Patient has cancer with small cell component, " +
                        "undetermined if neuroendocrine component could be present as well", "Undetermined neuroendocrine component"
            )
        }
        return if (hasNeuroendocrineMolecularProfile(record)) {
            EvaluationFactory.undetermined(
                "Patient has cancer with neuroendocrine molecular profile, undetermind if considered neuroendocrine component",
                "Undetermined if neuroendocrine component"
            )
        } else
            EvaluationFactory.fail(
                "Patient does not have cancer with neuroendocrine component",
                "No neuroendocrine component"
            )
    }

    companion object {
        val NEUROENDOCRINE_DOIDS = setOf(DoidConstants.NEUROENDOCRINE_TUMOR_DOID, DoidConstants.NEUROENDOCRINE_CARCINOMA_DOID)
        val NEUROENDOCRINE_TERMS = setOf("neuroendocrine")
        val NEUROENDOCRINE_EXTRA_DETAILS = setOf("neuroendocrine", "NEC", "NET")

        private fun hasNeuroendocrineMolecularProfile(record: PatientRecord): Boolean {
            return listOf("TP53", "PTEN", "RB1").count { geneIsInactivatedForPatient(it, record) } >= 2
        }
    }
}