package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule

class InfectionRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_ACTIVE_INFECTION to hasActiveInfectionCreator(),
            EligibilityRule.HAS_KNOWN_EBV_INFECTION to hasSpecificInfectionCreator(setOf(IcdCode(IcdConstants.EPSTEIN_BARR_MONONUCLEOSIS_CODE)), "Epstein-Barr virus"),
            EligibilityRule.HAS_KNOWN_HEPATITIS_A_INFECTION to hasSpecificInfectionCreator(setOf(IcdCode(IcdConstants.ACUTE_HEPATITIS_A_CODE)), "hepatitis A virus"),
            EligibilityRule.HAS_KNOWN_HEPATITIS_B_INFECTION to hasSpecificInfectionCreator(setOf(IcdCode(IcdConstants.ACUTE_HEPATITIS_B_CODE), IcdCode(IcdConstants.CHRONIC_HEPATITIS_B_CODE)), "hepatitis B virus"),
            EligibilityRule.HAS_KNOWN_HEPATITIS_C_INFECTION to hasSpecificInfectionCreator(setOf(IcdCode(IcdConstants.ACUTE_HEPATITIS_C_CODE), IcdCode(IcdConstants.CHRONIC_HEPATITIS_C_CODE)), "hepatitis C virus"),
            EligibilityRule.HAS_KNOWN_HIV_INFECTION to hasSpecificInfectionCreator(setOf(IcdCode(IcdConstants.HIV_BLOCK)), "HIV"),
            EligibilityRule.HAS_KNOWN_HSV_INFECTION to hasSpecificInfectionCreator(setOf(IcdCode(IcdConstants.HSV_CODE), IcdCode(IcdConstants.ANOGENITAL_HERPES_SIMPLEX_INFECTION_CODE)), "HSV"),
            EligibilityRule.HAS_KNOWN_CYTOMEGALOVIRUS_INFECTION to hasSpecificInfectionCreator(setOf(IcdCode(IcdConstants.CYTOMEGALOVIRAL_DISEASE_CODE)), "cytomegalovirus"),
            EligibilityRule.HAS_KNOWN_TUBERCULOSIS_INFECTION to hasSpecificInfectionCreator(setOf(IcdCode(IcdConstants.TUBERCULOSIS_BLOCK)), "tuberculosis"),
            EligibilityRule.MEETS_COVID_19_INFECTION_REQUIREMENTS to meetsCovid19InfectionRequirementsCreator(),
            EligibilityRule.HAS_RECEIVED_LIVE_VACCINE_WITHIN_X_MONTHS to hasReceivedLiveVaccineWithinMonthsCreator(),
            EligibilityRule.HAS_RECEIVED_NON_LIVE_VACCINE_WITHIN_X_WEEKS to hasReceivedNonLiveVaccineWithinWeeksCreator(),
            EligibilityRule.ADHERENCE_TO_PROTOCOL_REGARDING_ATTENUATED_VACCINE_USE to canAdhereToAttenuatedVaccineUseCreator(),
        )
    }

    private fun hasActiveInfectionCreator(): FunctionCreator {
        return { HasActiveInfection(atcTree(), referenceDateProvider().date()) }
    }

    private fun hasSpecificInfectionCreator(icdCodes: Set<IcdCode>, term: String): FunctionCreator {
        return { HasSpecificInfection(icdModel(), icdCodes, term) }
    }

    private fun meetsCovid19InfectionRequirementsCreator(): FunctionCreator {
        return { MeetsCovid19InfectionRequirements() }
    }

    private fun hasReceivedLiveVaccineWithinMonthsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minMonths = functionInputResolver().createOneIntegerInput(function)
            HasReceivedLiveVaccineWithinMonths(minMonths)
        }
    }

    private fun hasReceivedNonLiveVaccineWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minWeeks = functionInputResolver().createOneIntegerInput(function)
            HasReceivedNonLiveVaccineWithinWeeks(minWeeks)
        }
    }

    private fun canAdhereToAttenuatedVaccineUseCreator(): FunctionCreator {
        return { CanAdhereToAttenuatedVaccineUse() }
    }
}