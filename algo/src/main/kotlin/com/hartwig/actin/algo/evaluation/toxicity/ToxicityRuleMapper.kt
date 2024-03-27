package com.hartwig.actin.algo.evaluation.toxicity

import com.google.common.collect.Sets
import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.trial.datamodel.EligibilityFunction
import com.hartwig.actin.trial.datamodel.EligibilityRule

class ToxicityRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_INTOLERANCE_TO_NAME_X to hasIntoleranceWithSpecificNameCreator(),
            EligibilityRule.HAS_INTOLERANCE_BELONGING_TO_DOID_TERM_X to hasIntoleranceWithSpecificDoidTermCreator(),
            EligibilityRule.HAS_INTOLERANCE_TO_PLATINUM_COMPOUNDS to hasIntoleranceToPlatinumCompoundsCreator(),
            EligibilityRule.HAS_INTOLERANCE_TO_TAXANE to hasIntoleranceToTaxaneCreator(),
            EligibilityRule.HAS_INTOLERANCE_RELATED_TO_STUDY_MEDICATION to hasIntoleranceRelatedToStudyMedicationCreator(),
            EligibilityRule.HAS_INTOLERANCE_FOR_PD_1_OR_PD_L1_INHIBITORS to hasIntoleranceToPD1OrPDL1InhibitorsCreator(),
            EligibilityRule.HAS_HISTORY_OF_ANAPHYLAXIS to hasHistoryAnaphylaxisCreator(),
            EligibilityRule.HAS_EXPERIENCED_IMMUNE_RELATED_ADVERSE_EVENTS to hasExperiencedImmuneRelatedAdverseEventsCreator(),
            EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X to hasToxicityWithGradeCreator(),
            EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IN_Y to hasToxicityWithGradeAndNameCreator(),
            EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IGNORING_Y to hasToxicityWithGradeIgnoringNamesCreator()
        )
    }

    private fun hasIntoleranceWithSpecificNameCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val termToFind = functionInputResolver().createOneStringInput(function)
            HasIntoleranceWithSpecificName(termToFind)
        }
    }

    private fun hasIntoleranceWithSpecificDoidTermCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val doidTermToFind = functionInputResolver().createOneDoidTermInput(function)
            HasIntoleranceWithSpecificDoid(doidModel(), doidModel().resolveDoidForTerm(doidTermToFind)!!)
        }
    }

    private fun hasIntoleranceToPlatinumCompoundsCreator(): FunctionCreator {
        return FunctionCreator { HasIntoleranceToPlatinumCompounds() }
    }

    private fun hasIntoleranceToTaxaneCreator(): FunctionCreator {
        return FunctionCreator { HasIntoleranceToTaxanes() }
    }

    private fun hasIntoleranceRelatedToStudyMedicationCreator(): FunctionCreator {
        return FunctionCreator { HasIntoleranceRelatedToStudyMedication() }
    }

    private fun hasIntoleranceToPD1OrPDL1InhibitorsCreator(): FunctionCreator {
        return FunctionCreator { HasIntoleranceForPD1OrPDL1Inhibitors(doidModel()) }
    }

    private fun hasHistoryAnaphylaxisCreator(): FunctionCreator {
        return FunctionCreator { HasHistoryOfAnaphylaxis() }
    }

    private fun hasExperiencedImmuneRelatedAdverseEventsCreator(): FunctionCreator {
        return FunctionCreator { HasExperiencedImmuneRelatedAdverseEvents() }
    }

    private fun hasToxicityWithGradeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minGrade = functionInputResolver().createOneIntegerInput(function)
            HasToxicityWithGrade(minGrade, null, Sets.newHashSet(), resources.algoConfiguration.warnIfToxicitiesNotFromQuestionnaire)
        }
    }

    private fun hasToxicityWithGradeAndNameCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneIntegerOneStringInput(function)
            HasToxicityWithGrade(input.integer, input.string, emptySet(), resources.algoConfiguration.warnIfToxicitiesNotFromQuestionnaire)
        }
    }

    private fun hasToxicityWithGradeIgnoringNamesCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val input = functionInputResolver().createOneIntegerManyStringsInput(function)
            HasToxicityWithGrade(
                input.integer,
                null,
                input.strings.toSet(),
                resources.algoConfiguration.warnIfToxicitiesNotFromQuestionnaire
            )
        }
    }
}