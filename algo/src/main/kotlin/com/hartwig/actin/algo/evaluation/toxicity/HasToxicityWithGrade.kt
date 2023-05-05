package com.hartwig.actin.algo.evaluation.toxicity

import com.google.common.collect.Sets
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import com.hartwig.actin.util.ApplicationConfig

//TODO: In case X => 2, ignore EHR toxicities in evaluation
class HasToxicityWithGrade internal constructor(
    private val minGrade: Int,
    private val nameFilter: String?,
    private val ignoreFilters: Set<String>
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        var hasUnresolvableQuestionnaireToxicities = false
        var hasAtLeastOneMatchingQuestionnaireToxicity = false
        val unresolvableToxicities: MutableSet<String> = Sets.newHashSet()
        val toxicities: MutableSet<String> = Sets.newHashSet()
        for (toxicity in selectRelevantToxicities(record.clinical())) {
            var grade = toxicity.grade()
            if (grade == null && toxicity.source() == ToxicitySource.QUESTIONNAIRE) {
                if (minGrade > DEFAULT_QUESTIONNAIRE_GRADE) {
                    hasUnresolvableQuestionnaireToxicities = true
                    unresolvableToxicities.add(toxicity.name())
                }
                grade = DEFAULT_QUESTIONNAIRE_GRADE
            }
            val gradeMatch = grade != null && grade >= minGrade
            val nameMatch =
                nameFilter == null || toxicity.name().lowercase(ApplicationConfig.LOCALE)
                    .contains(nameFilter.lowercase(ApplicationConfig.LOCALE))
            if (gradeMatch && nameMatch) {
                if (toxicity.source() == ToxicitySource.QUESTIONNAIRE) {
                    hasAtLeastOneMatchingQuestionnaireToxicity = true
                }
                toxicities.add(toxicity.name())
            }
        }
        if (!toxicities.isEmpty()) {
            return if (hasAtLeastOneMatchingQuestionnaireToxicity) {
                recoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages("Toxicities with grade >= " + minGrade + " found: " + concat(toxicities))
                    .addPassGeneralMessages("Toxicities grade >= " + minGrade + " found: " + concat(toxicities))
                    .build()
            } else {
                recoverable()
                    .result(EvaluationResult.WARN)
                    .addWarnSpecificMessages(
                        "Toxicities with grade >= " + minGrade + " found: " + concat(toxicities)
                                + " but source is not questionnaire"
                    )
                    .addWarnGeneralMessages(
                        "Toxicities grade >= " + minGrade + " found: " + concat(toxicities)
                                + " but source is not questionnaire"
                    )
                    .build()
            }
        } else if (hasUnresolvableQuestionnaireToxicities) {
            return unrecoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages(
                    "The exact grade (2, 3 or 4) is not known for toxicities: " + concat(unresolvableToxicities)
                )
                .addUndeterminedGeneralMessages(concat(unresolvableToxicities) + " present, but grade 2/3/4 unknown")
                .build()
        }
        return unrecoverable()
            .result(EvaluationResult.FAIL)
            .addFailSpecificMessages("No toxicities found with grade $minGrade or higher")
            .addFailGeneralMessages("Grade >=$minGrade toxicities not present")
            .build()
    }

    private fun selectRelevantToxicities(clinical: ClinicalRecord): List<Toxicity> {
        val withoutOutdatedEHRToxicities = dropOutdatedEHRToxicities(clinical.toxicities())
        val withoutEHRToxicitiesThatAreComplications =
            dropEHRToxicitiesThatAreComplications(withoutOutdatedEHRToxicities, clinical.complications())
        return applyIgnoreFilters(withoutEHRToxicitiesThatAreComplications, ignoreFilters)
    }

    companion object {
        val DEFAULT_QUESTIONNAIRE_GRADE = 2

        private fun dropOutdatedEHRToxicities(toxicities: List<Toxicity>): List<Toxicity> {
            val filtered: MutableList<Toxicity> = mutableListOf()
            val mostRecentToxicityByName: MutableMap<String, Toxicity> = mutableMapOf()
            for (toxicity in toxicities) {
                if (toxicity.source() == ToxicitySource.EHR) {
                    val current = mostRecentToxicityByName[toxicity.name()]
                    if (current == null || current.evaluatedDate().isBefore(toxicity.evaluatedDate())) {
                        mostRecentToxicityByName[toxicity.name()] = toxicity
                    }
                } else {
                    filtered.add(toxicity)
                }
            }
            filtered.addAll(mostRecentToxicityByName.values)
            return filtered
        }

        private fun dropEHRToxicitiesThatAreComplications(toxicities: List<Toxicity>, complications: List<Complication>?): List<Toxicity> {
            return toxicities.filter { it.source() != ToxicitySource.EHR || !hasComplicationWithName(complications, it.name()) }
        }

        private fun hasComplicationWithName(complications: List<Complication>?, nameToFind: String): Boolean {
            return complications?.any { it.name() == nameToFind } ?: false
        }

        private fun applyIgnoreFilters(toxicities: List<Toxicity>, ignoreFilters: Set<String>): List<Toxicity> {
            return toxicities.filterNot { stringCaseInsensitivelyMatchesQueryCollection(it.name(), ignoreFilters) }
        }
    }
}