package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.isOfAtLeastOneDoidType
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithCommaAndOr
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.CuppaToDoidMapping
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.molecular.interpretation.TumorOriginInterpreter


class PrimaryTumorLocationBelongsToDoid(
    private val doidModel: DoidModel,
    private val cuppaToDoidMapping: CuppaToDoidMapping,
    private val doidsToMatch: Set<String>,
    private val specificQuery: String?,
    private val labels: EvaluationLabels.Tumor
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        return if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            EvaluationFactory.undetermined(labels.primaryTumorLocationBelongsToDoidUndeterminedUnknownType())
        } else {
            val doidsTumorBelongsTo =
                DoidEvaluationFunctions.createFullExpandedParentsDoidTree(doidModel, tumorDoids).intersect(doidsToMatch.toSet())
            val doidTermsTumorBelongsTo = Format.concat(doidsToTerms(doidsTumorBelongsTo))
            val potentialAdenoSquamousMatches = isPotentialAdenoSquamousMatch(tumorDoids!!, doidsToMatch)
            val undeterminedUnderMainCancerTypes = isUndeterminedUnderMainCancerType(tumorDoids, doidsToMatch)

            when {
                !DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids) ->
                    EvaluationFactory.undetermined(labels.primaryTumorLocationBelongsToDoidUndeterminedUnknownType())

                doidsTumorBelongsTo.isNotEmpty() && specificQuery != null -> {
                    val name = record.tumor.name
                    when {
                        name.lowercase().contains(specificQuery.lowercase()) ->
                            EvaluationFactory.pass(
                                labels.primaryTumorLocationBelongsToDoidPassSpecificQuery(doidTermsTumorBelongsTo, specificQuery)
                            )

                        else -> EvaluationFactory.warn(
                            labels.primaryTumorLocationBelongsToDoidWarnSpecificQuery(doidTermsTumorBelongsTo, specificQuery)
                        )
                    }
                }

                doidsTumorBelongsTo.isNotEmpty() -> EvaluationFactory.pass(
                    labels.primaryTumorLocationBelongsToDoidPass(doidTermsTumorBelongsTo)
                )

                potentialAdenoSquamousMatches.isNotEmpty() -> {
                    val potentialAdenoSquamousMatchesString = concatLowercaseWithCommaAndOr(doidsToTerms(potentialAdenoSquamousMatches))
                    EvaluationFactory.warn(labels.primaryTumorLocationBelongsToDoidWarnAdenoSquamous(potentialAdenoSquamousMatchesString))
                }

                undeterminedUnderMainCancerTypes.isNotEmpty() -> {
                    val terms = concatLowercaseWithCommaAndOr(doidsToTerms(undeterminedUnderMainCancerTypes.toSet()))
                    EvaluationFactory.undetermined(labels.primaryTumorLocationBelongsToDoidUndetermined(terms))
                }

                else -> {
                    evaluateCuppaPrediction(record) ?: EvaluationFactory.fail(
                        labels.primaryTumorLocationBelongsToDoidFail(concatLowercaseWithCommaAndOr(doidsToTerms(doidsToMatch)))
                    )
                }
            }
        }
    }

    private fun evaluateCuppaPrediction(record: PatientRecord): Evaluation? {
        if (!TumorEvaluationFunctions.hasCancerOfUnknownPrimary(record.tumor.name) || specificQuery != null) {
            return null
        }
        return TumorOriginInterpreter.create(record.molecularTests)
            .takeIf { it.hasConfidentPrediction() }
            ?.predictedTumorOrigin
            ?.let { predictedTumorOrigin ->
                val cancerType = predictedTumorOrigin.cancerType()
                val cuppaDoids = cuppaToDoidMapping.doidsForCuppaType(cancerType)
                    ?: throw IllegalArgumentException("CUPPA cancer type $cancerType not found in mapping")

                val parents = DoidEvaluationFunctions.createFullExpandedParentsDoidTree(doidModel, cuppaDoids.included)
                val children = DoidEvaluationFunctions.createFullExpandedChildrenDoidTree(doidModel, cuppaDoids.included, cuppaDoids.excluded ?: emptySet())

                (parents + children).intersect(doidsToMatch)
                    .firstOrNull()
                    ?.let {
                        val likelihoodPct = (predictedTumorOrigin.likelihood() * 100).toInt()
                        EvaluationFactory.warn(labels.primaryTumorLocationBelongsToDoidWarnCuppa(cancerType, likelihoodPct))
                    }
            }
    }

    private fun isPotentialAdenoSquamousMatch(tumorDoids: Set<String>, doidsToMatch: Set<String>): Set<String> {
        return doidsToMatch.filter { doidToMatch ->
            val doidTreeToMatch: Set<String> = doidModel.adenoSquamousMappingsForDoid(doidToMatch).map { it.adenoSquamousDoid }.toSet()
            isOfAtLeastOneDoidType(doidModel, tumorDoids, doidTreeToMatch)
        }.toSet()
    }

    private fun isUndeterminedUnderMainCancerType(tumorDoids: Set<String>, doidsToMatch: Set<String>): List<String> {
        return doidsToMatch.filter { doidToMatch ->
            val fullDoidToMatchTree: Set<String> = doidModel.doidWithParents(doidToMatch)
            val mainCancerTypesToMatch: Set<String> = doidModel.mainCancerDoids(doidToMatch)
            tumorDoids.any { tumorDoid ->
                val tumorDoidTree = doidModel.doidWithParents(tumorDoid)
                fullDoidToMatchTree.contains(tumorDoid) && !tumorDoidTree.contains(doidToMatch)
                        && tumorDoidTree.intersect(mainCancerTypesToMatch).isNotEmpty()
                        && !hasNeuroendocrineDoidAndNoNeuroendocrineDoidToMatch(tumorDoids, fullDoidToMatchTree)
            }
        }
    }

    private fun hasNeuroendocrineDoidAndNoNeuroendocrineDoidToMatch(tumorDoids: Set<String>, fullDoidToMatchTree: Set<String>): Boolean {
        return tumorDoids.intersect(DoidConstants.NEUROENDOCRINE_DOIDS).isNotEmpty()
                && fullDoidToMatchTree.intersect(DoidConstants.NEUROENDOCRINE_DOIDS).isEmpty()
    }

    private fun doidsToTerms(doids: Set<String>): Set<String> {
        return doids.mapNotNull(doidModel::resolveTermForDoid).toSet()
    }
}
