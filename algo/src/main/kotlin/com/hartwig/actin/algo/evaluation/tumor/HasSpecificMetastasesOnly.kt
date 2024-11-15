package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorDetails

class HasSpecificMetastasesOnly(
    private val hasSpecificMetastases: (TumorDetails) -> Boolean?,
    private val hasSuspectedSpecificMetastases: (TumorDetails) -> Boolean?,
    private val typeOfMetastases: String
) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            val hasSpecificMetastases = hasSpecificMetastases(this)
                ?: return EvaluationFactory.undetermined(
                    "Data regarding presence of $typeOfMetastases metastases is missing",
                    "Missing $typeOfMetastases metastasis data"
                )
            val hasSuspectedSpecificMetastases = hasSuspectedSpecificMetastases(this) ?: false

            val otherMetastasesAccessors = confirmedCategoricalLesionList() - hasSpecificMetastases
            val suspectedOtherMetastasesAccessors = suspectedCategoricalLesionList() - hasSuspectedSpecificMetastases
            val hasAnyOtherLesion = otherMetastasesAccessors.any { it == true } || !otherLesions.isNullOrEmpty()
            val hasSuspectedOtherLesion = !otherSuspectedLesions.isNullOrEmpty() || suspectedOtherMetastasesAccessors.any { it == true }

            val metastasisString = "${typeOfMetastases.replaceFirstChar { it.uppercase() }}-only metastases"

            return when {
                hasSpecificMetastases && !hasAnyOtherLesion && otherLesions == null && otherMetastasesAccessors.any { it == null } -> {
                    EvaluationFactory.warn(
                        "Patient has $typeOfMetastases lesions but data regarding other lesion locations is missing " +
                                "so unknown if patient has only $typeOfMetastases metastases",
                        "Lesion location data is missing: unknown if $typeOfMetastases metastases only"
                    )
                }

                hasSpecificMetastases && !hasAnyOtherLesion -> {
                    val specificMessage = "Patient only has $typeOfMetastases metastases"

                    if (hasSuspectedOtherLesion) {
                        EvaluationFactory.warn(
                            "$specificMessage but suspected other lesion(s) present as well",
                            "Uncertain $metastasisString - suspected other lesions present"
                        )
                    } else {
                        EvaluationFactory.pass(specificMessage, metastasisString)
                    }
                }

                hasSuspectedSpecificMetastases && !hasAnyOtherLesion -> {
                    val specificMessage = "Patient only has $typeOfMetastases metastases but lesion is suspected only"

                    if (hasSuspectedOtherLesion) {
                        EvaluationFactory.warn(
                            "$specificMessage and suspected other lesion(s) present as well",
                            "Uncertain $metastasisString - lesion is suspected and other suspected lesion(s) present as well"
                        )
                    } else {
                        EvaluationFactory.warn(
                            specificMessage,
                            "Uncertain $metastasisString - lesion is suspected only"
                        )
                    }
                }

                else -> {
                    EvaluationFactory.fail(
                        "Patient does not have $typeOfMetastases metastases exclusively",
                        "No $typeOfMetastases-only metastases"
                    )
                }
            }
        }
    }
}