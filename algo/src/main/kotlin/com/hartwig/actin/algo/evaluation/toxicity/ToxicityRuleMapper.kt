package com.hartwig.actin.algo.evaluation.toxicity

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.IcdTitleParameter
import com.hartwig.actin.datamodel.trial.IntegerParameter
import com.hartwig.actin.datamodel.trial.ManyIcdTitlesParameter
import com.hartwig.actin.datamodel.trial.Parameter
import com.hartwig.actin.datamodel.trial.StringParameter
import com.hartwig.actin.trial.input.EligibilityRule

class ToxicityRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_INTOLERANCE_TO_NAME_X to hasIntoleranceWithSpecificNameCreator(),
            EligibilityRule.HAS_INTOLERANCE_WITH_ICD_TITLE_X to hasIntoleranceWithSpecificIcdTitleCreator(),
            EligibilityRule.HAS_INTOLERANCE_TO_PLATINUM_COMPOUNDS to {
                HasDrugIntoleranceWithAnyIcdCodeOrName(
                    icdModel(),
                    IcdConstants.PLATINUM_COMPOUND_CODE,
                    platinumCompoundsSet,
                    "platinum compounds"
                )
            },
            EligibilityRule.HAS_INTOLERANCE_TO_TAXANE to
                    { HasDrugIntoleranceWithAnyIcdCodeOrName(icdModel(), IcdConstants.TAXANE_CODE, taxaneSet, "taxanes") },
            EligibilityRule.HAS_INTOLERANCE_RELATED_TO_STUDY_MEDICATION to hasIntoleranceRelatedToStudyMedicationCreator(),
            EligibilityRule.HAS_INTOLERANCE_FOR_PD_1_OR_PD_L1_INHIBITORS to hasIntoleranceToPD1OrPDL1InhibitorsCreator(),
            EligibilityRule.HAS_HISTORY_OF_ANAPHYLAXIS to hasHistoryAnaphylaxisCreator(),
            EligibilityRule.HAS_EXPERIENCED_IMMUNOTHERAPY_RELATED_ADVERSE_EVENTS to hasExperiencedImmunotherapyRelatedAdverseEventsCreator(),
            EligibilityRule.HAS_TOXICITY_CTCAE_OF_AT_LEAST_GRADE_X to hasToxicityWithGradeCreator(),
            EligibilityRule.HAS_TOXICITY_CTCAE_OF_AT_LEAST_GRADE_X_WITH_ANY_ICD_TITLE_Y to hasToxicityWithGradeAndNameCreator(),
            EligibilityRule.HAS_TOXICITY_ASTCT_OF_AT_LEAST_GRADE_X_WITH_ANY_ICD_TITLE_Y to hasToxicityWithGradeAndNameCreator(),
            EligibilityRule.HAS_TOXICITY_CTCAE_OF_AT_LEAST_GRADE_X_IGNORING_ICD_TITLES_Y to hasToxicityWithGradeIgnoringNamesCreator(),
            EligibilityRule.HAD_TOXICITY_X_OF_AT_LEAST_GRADE_Y_DURING_PREVIOUS_TREATMENT to hadToxicityWithGradeDuringPreviousTreatmentCreator()
        )
    }

    private fun hasIntoleranceWithSpecificNameCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val termToFind = function.param<StringParameter>(0).value
            HasIntoleranceWithSpecificName(termToFind)
        }
    }

    private fun hasIntoleranceWithSpecificIcdTitleCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val icdTitle = function.param<IcdTitleParameter>(0).value
            HasIntoleranceWithSpecificIcdTitle(icdModel(), icdTitle)
        }
    }

    private fun hasIntoleranceRelatedToStudyMedicationCreator(): FunctionCreator {
        return { HasIntoleranceRelatedToStudyMedication(icdModel()) }
    }

    private fun hasIntoleranceToPD1OrPDL1InhibitorsCreator(): FunctionCreator {
        return { HasIntoleranceForPD1OrPDL1Inhibitors(icdModel()) }
    }

    private fun hasHistoryAnaphylaxisCreator(): FunctionCreator {
        return { HasHistoryOfAnaphylaxis(icdModel()) }
    }

    private fun hasExperiencedImmunotherapyRelatedAdverseEventsCreator(): FunctionCreator {
        return { HasExperiencedImmunotherapyRelatedAdverseEvents(icdModel()) }
    }

    private fun hasToxicityWithGradeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minGrade = function.param<IntegerParameter>(0).value
            createHasToxicityWithGrade(minGrade)
        }
    }

    private fun hasToxicityWithGradeAndNameCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.INTEGER, Parameter.Type.MANY_ICD_TITLES)
            val minGrade = function.param<IntegerParameter>(0).value
            val icdTitles = function.param<ManyIcdTitlesParameter>(1).value
            createHasToxicityWithGrade(minGrade, icdTitles)
        }
    }

    private fun hasToxicityWithGradeIgnoringNamesCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.INTEGER, Parameter.Type.MANY_ICD_TITLES)
            val minGrade = function.param<IntegerParameter>(0).value
            val toxicitiesToIgnore = function.param<ManyIcdTitlesParameter>(1).value
            createHasToxicityWithGrade(minGrade, null, toxicitiesToIgnore)
        }
    }

    private fun hadToxicityWithGradeDuringPreviousTreatmentCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            function.expectTypes(Parameter.Type.STRING, Parameter.Type.INTEGER)
            val name = function.param<StringParameter>(0).value
            val grade = function.param<IntegerParameter>(1).value
            HadToxicityWithGradeDuringPreviousTreatment(name, grade)
        }
    }

    private fun createHasToxicityWithGrade(
        minGrade: Int, targetIcdTitles: List<String>? = null, icdTitlesToIgnore: List<String> = emptyList()
    ) = HasToxicityWithGrade(
        icdModel(),
        minGrade,
        targetIcdTitles,
        icdTitlesToIgnore,
        resources.algoConfiguration.warnIfToxicitiesNotFromQuestionnaire,
        referenceDateProvider().date()
    )

    private val platinumCompoundsSet =
        setOf(
            "satraplatin",
            "eloxatin",
            "paraplatin",
            "platinol",
            "imifolatin",
            "nedaplatin",
            "NC-6004"
        )

    private val taxaneSet = setOf("nab-paclitaxel", "Abraxane", "Jevtana", "Tesetaxel")
}
