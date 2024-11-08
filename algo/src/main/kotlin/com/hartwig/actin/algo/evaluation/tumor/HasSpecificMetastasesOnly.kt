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
        val tumorDetails = record.tumor
        val hasSpecificMetastases = hasSpecificMetastases.invoke(tumorDetails) ?: return EvaluationFactory.undetermined(
            "Data regarding presence of $typeOfMetastases metastases is missing", "Missing $typeOfMetastases metastasis data"
        )
        val otherLesions = tumorDetails.otherLesions
        val otherMetastasesAccessors = metastasesAccessors - this.hasSpecificMetastases
        val suspectedOtherMetastasesAccessors = suspectedMetastasesAccessors - this.hasSuspectedSpecificMetastases
        val hasOtherLesion = !otherLesions.isNullOrEmpty()
        val hasAnyOtherLesion = otherMetastasesAccessors.any { it.invoke(tumorDetails) == true } || hasOtherLesion
        val hasSuspectedOtherLesion =
            !tumorDetails.otherSuspectedLesions.isNullOrEmpty() || suspectedOtherMetastasesAccessors.any { it.invoke(tumorDetails) == true }

        val metastasisString = "${typeOfMetastases.replaceFirstChar { it.uppercase() }}-only metastases"

        return when {
            hasSpecificMetastases && !hasAnyOtherLesion && otherLesions == null && otherMetastasesAccessors.any { it.invoke(tumorDetails) == null } -> {
                EvaluationFactory.warn(
                    "Patient has $typeOfMetastases lesions but data regarding other lesion locations is missing " +
                            "so unknown if patient has only $typeOfMetastases metastases",
                    "Lesion location data is missing: unknown if $typeOfMetastases metastases only"
                )
            }

            hasSpecificMetastases && !hasAnyOtherLesion -> {
                if (hasSuspectedOtherLesion) {
                    EvaluationFactory.undetermined(
                        "Undetermined if $metastasisString - suspected other lesion(s) present",
                        "Undetermined if $metastasisString - suspected other lesion(s) present",
                    )
                } else {
                    EvaluationFactory.pass("Patient only has $typeOfMetastases metastases", metastasisString)
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

    companion object {
        val metastasesAccessors = setOf(
            TumorDetails::hasBrainLesions,
            TumorDetails::hasLiverLesions,
            TumorDetails::hasCnsLesions,
            TumorDetails::hasBoneLesions,
            TumorDetails::hasLungLesions,
            TumorDetails::hasLymphNodeLesions
        )

        val suspectedMetastasesAccessors = setOf(
            TumorDetails::hasSuspectedBrainLesions,
            TumorDetails::hasSuspectedLiverLesions,
            TumorDetails::hasSuspectedCnsLesions,
            TumorDetails::hasSuspectedBoneLesions,
            TumorDetails::hasSuspectedLungLesions,
            TumorDetails::hasSuspectedLymphNodeLesions
        )
    }
}