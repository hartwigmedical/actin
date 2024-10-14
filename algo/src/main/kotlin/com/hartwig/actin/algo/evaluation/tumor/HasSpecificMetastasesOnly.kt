package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorDetails

class HasSpecificMetastasesOnly(private val hasSpecificMetastases: (TumorDetails) -> Boolean?, private val typeOfMetastases: String) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDetails = record.tumor
        val hasSpecificMetastases = hasSpecificMetastases.invoke(tumorDetails) ?: return EvaluationFactory.undetermined(
            "Data regarding presence of $typeOfMetastases metastases is missing", "Missing $typeOfMetastases metastasis data"
        )
        val otherLesions = tumorDetails.otherLesions()
        val otherMetastasesAccessors = metastasesAccessors.filterKeys { it != this.typeOfMetastases }.values
        if (hasSpecificMetastases && tumorDetails.otherLesions() == null
            && otherMetastasesAccessors.all { it.invoke(tumorDetails) == null }
        ) {
            return EvaluationFactory.warn(
                "Patient has $typeOfMetastases lesions but data regarding other lesion locations is missing, so unknown if patient has only $typeOfMetastases metastases",
                "Lesion location data is missing: unknown if $typeOfMetastases metastases only"
            )
        }
        val hasOtherLesion = !otherLesions.isNullOrEmpty()
        val hasAnyOtherLesion = otherMetastasesAccessors.any { it.invoke(tumorDetails) == true } || hasOtherLesion

        return if (hasSpecificMetastases && !hasAnyOtherLesion) {
            EvaluationFactory.pass(
                "Patient only has $typeOfMetastases metastases",
                "${typeOfMetastases.replaceFirstChar { it.uppercase() }}-only metastases"
            )
        } else {
            EvaluationFactory.fail("Patient does not have $typeOfMetastases metastases exclusively", "No $typeOfMetastases-only metastases")
        }
    }

    companion object {
        val metastasesAccessors = mapOf<String, (TumorDetails) -> Boolean?>(
            "bone" to { it.hasBoneLesions() },
            "liver" to { it.hasLiverLesions() },
            "cns" to { it.hasCnsLesions() },
            "brain" to { it.hasBrainLesions() },
            "lung" to { it.hasLungLesions() },
            "lymphnode" to { it.hasLymphNodeLesions() }
        )
    }
}