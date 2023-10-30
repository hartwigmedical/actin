package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.treatment.datamodel.EligibilityFunction
import com.hartwig.actin.treatment.datamodel.EligibilityRule

class GeneralRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.IS_AT_LEAST_X_YEARS_OLD to hasAtLeastCertainAgeCreator(),
            EligibilityRule.IS_MALE to isMaleCreator,
            EligibilityRule.IS_FEMALE to isFemaleCreator,
            EligibilityRule.HAS_WHO_STATUS_OF_AT_MOST_X to hasMaximumWHOStatusCreator(),
            EligibilityRule.HAS_WHO_STATUS_OF_AT_EXACTLY_X to hasWHOStatusCreator(),
            EligibilityRule.HAS_KARNOFSKY_SCORE_OF_AT_LEAST_X to hasMinimumKarnofskyScoreCreator(),
            EligibilityRule.HAS_LANSKY_SCORE_OF_AT_LEAST_X to hasMinimumLanskyScoreCreator(),
            EligibilityRule.CAN_GIVE_ADEQUATE_INFORMED_CONSENT to canGiveAdequateInformedConsentCreator(),
            EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_WEEKS to hasSufficientLifeExpectancyCreator(),
            EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_MONTHS to hasSufficientLifeExpectancyCreator(),
            EligibilityRule.IS_TREATED_IN_HOSPITAL_X to isTreatedInHospitalCreator,
            EligibilityRule.WILL_PARTICIPATE_IN_TRIAL_IN_COUNTRY_X to willParticipateInTrialInCountryCreator(),
            EligibilityRule.IS_LEGALLY_INSTITUTIONALIZED to isLegallyInstitutionalizedCreator,
            EligibilityRule.IS_INVOLVED_IN_STUDY_PROCEDURES to isInvolvedInStudyProceduresCreator,
        )
    }

    private fun hasAtLeastCertainAgeCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minAge: Int = functionInputResolver().createOneIntegerInput(function)
            HasAtLeastCertainAge(referenceDateProvider().year(), minAge)
        }
    }

    private val isMaleCreator: FunctionCreator
        get() = FunctionCreator { IsMale() }
    private val isFemaleCreator: FunctionCreator
        get() = FunctionCreator { IsFemale() }

    private fun hasMaximumWHOStatusCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val maximumWHO = functionInputResolver().createOneIntegerInput(function)
            HasMaximumWHOStatus(maximumWHO)
        }
    }

    private fun hasWHOStatusCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction? ->
            val requiredWHO = functionInputResolver().createOneIntegerInput(function!!)
            HasWHOStatus(requiredWHO)
        }
    }

    private fun hasMinimumKarnofskyScoreCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minScore = functionInputResolver().createOneIntegerInput(function)
            HasMinimumLanskyKarnofskyScore(PerformanceScore.KARNOFSKY, minScore)
        }
    }

    private fun hasMinimumLanskyScoreCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val minScore = functionInputResolver().createOneIntegerInput(function)
            HasMinimumLanskyKarnofskyScore(PerformanceScore.LANSKY, minScore)
        }
    }

    private fun canGiveAdequateInformedConsentCreator(): FunctionCreator {
        return FunctionCreator { CanGiveAdequateInformedConsent() }
    }

    private val isInvolvedInStudyProceduresCreator: FunctionCreator
        get() = FunctionCreator { IsInvolvedInStudyProcedures() }

    private fun hasSufficientLifeExpectancyCreator(): FunctionCreator {
        return FunctionCreator { HasSufficientLifeExpectancy() }
    }

    private val isTreatedInHospitalCreator: FunctionCreator
        get() = FunctionCreator { IsTreatedInHospital() }

    private fun willParticipateInTrialInCountryCreator(): FunctionCreator {
        return FunctionCreator { function: EligibilityFunction ->
            val country = functionInputResolver().createOneStringInput(function)
            WillParticipateInTrialInCountry(country)
        }
    }

    private val isLegallyInstitutionalizedCreator: FunctionCreator
        get() = FunctionCreator { IsLegallyInstitutionalized() }
}