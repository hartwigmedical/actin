package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.doid.DoidModel
import org.apache.logging.log4j.LogManager

internal object DoidEvaluationFunctions {

    private val LOGGER = LogManager.getLogger(DoidEvaluationFunctions::class.java)
    fun hasConfiguredDoids(tumorDoids: Set<String?>?): Boolean {
        return !tumorDoids.isNullOrEmpty()
    }

    fun isOfDoidType(doidModel: DoidModel, tumorDoids: Set<String>?, doidToMatch: String): Boolean {
        return isOfAtLeastOneDoidType(doidModel, tumorDoids, setOf(doidToMatch))
    }

    fun isOfAtLeastOneDoidType(doidModel: DoidModel, tumorDoids: Set<String>?, doidsToMatch: Set<String>): Boolean {
        val fullExpandedDoidTree = createFullExpandedDoidTree(doidModel, tumorDoids)
        for (doidToMatch in doidsToMatch) {
            if (fullExpandedDoidTree.contains(doidToMatch)) {
                return true
            }
        }
        return false
    }

    fun isOfAtLeastOneDoidTerm(doidModel: DoidModel, tumorDoids: Set<String>?, doidTermsToMatch: Set<String>): Boolean {
        for (doid in createFullExpandedDoidTree(doidModel, tumorDoids)) {
            val term: String? = doidModel.resolveTermForDoid(doid)
            if (term == null) {
                LOGGER.warn("Could not resolve term for doid '{}'", doid)
            } else {
                if (stringCaseInsensitivelyMatchesQueryCollection(term, doidTermsToMatch)) {
                    return true
                }
            }
        }
        return false
    }

    fun isOfExactDoid(tumorDoids: Set<String>?, doidToMatch: String): Boolean {
        return tumorDoids != null && tumorDoids == setOf(doidToMatch)
    }

    fun isOfDoidCombinationType(tumorDoids: Set<String>?, validDoidCombination: Set<String>): Boolean {
        val validDoidCombinations: Set<Set<String>> = setOf(validDoidCombination)
        return hasAtLeastOneCombinationOfDoids(tumorDoids, validDoidCombinations)
    }

    fun isOfExclusiveDoidType(
        doidModel: DoidModel, tumorDoids: Set<String>?, doidToMatch: String
    ): Boolean {
        return evaluateAllDoidsMatchWithFailAndWarns(
            doidModel, tumorDoids, setOf(doidToMatch), emptySet(), emptySet()
        ) == EvaluationResult.PASS
    }

    fun evaluateAllDoidsMatchWithFailAndWarns(
        doidModel: DoidModel, tumorDoids: Set<String>?, doidsToMatch: Set<String>, failDoids: Set<String>, warnDoids: Set<String>
    ): EvaluationResult {
        if (tumorDoids == null) {
            return EvaluationResult.FAIL
        }
        val (allDoidsMatch, hasFailDoid, hasWarnDoid) = tumorDoids.map { doid ->
            val expandedDoids = doidModel.doidWithParents(doid)
            Triple(
                setsIntersect(expandedDoids, doidsToMatch),
                setsIntersect(expandedDoids, failDoids),
                setsIntersect(expandedDoids, warnDoids)
            )
        }.reduce { acc, triple -> Triple(acc.first && triple.first, acc.second || triple.second, acc.third || triple.third) }

        return when {
            !allDoidsMatch || hasFailDoid -> EvaluationResult.FAIL
            hasWarnDoid -> EvaluationResult.WARN
            else -> EvaluationResult.PASS
        }
    }

    fun hasAtLeastOneCombinationOfDoids(tumorDoids: Set<String?>?, validDoidCombinations: Set<Set<String>>): Boolean {
        if (tumorDoids == null) {
            return false
        }
        for (validDoidCombination in validDoidCombinations) {
            var containsAll = true
            for (doid in validDoidCombination) {
                if (!tumorDoids.contains(doid)) {
                    containsAll = false
                    break
                }
            }
            if (containsAll) {
                return true
            }
        }
        return false
    }

    fun createFullExpandedDoidTree(doidModel: DoidModel, doidsToExpand: Set<String>?): Set<String> {
        return doidsToExpand?.flatMap { doidModel.doidWithParents(it) }?.toSet() ?: emptySet()
    }

    private fun <T> setsIntersect(setA: Set<T>, setB: Set<T>): Boolean {
        return setA.intersect(setB).isNotEmpty()
    }
}