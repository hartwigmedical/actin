package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import java.time.LocalDate

const val DEFAULT_QUESTIONNAIRE_GRADE = 2

//TODO: In case X => 2, ignore EHR toxicities in evaluation
class HasToxicityWithGrade internal constructor(
    private val minGrade: Int,
    private val nameFilter: String?,
    private val ignoreFilters: Set<String>,
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
                val nameMatch = nameFilter == null || toxicity.name.lowercase().contains(nameFilter.lowercase())

                val (matchingToxicity, isMatchingQuestionnaireToxicity) = if (gradeMatch && nameMatch) {
                    setOf(toxicity.name) to (toxicity.source == ToxicitySource.QUESTIONNAIRE)
                } else emptySet<String>() to false

                Triple(
                    matchingToxicity,
                    if (unresolvable) setOf(toxicity.name) else emptySet(),
                    isMatchingQuestionnaireToxicity
                )
            }
                .fold(Triple(emptySet<String>(), emptySet<String>(), false)) { acc, triple ->
                    Triple(acc.first + triple.first, acc.second + triple.second, acc.third || triple.third)
                }

        when {
            matchingToxicities.isNotEmpty() -> {
                val toxicityString = formatToxicities(matchingToxicities)
                return if (hasAtLeastOneMatchingQuestionnaireToxicity || !warnIfToxicitiesNotFromQuestionnaire) {
                    EvaluationFactory.recoverablePass("Has $nameFilter toxicity grade >= $minGrade$toxicityString")
                } else {
                    EvaluationFactory.recoverableWarn("Has $nameFilter toxicity grade >= $minGrade$toxicityString - n.b. different EHR source than questionnaire")
                }
            }

            unresolvableToxicities.isNotEmpty() -> {
                val toxicityString = formatToxicities(unresolvableToxicities)
                return EvaluationFactory.undetermined("Has $nameFilter toxicity grade >= $DEFAULT_QUESTIONNAIRE_GRADE$toxicityString but unknown if grade >= $minGrade")
            }

            else -> return EvaluationFactory.fail("No $nameFilter toxicity found with grade $minGrade or higher")
        }
    }

    private fun selectRelevantToxicities(record: PatientRecord): List<Toxicity> {
        val complicationNames = record.complications?.map(Complication::name)?.toSet() ?: emptySet()
        return dropOutdatedEHRToxicities(record.toxicities)
            .filter { it.endDate?.let { endDate -> endDate >= referenceDate } ?: true }
            .filter { it.source != ToxicitySource.EHR || it.name !in complicationNames }
            .filterNot { stringCaseInsensitivelyMatchesQueryCollection(it.name, ignoreFilters) }
    }

    private fun dropOutdatedEHRToxicities(toxicities: List<Toxicity>): List<Toxicity> {
        val (ehrToxicities, otherToxicities) = toxicities.partition { it.source == ToxicitySource.EHR }
        val mostRecentEhrToxicitiesByName = ehrToxicities.groupBy(Toxicity::name)
            .map { (_, toxGroup) -> toxGroup.maxBy(Toxicity::evaluatedDate) }

        return otherToxicities + mostRecentEhrToxicitiesByName
    }

    private fun formatToxicities(toxicityNames: Iterable<String>): String {
        val toxicityListing = Format.concatLowercaseWithCommaAndAnd(toxicityNames.filter(String::isNotEmpty))
        return if (toxicityListing.isNotEmpty()) " ($toxicityListing)" else ""
    }
}