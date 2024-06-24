package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.trial.datamodel.EligibilityRule

class InfectionRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {
    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_ACTIVE_INFECTION to hasActiveInfectionCreator(),
            EligibilityRule.HAS_KNOWN_EBV_INFECTION to hasKnownEBVInfectionCreator(),
            EligibilityRule.HAS_KNOWN_HEPATITIS_A_INFECTION to hasSpecificInfectionCreator(DoidConstants.HEPATITIS_A_DOID),
            EligibilityRule.HAS_KNOWN_HEPATITIS_B_INFECTION to hasSpecificInfectionCreator(DoidConstants.HEPATITIS_B_DOID),
            EligibilityRule.HAS_KNOWN_HEPATITIS_C_INFECTION to hasSpecificInfectionCreator(DoidConstants.HEPATITIS_C_DOID),
            EligibilityRule.HAS_KNOWN_HIV_INFECTION to hasSpecificInfectionCreator(DoidConstants.HIV_DOID),
            EligibilityRule.HAS_KNOWN_CYTOMEGALOVIRUS_INFECTION to hasSpecificInfectionCreator(DoidConstants.CYTOMEGALOVIRUS_DOID),
            EligibilityRule.HAS_KNOWN_TUBERCULOSIS_INFECTION to hasSpecificInfectionCreator(DoidConstants.TUBERCULOSIS_DOID),
            EligibilityRule.MEETS_COVID_19_INFECTION_REQUIREMENTS to meetsCovid19InfectionRequirementsCreator(),
            EligibilityRule.HAS_RECEIVED_LIVE_VACCINE_WITHIN_X_MONTHS to hasReceivedLiveVaccineWithinMonthsCreator(),
            EligibilityRule.ADHERENCE_TO_PROTOCOL_REGARDING_ATTENUATED_VACCINE_USE to canAdhereToAttenuatedVaccineUseCreator(),
        )
    }

    private fun hasActiveInfectionCreator(): FunctionCreator {
        return FunctionCreator { HasActiveInfection(atcTree(), referenceDateProvider().date()) }
    }

    private fun hasKnownEBVInfectionCreator(): FunctionCreator {
        return FunctionCreator { HasKnownEBVInfection() }
    }

    private fun hasSpecificInfectionCreator(doidToFind: String): FunctionCreator {
        return FunctionCreator { HasSpecificInfection(doidModel(), doidToFind) }
    }

    private fun meetsCovid19InfectionRequirementsCreator(): FunctionCreator {
        return FunctionCreator { MeetsCovid19InfectionRequirements() }
    }

    private fun hasReceivedLiveVaccineWithinMonthsCreator(): FunctionCreator {
        return FunctionCreator { HasReceivedLiveVaccineWithinMonths() }
    }

    private fun hasReceivedNonLiveVaccineWithinWeeksCreator(): FunctionCreator {
        return FunctionCreator { HasReceivedNonLiveVaccineWithinWeeks() }
    }

    private fun canAdhereToAttenuatedVaccineUseCreator(): FunctionCreator {
        return FunctionCreator { CanAdhereToAttenuatedVaccineUse() }
    }
}