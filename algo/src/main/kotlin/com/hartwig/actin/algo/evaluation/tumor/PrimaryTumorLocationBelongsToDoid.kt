package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.isOfAtLeastOneDoidType
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class PrimaryTumorLocationBelongsToDoid(
    private val doidModel: DoidModel,
    private val doidToMatch: String,
    private val subLocationQuery: String?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val doidTerm: String? = doidModel.resolveTermForDoid(doidToMatch)
        val tumorDoids = record.tumor.doids
        val tumorBelongsToDoid = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, doidToMatch)
        return when {
            !DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids) -> EvaluationFactory.undetermined(
                "Tumor type of patient is not configured",
                "Unknown tumor type"
            )

            tumorBelongsToDoid && subLocationQuery != null -> {
                val subLocation = record.tumor.primaryTumorSubLocation
                when {
                    subLocation != null && subLocation.lowercase()
                        .contains(subLocationQuery.lowercase()) ->
                        EvaluationFactory.pass("Tumor belongs to $doidTerm with sub-location $subLocation")

                    subLocation == null -> EvaluationFactory.warn("Tumor belongs to $doidTerm with unknown sub-location")
                    else -> EvaluationFactory.warn("Tumor belongs to $doidTerm but sub-location $subLocation does not match '$subLocationQuery'")
                }
            }

            tumorBelongsToDoid -> EvaluationFactory.pass(
                "Patient has $doidTerm",
                "Tumor type"
            )

            isPotentialAdenoSquamousMatch(tumorDoids!!, doidToMatch) -> EvaluationFactory.warn(
                "Unclear whether tumor type of patient can be considered $doidTerm, because patient has adenosquamous tumor type",
                "Unclear if tumor type is considered $doidTerm"
            )

            isUndeterminateUnderMainCancerType(tumorDoids, doidToMatch) -> EvaluationFactory.undetermined(
                "Could not determine based on configured tumor type if patient may have $doidTerm", "Undetermined if $doidTerm"
            )

            else -> EvaluationFactory.fail("Patient has no $doidTerm", "Tumor type")
        }
    }

    private fun isPotentialAdenoSquamousMatch(patientDoids: Set<String>, doidToMatch: String): Boolean {
        val doidTreeToMatch: Set<String> = doidModel.adenoSquamousMappingsForDoid(doidToMatch).map { it.adenoSquamousDoid }.toSet()
        return isOfAtLeastOneDoidType(doidModel, patientDoids, doidTreeToMatch)
    }

    private fun isUndeterminateUnderMainCancerType(tumorDoids: Set<String>, doidToMatch: String): Boolean {
        val fullDoidToMatchTree: Set<String> = doidModel.doidWithParents(doidToMatch)
        val mainCancerTypesToMatch: Set<String> = doidModel.mainCancerDoids(doidToMatch)
        return tumorDoids.any { tumorDoid ->
            val tumorDoidTree = doidModel.doidWithParents(tumorDoid)
            fullDoidToMatchTree.contains(tumorDoid) && !tumorDoidTree.contains(doidToMatch)
                    && tumorDoidTree.intersect(mainCancerTypesToMatch).isNotEmpty()
        }
    }
}