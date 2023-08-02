package com.hartwig.actin.algo

import com.hartwig.actin.algo.datamodel.CohortMatch
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.algo.datamodel.TrialMatch
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.treatment.datamodel.CriterionReference
import com.hartwig.actin.treatment.datamodel.Eligibility
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule
import com.hartwig.actin.treatment.datamodel.TrialIdentification
import com.hartwig.actin.treatment.input.composite.CompositeRules
import org.apache.logging.log4j.LogManager
import kotlin.system.exitProcess

class MatchOutputComparisonApplication {

    fun run(): Int {
        LOGGER.info("Running ACTIN Test Match Output Comparison Application with $OLD_MATCH_FILE and $NEW_MATCH_FILE")

        val oldMatches = TreatmentMatchJson.read(OLD_MATCH_FILE)
        val newMatches = TreatmentMatchJson.read(NEW_MATCH_FILE)

        extractDifferences(oldMatches, newMatches, mapOf(
            "patientId" to TreatmentMatch::patientId,
            "sampleId" to TreatmentMatch::sampleId,
            "referenceDate" to TreatmentMatch::referenceDate
        )).forEach(::log)

        val oldTrialSummary = trialMatchesById(oldMatches)
        val newTrialSummary = trialMatchesById(newMatches)

        mapKeyDifferences(oldTrialSummary, newTrialSummary, "trials", TrialIdentification::trialId).forEach(::log)

        oldTrialSummary.map { (key, oldTrialMatch) ->
            val newTrialMatch = newTrialSummary[key]
            if (newTrialMatch != null) {
//                log("Comparing trial ${key.trialId()}")
                extractDifferences(oldTrialMatch, newTrialMatch, mapOf("eligibility" to TrialMatch::isPotentiallyEligible)).forEach(::log)
                reportEvaluationDifferences(oldTrialMatch.evaluations(), newTrialMatch.evaluations(), key.trialId(), 0)

                val oldCohortSummary = cohortMatchesById(oldTrialMatch)
                val newCohortSummary = cohortMatchesById(newTrialMatch)
                mapKeyDifferences(oldCohortSummary, newCohortSummary, "cohorts") { it }.forEach { log(it, INDENT_WIDTH) }
                oldCohortSummary.map { (key, oldCohortMatch) ->
                    val newCohortMatch = newCohortSummary[key]
                    if (newCohortMatch != null) {
//                        log("Comparing cohort $key", INDENT_WIDTH)
                        extractDifferences(oldCohortMatch, newCohortMatch, mapOf("eligibility" to CohortMatch::isPotentiallyEligible))
                            .forEach { log(it, INDENT_WIDTH) }
                        reportEvaluationDifferences(oldCohortMatch.evaluations(), newCohortMatch.evaluations(), key, INDENT_WIDTH)
                    }
                }
            }
        }

//        LOGGER.info(
//            "${summary.nameToDrugDifferences.size} name-to-drug conversions:"
//                    + summary.otherDifferences.joinToString("\n")
//                    "${summary.otherDifferences.size} other differences:\n"
//                    + summary.otherDifferences.joinToString("\n")
//        )

        return 0
    }

