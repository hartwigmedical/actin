package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.IcdModel
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
        val relevantToxicities = ToxicityFunctions.selectRelevantToxicities(record, icdModel, referenceDate, icdTitlesToIgnore)
        val icdMatches = targetIcdTitles?.mapNotNull(icdModel::resolveCodeForTitle)?.toSet()?.let { targetCodes ->
            icdModel.findInstancesMatchingAnyIcdCode(relevantToxicities, targetCodes).fullMatches.toSet()
        }

        val (matchingToxicities, otherToxicities) =
            relevantToxicities.partition { toxicity ->
                val grade = toxicity.grade ?: DEFAULT_QUESTIONNAIRE_GRADE.takeIf { toxicity.source == ToxicitySource.QUESTIONNAIRE }
                val gradeMatch = grade?.let { it >= minGrade } ?: false

                gradeMatch && (icdMatches == null || icdMatches.contains(toxicity))
            }

        val unresolvableToxicities = if (minGrade <= DEFAULT_QUESTIONNAIRE_GRADE) emptyList() else {
            otherToxicities.filter {
                with(it) { grade == null && source == ToxicitySource.QUESTIONNAIRE && icdMatches?.contains(this) != false }
            }
        }

        val icdTitleText = targetIcdTitles?.let { "in ${Format.concatLowercaseWithCommaAndOr(it)}" } ?: ""
        return when {
            matchingToxicities.isNotEmpty() &&
                    (matchingToxicities.any { it.source == ToxicitySource.QUESTIONNAIRE } || !warnIfToxicitiesNotFromQuestionnaire) -> {
                val toxicityString = formatToxicities(matchingToxicities)
                EvaluationFactory.recoverablePass("Has toxicities grade >= $minGrade$toxicityString")
            }

            matchingToxicities.isNotEmpty() -> {
                val toxicityString = formatToxicities(matchingToxicities)
                EvaluationFactory.warn("Has toxicities grade >= $minGrade$toxicityString - n.b. different EHR source than questionnaire")
            }

            unresolvableToxicities.isNotEmpty() -> {
                val toxicityString = formatToxicities(unresolvableToxicities)
                return EvaluationFactory.undetermined("Has toxicities grade >= $DEFAULT_QUESTIONNAIRE_GRADE$toxicityString but unknown if grade >= $minGrade")
            }

            else -> return EvaluationFactory.fail("No toxicities $icdTitleText found with grade $minGrade or higher")
        }
    }

    private fun formatToxicities(toxicities: List<Toxicity>) =
        if (toxicities.any { !it.name.isNullOrEmpty() }) " (${toxicities.joinToString(", ") { it.display() }})" else ""
}