package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.isOfAtLeastOneDoidType
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithCommaAndOr
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.doid.CuppaToDoidMapping
import com.hartwig.actin.doid.DoidModel


class PrimaryTumorLocationBelongsToDoid(
    private val doidModel: DoidModel,
    private val cuppaToDoidMapping: CuppaToDoidMapping,
    private val doidsToMatch: Set<String>,
    private val specificQuery: String?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val result = evaluateForDoids(record, record.tumor.doids)
        if (result.result != EvaluationResult.FAIL) {
            return result
        }
        return evaluateCuppaForCup(record) ?: result
    }

    private fun evaluateForDoids(record: PatientRecord, tumorDoids: Set<String>?): Evaluation {
        return if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            EvaluationFactory.undetermined("Unknown tumor type")
        } else {
            val doidsTumorBelongsTo =
                DoidEvaluationFunctions.createFullExpandedDoidTree(doidModel, tumorDoids).intersect(doidsToMatch.toSet())
            val doidTermsTumorBelongsTo = Format.concat(doidsToTerms(doidsTumorBelongsTo))
            val potentialAdenoSquamousMatches = isPotentialAdenoSquamousMatch(tumorDoids!!, doidsToMatch)
            val undeterminedUnderMainCancerTypes = isUndeterminedUnderMainCancerType(tumorDoids, doidsToMatch)

            when {
                !DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids) -> EvaluationFactory.undetermined("Unknown tumor type")

                doidsTumorBelongsTo.isNotEmpty() && specificQuery != null -> {
                    val name = record.tumor.name
                    when {
                        name.lowercase().contains(specificQuery.lowercase()) ->
                            EvaluationFactory.pass("Tumor belongs to $doidTermsTumorBelongsTo with specific request '$specificQuery'")

                        else -> EvaluationFactory.warn("Tumor belongs to $doidTermsTumorBelongsTo but undetermined if '$specificQuery'")
                    }
                }

                doidsTumorBelongsTo.isNotEmpty() -> EvaluationFactory.pass("Tumor belongs to DOID term(s) $doidTermsTumorBelongsTo")

                potentialAdenoSquamousMatches.isNotEmpty() -> {
                    val potentialAdenoSquamousMatchesString = concatLowercaseWithCommaAndOr(doidsToTerms(potentialAdenoSquamousMatches))
                    EvaluationFactory.warn("Unclear if tumor type is considered $potentialAdenoSquamousMatchesString")
                }

                undeterminedUnderMainCancerTypes.isNotEmpty() -> {
                    val terms = concatLowercaseWithCommaAndOr(doidsToTerms(undeterminedUnderMainCancerTypes.toSet()))
                    EvaluationFactory.undetermined("Undetermined if $terms")
                }

                else -> EvaluationFactory.fail("No ${concatLowercaseWithCommaAndOr(doidsToTerms(doidsToMatch))}")
            }
        }
    }

    private fun evaluateCuppaForCup(record: PatientRecord): Evaluation? {
        if (!TumorEvaluationFunctions.hasCancerOfUnknownPrimary(record.tumor.name)) {
            return null
        }

        val orangeRecord = MolecularHistory(record.molecularTests).latestOrangeMolecularRecord() ?: return null
        if (!orangeRecord.hasSufficientQuality) {
            return null
        }

        val predictedTumorOrigin = orangeRecord.characteristics.predictedTumorOrigin ?: return null
        val likelihood = predictedTumorOrigin.likelihood()
        if (likelihood < CUPPA_CONFIDENCE_THRESHOLD) {
            return null
        }

        val cancerType = predictedTumorOrigin.cancerType()
        val cuppaDoids = cuppaToDoidMapping.doidsForCuppaType(cancerType) ?: return null

        val cuppaResult = evaluateForDoids(record, cuppaDoids)
        return if (cuppaResult.result == EvaluationResult.PASS) {
            val likelihoodPct = (likelihood * 100).toInt()
            EvaluationFactory.warn("Tumor type undetermined but CUPPA predicts $cancerType ($likelihoodPct%)")
        } else {
            null
        }
    }

    companion object {
        private const val CUPPA_CONFIDENCE_THRESHOLD = 0.8
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
        return tumorDoids.intersect(DoidConstants.NEUROENDOCRINE_DOIDS)
            .isNotEmpty() && fullDoidToMatchTree.intersect(DoidConstants.NEUROENDOCRINE_DOIDS).isEmpty()
    }

    private fun doidsToTerms(doids: Set<String>): Set<String> {
        return doids.mapNotNull(doidModel::resolveTermForDoid).toSet()
    }
}