    companion object {
        private const val OLD_MATCH_FILE = "../../tmp/old/ACTN01020254.treatment_match.json"
        private const val NEW_MATCH_FILE = "../../tmp/ACTN01020254.treatment_match.json"
        private const val INDENT_WIDTH = 2
        private val LOGGER = LogManager.getLogger(MatchOutputComparisonApplication::class.java)

        private fun reportEvaluationDifferences(
            oldEvaluations: Map<Eligibility, Evaluation>, newEvaluations: Map<Eligibility, Evaluation>, id: String, indent: Int
        ) {
            val oldEvaluationsByCriteria = evaluationsByCriteria(oldEvaluations)
            val newEvaluationsByCriteria = evaluationsByCriteria(newEvaluations)
            val detailIndent = indent + INDENT_WIDTH
            mapKeyDifferences(oldEvaluationsByCriteria, newEvaluationsByCriteria, "evaluations") { it.toString() }
                .forEach { log(it, indent) }
            oldEvaluationsByCriteria.map { (references, oldFunctionAndEvaluation) ->
                val (oldFunction, oldEvaluation) = oldFunctionAndEvaluation
                val (newFunction, newEvaluation) = newEvaluationsByCriteria[references]!!
                val functionDifferences = extractFunctionDifferences(listOf(oldFunction), listOf(newFunction))
                val evaluationDifferences = extractDifferences(oldEvaluation, newEvaluation, mapOf(
                    "result" to Evaluation::result, "recoverable" to Evaluation::recoverable
                ))
                val messageDifferences = extractMessageDifferences(oldEvaluation, newEvaluation)
                if (functionDifferences.isNotEmpty() || evaluationDifferences.isNotEmpty() || messageDifferences.isNotEmpty()) {
                    log("Differences found for ID $id, Criteria ${references.joinToString(", ") { it.id() }}:", indent)
                    evaluationDifferences.forEach { log(it, detailIndent) }
//                    log("Messages:", detailIndent)
                    messageDifferences.forEach { log(it, detailIndent) }
//                    if (functionDifferences.isNotEmpty()) {
//                        log("Eligibility functions:", detailIndent)
                    functionDifferences.logSummary(detailIndent)
//                    }
                }
            }
        }

        private fun evaluationsByCriteria(evaluations: Map<Eligibility, Evaluation>): Map<Set<CriterionReference>, Pair<EligibilityFunction, Evaluation>> {
            return evaluations.map { (eligibility, evaluation) -> eligibility.references() to Pair(eligibility.function(), evaluation) }.toMap()
        }

        private fun extractMessageDifferences(old: Evaluation, new: Evaluation): List<String> {
            val oldMessages = getGeneralMessagesForEvaluation(old)
            val newMessages = getGeneralMessagesForEvaluation(new)
            val removedMessages = oldMessages - newMessages
            val addedMessages = newMessages - oldMessages
            return if (removedMessages.map(String::lowercase) == addedMessages.map(String::lowercase)) {
                emptyList()
//                listOf("~ ${removedMessages.joinToString("; ")} -> ${addedMessages.joinToString("; ")}")
            } else {
                removedMessages.map { "- $it" } + addedMessages.map { "+ $it" }
            }
        }

        private fun getGeneralMessagesForEvaluation(evaluation: Evaluation): Set<String> {
            return when (evaluation.result()) {
                EvaluationResult.PASS -> evaluation.passGeneralMessages()
                EvaluationResult.NOT_EVALUATED -> evaluation.passGeneralMessages()
                EvaluationResult.WARN -> evaluation.warnGeneralMessages()
                EvaluationResult.UNDETERMINED -> evaluation.undeterminedGeneralMessages()
                EvaluationResult.FAIL -> evaluation.failGeneralMessages()
                else -> emptySet()
            }
        }

        private tailrec fun extractFunctionDifferences(
            oldFunctions: List<EligibilityFunction>,
            newFunctions: List<EligibilityFunction>,
            differences: FunctionDifferences = FunctionDifferences()
        ): FunctionDifferences {
            return if (oldFunctions.isEmpty() || newFunctions.isEmpty()) {
                differences
            } else {
                val (oldFunctionsToAdd, newFunctionsToAdd, newDifferences) = functionDifferences(oldFunctions.first(), newFunctions.first())
//                val (updatedOldFunctions, updatedNewFunctions, updatedDifferences) = when {
//                    oldFunction == newFunction ->
//                        Triple(oldFunctions.drop(1), newFunctions.drop(1), differences)
//                    oldFunction.rule() == EligibilityRule.HAS_HAD_TREATMENT_NAME_X
//                            && newFunction.rule() == EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X
//                            && oldFunction.parameters().size == 1
//                            && oldFunction.parameters() == newFunction.parameters() ->
//                        Triple(oldFunctions.drop(1), newFunctions.drop(1), differences.copy(
//                            nameToDrugDifferences = differences.nameToDrugDifferences + oldFunction.parameters()[0].toString())
//                        )
//                    oldFunction.rule() == EligibilityRule.OR
//                            && oldFunction.parameters().all { it is EligibilityFunction && it.rule() == EligibilityRule.HAS_HAD_TREATMENT_NAME_X }
//                            && newFunction.rule() == EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X
//                            && oldFunction.parameters().flatMap { (it as? EligibilityFunction)?.parameters() ?: emptyList() } == newFunction.parameters() ->
//                        Triple(oldFunctions.drop(1), newFunctions.drop(1), differences.copy(
//                            nameToDrugDifferences = differences.nameToDrugDifferences + newFunction.parameters().joinToString(",")
//                        ))
//
//                    else -> Triple(oldFunctions.drop(1), newFunctions.drop(1), differences)
//                }
                extractFunctionDifferences(
                    oldFunctions.drop(1) + oldFunctionsToAdd,
                    newFunctions.drop(1) + newFunctionsToAdd,
                    differences + newDifferences
                )
            }
        }

        private fun functionDifferences(oldFunction: EligibilityFunction, newFunction: EligibilityFunction):
                Triple<List<EligibilityFunction>, List<EligibilityFunction>, FunctionDifferences> {
            return when {
                oldFunction == newFunction ->
                    Triple(emptyList(), emptyList(), FunctionDifferences())

                newFunction.rule() == EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X ->
                    Triple(emptyList(), emptyList(), FunctionDifferences(nameToDrugDifferences = listOf(
                        "${oldFunction.rule()}[${paramString(oldFunction)}] -> DRUGS[${newFunction.parameters()[0]}]"
                    )))

                oldFunction.rule() == EligibilityRule.OR && newFunction.rule() == EligibilityRule.OR && newFunction.parameters()
                    .mapNotNull { (it as? EligibilityFunction)?.rule() }
                    .all { it == EligibilityRule.HAS_HAD_TREATMENT_NAME_X || it == EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X } -> {
                    Triple(emptyList(), emptyList(), FunctionDifferences(nameToDrugDifferences = listOf(
                        "${oldFunction.rule()}[${paramString(oldFunction)}] -> \"${newFunction.rule()}[${paramString(newFunction)}]}]"
                    )))
                }
//                        oldFunction.rule() == EligibilityRule.HAS_HAD_TREATMENT_NAME_X
//                        && newFunction.rule() == EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X
//                        && oldFunction.parameters().size == 1
//                        && oldFunction.parameters() == newFunction.parameters() ->
//                    Triple(emptyList(), emptyList(), FunctionDifferences(nameToDrugDifferences = listOf(oldFunction.parameters()[0].toString())))
//
//                oldFunction.rule() == EligibilityRule.OR
//                        && oldFunction.parameters().all { it is EligibilityFunction && it.rule() == EligibilityRule.HAS_HAD_TREATMENT_NAME_X }
//                        && newFunction.rule() == EligibilityRule.HAS_HAD_TREATMENT_WITH_ANY_DRUG_X -> {
//                    val oldParams = oldFunction.parameters()
//                        .flatMap { (it as? EligibilityFunction)?.parameters() ?: emptyList() }
//                        .joinToString(";")
//                    val changeString = "$oldParams -> ${newFunction.parameters()[0]}"
//                    Triple(emptyList(), emptyList(), FunctionDifferences(nameToDrugDifferences = listOf(changeString)))
//                }
                CompositeRules.isComposite(oldFunction.rule()) && CompositeRules.isComposite(newFunction.rule()) -> {
                    val ruleDifferences = if (oldFunction.rule() != newFunction.rule()) {
                        listOf("${oldFunction.rule()} != ${newFunction.rule()}")
                    } else {
                        emptyList()
                    }
                    Triple(
                        oldFunction.parameters().mapNotNull { it as? EligibilityFunction },
                        newFunction.parameters().mapNotNull { it as? EligibilityFunction },
                        FunctionDifferences(otherDifferences = ruleDifferences)
                    )
                }

                oldFunction.rule() == newFunction.rule() ->
                    Triple(emptyList(), emptyList(), FunctionDifferences(parameterDifferences = listOf("${paramString(oldFunction)} != ${paramString(newFunction)}")))

                else -> Triple(emptyList(), emptyList(), FunctionDifferences(otherDifferences = listOf("$oldFunction != $newFunction")))
            }
        }

        private fun paramString(eligibilityFunction: EligibilityFunction) = eligibilityFunction.parameters().joinToString(";")

        private fun <T> extractDifferences(old: T, new: T, properties: Map<String, (T) -> Any>): List<String> {
            return properties.mapNotNull { (description, property) ->
                val oldValue = property(old)
                val newValue = property(new)
                if (oldValue != newValue) "> ${old!!::class.java.simpleName} difference in $description: $oldValue != $newValue" else null
            }
        }

        private fun trialMatchesById(treatmentMatch: TreatmentMatch): Map<TrialIdentification, TrialMatch> {
            return treatmentMatch.trialMatches().associateBy(TrialMatch::identification)
        }

        private fun cohortMatchesById(trialMatch: TrialMatch): Map<String, CohortMatch> {
            return trialMatch.cohorts().associateBy { it.metadata().cohortId() }
        }

        private fun <T> mapKeyDifferences(old: Map<T, Any>, new: Map<T, Any>, description: String, keyToString: (T) -> String): List<String> {
            val added = new.keys - old.keys
            val removed = old.keys - new.keys
            return listOf("added" to added, "removed" to removed)
                .filter { (_, differences) -> differences.isNotEmpty() }
                .map { (modification, differences) ->
                    "${differences.size} $description were $modification: ${Format.concat(differences.map(keyToString))}"
                }
        }

        private fun log(message: String, indent: Int = 0) {
            LOGGER.debug(" ".repeat(indent) + message)
        }
    }

