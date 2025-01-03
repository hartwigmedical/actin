package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.TumorDetails

//TODO (CB)!
class HasSpecificMetastasesOnly(
    private val hasSpecificMetastases: (TumorDetails) -> Boolean?,
    private val hasSuspectedSpecificMetastases: (TumorDetails) -> Boolean?,
    private val typeOfMetastases: String
) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            val hasSpecificMetastases = hasSpecificMetastases(this)
                ?: return EvaluationFactory.undetermined("Missing $typeOfMetastases metastasis data")
            val hasSuspectedSpecificMetastases = hasSuspectedSpecificMetastases(this) ?: false

            val otherMetastasesAccessors = confirmedCategoricalLesionList() - hasSpecificMetastases
            val suspectedOtherMetastasesAccessors = suspectedCategoricalLesionList() - hasSuspectedSpecificMetastases
            val hasAnyOtherLesion = otherMetastasesAccessors.any { it == true } || !otherLesions.isNullOrEmpty()
            val hasSuspectedOtherLesion = !otherSuspectedLesions.isNullOrEmpty() || suspectedOtherMetastasesAccessors.any { it == true }

            val metastasisString = "${typeOfMetastases.replaceFirstChar { it.uppercase() }}-only metastases"

            return when {
                hasSpecificMetastases && !hasAnyOtherLesion && otherLesions == null && otherMetastasesAccessors.any { it == null } -> {
                    EvaluationFactory.warn("Lesion location data is missing: unknown if $typeOfMetastases metastases only")
                }

                hasSpecificMetastases && !hasAnyOtherLesion -> {
                    if (hasSuspectedOtherLesion) {
                        EvaluationFactory.warn("Uncertain $metastasisString - suspected other lesions present")
                    } else {
                        EvaluationFactory.pass(metastasisString)
                    }
                }

                hasSuspectedSpecificMetastases && !hasAnyOtherLesion -> {
                    if (hasSuspectedOtherLesion) {
                        EvaluationFactory.warn("Uncertain $metastasisString - lesion is suspected and other suspected lesion(s) present as well")
                    } else {
                        EvaluationFactory.warn("Uncertain $metastasisString - lesion is suspected only")
                    }
                }

                else -> {
                    EvaluationFactory.fail("No $typeOfMetastases-only metastases")
                }
            }
        }
    }
}