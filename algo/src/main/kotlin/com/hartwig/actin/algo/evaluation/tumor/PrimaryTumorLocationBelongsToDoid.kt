package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.isOfAtLeastOneDoidType
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithCommaAndOr
import com.hartwig.actin.algo.evaluation.util.Format.concatStringsWithAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class PrimaryTumorLocationBelongsToDoid(
    private val doidModel: DoidModel,
    private val doidsToMatch: List<String>,
    private val subLocationQuery: String?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val doidTerms: Set<String> = doidsToMatch.mapNotNull { doidModel.resolveTermForDoid(it) }.toSet()
        val tumorDoids = record.tumor.doids
        val doidsTumorBelongsTo = doidsToMatch.filter { DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, it) }
        val doidTermsTumorBelongsTo = doidsTumorBelongsTo.mapNotNull { doidModel.resolveTermForDoid(it) }.toSet()
        val potentialAdenoSquamousMatches = if (tumorDoids == null) {
            emptyList()
        } else {
            isPotentialAdenoSquamousMatch(tumorDoids, doidsToMatch)
        }
        val undeterminatedUnderMainCancerTypes = if (tumorDoids == null) {
            emptyList()
        } else {
            isUndeterminateUnderMainCancerType(tumorDoids, doidsToMatch)
        }

        return when {
            !DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids) -> EvaluationFactory.undetermined(
                "Tumor type of patient is not configured",
                "Unknown tumor type"
            )

            doidsTumorBelongsTo.isNotEmpty() && subLocationQuery != null -> {
                val subLocation = record.tumor.primaryTumorSubLocation
                when {
                    subLocation != null && subLocation.lowercase()
                        .contains(subLocationQuery.lowercase()) ->
                        EvaluationFactory.pass("Tumor belongs to ${concatStringsWithAnd(doidTermsTumorBelongsTo)} with sub-location $subLocation")

                    subLocation == null -> EvaluationFactory.warn("Tumor belongs to ${concatStringsWithAnd(doidTermsTumorBelongsTo)} with unknown sub-location")
                    else -> EvaluationFactory.warn("Tumor belongs to ${concatStringsWithAnd(doidTermsTumorBelongsTo)} but sub-location $subLocation does not match '$subLocationQuery'")
                }
            }

            doidsTumorBelongsTo.isNotEmpty() -> EvaluationFactory.pass(
                "Patient has ${concatStringsWithAnd(doidTermsTumorBelongsTo)}",
                "Has ${concatStringsWithAnd(doidTermsTumorBelongsTo)}"
            )

            potentialAdenoSquamousMatches.isNotEmpty() -> EvaluationFactory.warn(
                "Unclear whether tumor type of patient can be considered ${concatLowercaseWithCommaAndOr(potentialAdenoSquamousMatches)}, because patient has adenosquamous tumor type",
                "Unclear if tumor type is considered ${concatLowercaseWithCommaAndOr(potentialAdenoSquamousMatches)}"
            )

            undeterminatedUnderMainCancerTypes.isNotEmpty() -> EvaluationFactory.undetermined(
                "Could not determine based on configured tumor type if patient may have ${
                    concatLowercaseWithCommaAndOr(
                        undeterminatedUnderMainCancerTypes
                    )
                }",
                "Undetermined if ${concatLowercaseWithCommaAndOr(undeterminatedUnderMainCancerTypes)}"
            )

            else -> EvaluationFactory.fail(
                "Patient has no ${concatLowercaseWithCommaAndOr(doidTerms)}",
                "No ${concatLowercaseWithCommaAndOr(doidTerms)}"
            )
        }
    }

    private fun isPotentialAdenoSquamousMatch(patientDoids: Set<String>, doidsToMatch: List<String>): List<String> {
        return doidsToMatch.filter { doidToMatch ->
            val doidTreeToMatch: Set<String> = doidModel.adenoSquamousMappingsForDoid(doidToMatch).map { it.adenoSquamousDoid }.toSet()
            isOfAtLeastOneDoidType(doidModel, patientDoids, doidTreeToMatch)
        }
    }

    private fun isUndeterminateUnderMainCancerType(tumorDoids: Set<String>, doidsToMatch: List<String>): List<String> {
        return doidsToMatch.filter { doidToMatch ->
            val fullDoidToMatchTree: Set<String> = doidModel.doidWithParents(doidToMatch)
            val mainCancerTypesToMatch: Set<String> = doidModel.mainCancerDoids(doidToMatch)
            tumorDoids.any { tumorDoid ->
                val tumorDoidTree = doidModel.doidWithParents(tumorDoid)
                fullDoidToMatchTree.contains(tumorDoid) && !tumorDoidTree.contains(doidToMatch)
                        && tumorDoidTree.intersect(mainCancerTypesToMatch).isNotEmpty()
            }
        }
    }
}