    private data class FunctionDifferences(
        val nameToDrugDifferences: List<String> = emptyList(),
        val parameterDifferences: List<String> = emptyList(),
        val otherDifferences: List<String> = emptyList()
    ) {

        operator fun plus(other: FunctionDifferences): FunctionDifferences {
            return FunctionDifferences(
                nameToDrugDifferences = nameToDrugDifferences + other.nameToDrugDifferences,
                parameterDifferences = parameterDifferences + other.parameterDifferences,
                otherDifferences = otherDifferences + other.otherDifferences)
        }

        fun isNotEmpty() = nameToDrugDifferences.isNotEmpty() || parameterDifferences.isNotEmpty() || otherDifferences.isNotEmpty()

        fun logSummary(indent: Int) {
            listOf(
                "name to drug" to nameToDrugDifferences,
                "parameter" to parameterDifferences,
                "other" to otherDifferences
            )
//                .filter { (_, differences) -> differences.isNotEmpty() }
                .forEach { (name, differences) ->
                    differences.forEach { LOGGER.debug("${" ".repeat(indent)}* $name: $it") }
                }
//            FunctionDifferences::class.memberProperties.forEach { property ->
//                property.isAccessible = true
//                val differenceGroup = property.get(summary) as? List<*>
//                LOGGER.info("${differenceGroup?.size} ${property.name}:\n${differenceGroup?.joinToString("\n")}")
//            }
        }
    }
}

fun main(): Unit = exitProcess(MatchOutputComparisonApplication().run())
