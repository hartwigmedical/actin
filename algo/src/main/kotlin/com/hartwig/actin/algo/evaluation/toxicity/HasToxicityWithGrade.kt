package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.datamodel.IcdNode
import java.time.LocalDate

const val DEFAULT_QUESTIONNAIRE_GRADE = 2

class HasToxicityWithGrade(
    private val icdModel: IcdModel,
    private val minGrade: Int,
    private val targetIcdTitles: List<String>?,
    private val icdTitlesToIgnore: List<String>,
    private val warnIfToxicitiesNotFromQuestionnaire: Boolean,
    private val referenceDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (matchingToxicities, unresolvableToxicities, hasAtLeastOneMatchingQuestionnaireToxicity) =
            selectRelevantToxicities(record).map { toxicity ->
                val (grade, unresolvable) =
                    if (toxicity.grade == null && toxicity.source == ToxicitySource.QUESTIONNAIRE) {
                        DEFAULT_QUESTIONNAIRE_GRADE to (minGrade > DEFAULT_QUESTIONNAIRE_GRADE)
                    } else toxicity.grade to false
                val gradeMatch = grade != null && grade >= minGrade

                val (icdMatches, isParentMatch) = resolveIcdMatches(toxicity, targetIcdTitles, icdModel)
                val hasIcdMatch = targetIcdTitles == null || icdMatches.isNotEmpty()

                val (matchingToxicity, isMatchingQuestionnaireToxicity) = if (gradeMatch && hasIcdMatch) {
                    setOf(Triple(toxicity.name,icdMatches, isParentMatch)) to (toxicity.source == ToxicitySource.QUESTIONNAIRE)
                } else emptySet<MatchingToxicity>() to false

                Triple(
                    matchingToxicity,
                    if (unresolvable) setOf(Triple(toxicity.name, icdMatches, isParentMatch)) else emptySet(),
                    isMatchingQuestionnaireToxicity
                )
            }
                .fold(Triple(emptySet<MatchingToxicity>(), emptySet<MatchingToxicity>(), false)) { acc, triple ->
                    Triple(acc.first + triple.first, acc.second + triple.second, acc.third || triple.third)
                }

        return when {
            matchingToxicities.isNotEmpty() && (hasAtLeastOneMatchingQuestionnaireToxicity || !warnIfToxicitiesNotFromQuestionnaire) -> {
                val toxicityString = formatToxicities(matchingToxicities)
                EvaluationFactory.recoverablePass("Has toxicities grade >= $minGrade$toxicityString")
            }

            matchingToxicities.isNotEmpty() -> {
                val toxicityString = formatToxicities(matchingToxicities)
                EvaluationFactory.recoverableWarn(
                    "Patient has toxicities grade >= $minGrade$toxicityString - n.b. different EHR source than questionnaire",
                    "Has toxicities grade >= $minGrade$toxicityString - n.b. different EHR source than questionnaire"
                )
            }

            unresolvableToxicities.isNotEmpty() -> {
                val toxicityString = formatToxicities(unresolvableToxicities)
                return EvaluationFactory.undetermined(
                    "Patient has toxicities grade >= $DEFAULT_QUESTIONNAIRE_GRADE$toxicityString but unknown if grade >= $minGrade",
                    "Has toxicities grade >= $DEFAULT_QUESTIONNAIRE_GRADE$toxicityString but unknown if grade >= $minGrade"
                )
            }

            else -> return EvaluationFactory.fail(
                "No toxicities found with grade $minGrade or higher", "Grade >=$minGrade toxicities not present"
            )
        }
    }

    private fun selectRelevantToxicities(record: PatientRecord): List<Toxicity> {
        val complicationIcdCodes = record.complications?.map(Complication::icdCode)?.toSet() ?: emptySet()
        val ignoredIcdCodes = icdTitlesToIgnore.mapNotNull(icdModel::resolveCodeForTitle).toSet()

        return dropOutdatedEHRToxicities(record.toxicities)
            .filter { it.endDate?.isAfter(referenceDate) != false }
            .filter { it.source != ToxicitySource.EHR || it.icdCode !in complicationIcdCodes }
            .filterNot { it.icdCode in ignoredIcdCodes }
    }

    private fun dropOutdatedEHRToxicities(toxicities: List<Toxicity>): List<Toxicity> {
        val (ehrToxicities, otherToxicities) = toxicities.partition { it.source == ToxicitySource.EHR }
        val mostRecentEhrToxicitiesByCode = ehrToxicities.groupBy(Toxicity::icdCode)
            .map { (_, toxGroup) -> toxGroup.maxBy(Toxicity::evaluatedDate) }

        return otherToxicities + mostRecentEhrToxicitiesByCode
    }

    private fun resolveIcdMatches(toxicity: Toxicity, targetIcdTitles: List<String>?, icdModel: IcdModel): Pair<List<IcdNode>, Boolean> {
        if (targetIcdTitles == null) return emptyList<IcdNode>() to false

        val targetIcdCodes = targetIcdTitles.mapNotNull { icdModel.resolveCodeForTitle(it) }
        val (toxicityIcdNode, toxicityParentNodes) = with(icdModel) {
            codeToNode(toxicity.icdCode) to codeToParentNodes(toxicity.icdCode)
        }

        return when {
            targetIcdCodes.contains(toxicityIcdNode?.code) -> listOfNotNull(toxicityIcdNode) to false
            else -> toxicityParentNodes.filter { targetIcdCodes.contains(it.code) } to true
        }
    }

    private fun formatToxicities(toxicities: Set<MatchingToxicity>): String {
        val formattedToxicities = toxicities.joinToString(", ") { (name, nodes, isParentMatch) ->
            val indicativeNodes = nodes.joinToString(", ") { it.title }
            if (indicativeNodes.isEmpty() || !isParentMatch) name else "$name - indicative of $indicativeNodes"
        }
        return if (formattedToxicities.isNotEmpty()) " ($formattedToxicities)" else ""
    }
}

private typealias MatchingToxicity = Triple<String, List<IcdNode>, Boolean>