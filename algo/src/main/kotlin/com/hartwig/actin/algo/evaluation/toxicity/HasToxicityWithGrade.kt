package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Complication
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
        val (matchingToxicities, otherToxicities) = selectRelevantToxicities(record).partition { toxicity ->
            val grade = toxicity.grade ?: DEFAULT_QUESTIONNAIRE_GRADE.takeIf { toxicity.source == ToxicitySource.QUESTIONNAIRE }
            val gradeMatch = grade?.let { it >= minGrade } ?: false
            val matchesIcd = hasIcdMatch(toxicity, targetIcdTitles, icdModel)
            gradeMatch && matchesIcd
        }

        val unresolvableToxicities = if (minGrade <= DEFAULT_QUESTIONNAIRE_GRADE) emptyList() else {
            otherToxicities.filter {
                with(it) { grade == null && source == ToxicitySource.QUESTIONNAIRE }
            }
        }

        return when {
            matchingToxicities.isNotEmpty() &&
                    (matchingToxicities.any { it.source == ToxicitySource.QUESTIONNAIRE } || !warnIfToxicitiesNotFromQuestionnaire) -> {
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

    private fun hasIcdMatch(toxicity: Toxicity, targetIcdTitles: List<String>?, icdModel: IcdModel): Boolean {
        if (targetIcdTitles == null) return true
        val targetIcdCodes = targetIcdTitles.mapNotNull { icdModel.resolveCodeForTitle(it) }
        return targetIcdCodes.any { it in icdModel.returnCodeWithParents(toxicity.icdCode) }
    }

    private fun formatToxicities(toxicities: List<Toxicity>) = if (toxicities.isNotEmpty()) " (${toxicities.joinToString(", ")})" else ""
}