package com.hartwig.actin.algo.evaluation.toxicity

import com.google.common.collect.Sets
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
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
    private val minGrade: Int, private val nameFilter: String?, private val ignoreFilters: Set<String>
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
            val nameMatch = nameFilter == null || toxicity.name().lowercase(ApplicationConfig.LOCALE)
                .contains(nameFilter.lowercase(ApplicationConfig.LOCALE))
            if (gradeMatch && nameMatch) {
                if (toxicity.source() == ToxicitySource.QUESTIONNAIRE) {
                    hasAtLeastOneMatchingQuestionnaireToxicity = true
                }
                toxicities.add(toxicity.name())
            }
        }
        if (toxicities.isNotEmpty()) {
            val toxicityString = formatToxicities(toxicities)
            return if (hasAtLeastOneMatchingQuestionnaireToxicity) {
                EvaluationFactory.recoverablePass(
                    "Toxicities with grade >= $minGrade$toxicityString",
                    "Toxicities grade >= $minGrade$toxicityString"
                )
            } else {
                EvaluationFactory.recoverableWarn(
                    "Toxicities with grade >= $minGrade$toxicityString but source is not questionnaire",
                    "Toxicities grade >= $minGrade$toxicityString but source is not questionnaire"
                )
            }
        } else if (hasUnresolvableQuestionnaireToxicities) {
            val toxicityString = formatToxicities(unresolvableToxicities)
            return EvaluationFactory.undetermined(
                "Unknown grade (2, 3 or 4) for toxicities$toxicityString",
                "Toxicities with unknown grade$toxicityString"
            )
        }
        return EvaluationFactory.fail(
            "No toxicities found with grade $minGrade or higher", "Grade >=$minGrade toxicities not present"
        )
    }

    private fun selectRelevantToxicities(clinical: ClinicalRecord): List<Toxicity> {
        val withoutOutdatedEHRToxicities = dropOutdatedEHRToxicities(clinical.toxicities())
        val withoutEHRToxicitiesThatAreComplications =
            dropEHRToxicitiesThatAreComplications(withoutOutdatedEHRToxicities, clinical.complications())
        return applyIgnoreFilters(withoutEHRToxicitiesThatAreComplications, ignoreFilters)
    }

    companion object {
        const val DEFAULT_QUESTIONNAIRE_GRADE = 2

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

        private fun formatToxicities(toxicityNames: Iterable<String>): String {
            val toxicityListing = concat(toxicityNames.filter { it.isNotEmpty() })
            return if (toxicityListing.isNotEmpty()) " ($toxicityListing)" else ""
        }
    }
}