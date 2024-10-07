package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator.geneIsInactivatedForPatient
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel
import java.time.LocalDate

val NEUROENDOCRINE_DOIDS = setOf(DoidConstants.NEUROENDOCRINE_TUMOR_DOID, DoidConstants.NEUROENDOCRINE_CARCINOMA_DOID)
val NEUROENDOCRINE_TERMS = setOf("neuroendocrine")
val NEUROENDOCRINE_EXTRA_DETAILS = setOf("neuroendocrine", "NEC", "NET")

class HasCancerWithNeuroendocrineComponent(private val doidModel: DoidModel, private val maxTestAge: LocalDate? = null) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids) && record.tumor.primaryTumorExtraDetails == null) {
            return EvaluationFactory.undetermined(
                "Could not determine whether tumor of patient may have a neuroendocrine component",
                "Undetermined neuroendocrine component"
            )
        }
        val hasNeuroendocrineDoid = DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, tumorDoids, NEUROENDOCRINE_DOIDS)
        val hasNeuroendocrineTerm = DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(doidModel, tumorDoids, NEUROENDOCRINE_TERMS)
        val hasNeuroendocrineDetails =
            TumorTypeEvaluationFunctions.hasTumorWithDetails(record.tumor, NEUROENDOCRINE_EXTRA_DETAILS)
        if (hasNeuroendocrineDoid || hasNeuroendocrineTerm || hasNeuroendocrineDetails) {
            return EvaluationFactory.pass("Patient has cancer with neuroendocrine component", "Presence of neuroendocrine component")
        }
        val hasSmallCellDoid = DoidEvaluationFunctions.isOfAtLeastOneDoidType(
            doidModel, tumorDoids,
            DoidConstants.SMALL_CELL_DOID_SET
        )
        val hasSmallCellDetails = TumorTypeEvaluationFunctions.hasTumorWithDetails(
            record.tumor,
            HasCancerWithSmallCellComponent.SMALL_CELL_EXTRA_DETAILS
        )
        if (hasSmallCellDoid || hasSmallCellDetails) {
            return EvaluationFactory.undetermined(
                "Patient has cancer with small cell component, " +
                        "undetermined if neuroendocrine component could be present as well", "Undetermined neuroendocrine component"
            )
        }
        return if (hasNeuroendocrineMolecularProfile(record).first) {
            val message = "Neuroendocrine molecular profile " +
                    " (inactivated genes: ${hasNeuroendocrineMolecularProfile(record).second.joinToString(", ")})"
            EvaluationFactory.undetermined(
                "$message - undetermined if considered cancer with neuroendocrine component",
                "$message - undetermined if considered cancer with neuroendocrine component"
            )
        } else
            EvaluationFactory.fail(
                "Patient does not have cancer with neuroendocrine component",
                "No neuroendocrine component"
            )
    }

    private fun hasNeuroendocrineMolecularProfile(record: PatientRecord): Pair<Boolean, List<String>> {
        val genes = listOf("TP53", "PTEN", "RB1")
        val inactivatedGenes = genes.filter { geneIsInactivatedForPatient(it, record, maxTestAge) }
        return Pair(inactivatedGenes.size >= 2, inactivatedGenes)
    }
}