package com.hartwig.actin.algo.evaluation.general

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule

class GeneralRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.IS_AT_LEAST_X_YEARS_OLD to hasAtLeastCertainAgeCreator(),
            EligibilityRule.IS_MALE to { IsMale() },
            EligibilityRule.IS_FEMALE to { IsFemale() },
            EligibilityRule.HAS_WHO_STATUS_OF_AT_MOST_X to hasMaximumWHOStatusCreator(),
            EligibilityRule.HAS_WHO_STATUS_OF_AT_EXACTLY_X to hasWHOStatusCreator(),
            EligibilityRule.HAS_KARNOFSKY_SCORE_OF_AT_LEAST_X to hasMinimumKarnofskyScoreCreator(),
            EligibilityRule.HAS_LANSKY_SCORE_OF_AT_LEAST_X to hasMinimumLanskyScoreCreator(),
            EligibilityRule.CAN_GIVE_ADEQUATE_INFORMED_CONSENT to { CanGiveAdequateInformedConsent() },
            EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_WEEKS to { HasSufficientLifeExpectancy() },
            EligibilityRule.HAS_LIFE_EXPECTANCY_OF_AT_LEAST_X_MONTHS to { HasSufficientLifeExpectancy() },
            EligibilityRule.WILL_PARTICIPATE_IN_TRIAL_IN_COUNTRY_X to willParticipateInTrialInCountryCreator(),
            EligibilityRule.IS_LEGALLY_INSTITUTIONALIZED to { IsLegallyInstitutionalized() },
            EligibilityRule.IS_INVOLVED_IN_STUDY_PROCEDURES to { IsInvolvedInStudyProcedures() },
            EligibilityRule.USES_TOBACCO_PRODUCTS to { UsesTobaccoProducts() },
            EligibilityRule.ADHERES_TO_BLOOD_DONATION_PRESCRIPTIONS to { AdheresToBloodDonationPrescriptions() },
        )
    }

    private fun hasAtLeastCertainAgeCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minAge: Int = functionInputResolver().createOneIntegerInput(function)
            HasAtLeastCertainAge(referenceDateProvider().year(), minAge)
        }
    }

    private fun hasMaximumWHOStatusCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasMaximumWHOStatus(functionInputResolver().createOneIntegerInput(function))
        }
    }

    private fun hasWHOStatusCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasWHOStatus(functionInputResolver().createOneIntegerInput(function))
        }
    }

    private fun hasMinimumKarnofskyScoreCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasMinimumLanskyKarnofskyScore(PerformanceScore.KARNOFSKY, functionInputResolver().createOneIntegerInput(function))
        }
    }

    private fun hasMinimumLanskyScoreCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            HasMinimumLanskyKarnofskyScore(PerformanceScore.LANSKY, functionInputResolver().createOneIntegerInput(function))
        }
    }

    private fun willParticipateInTrialInCountryCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            WillParticipateInTrialInCountry(functionInputResolver().createOneStringInput(function))
        }
    }
}