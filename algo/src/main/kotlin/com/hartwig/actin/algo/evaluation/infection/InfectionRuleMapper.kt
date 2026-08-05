package com.hartwig.actin.algo.evaluation.infection

import com.hartwig.actin.algo.evaluation.FunctionCreator
import com.hartwig.actin.algo.evaluation.RuleMapper
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.IntegerParameter
import com.hartwig.actin.trial.input.EligibilityRule

class InfectionRuleMapper(resources: RuleMappingResources) : RuleMapper(resources) {

    override fun createMappings(): Map<EligibilityRule, FunctionCreator> {
        return mapOf(
            EligibilityRule.HAS_ACTIVE_INFECTION to hasActiveInfectionCreator(),
            EligibilityRule.HAS_KNOWN_EBV_INFECTION to hasSpecificInfectionCreator(setOf(IcdCode(IcdConstants.EPSTEIN_BARR_MONONUCLEOSIS_CODE)), evaluationLabels.infection.descriptionEpsteinBarrVirus()),
            EligibilityRule.HAS_KNOWN_HEPATITIS_A_INFECTION to hasSpecificInfectionCreator(setOf(IcdCode(IcdConstants.ACUTE_HEPATITIS_A_CODE)), evaluationLabels.infection.descriptionHepatitisAVirus()),
            EligibilityRule.HAS_KNOWN_HEPATITIS_B_INFECTION to hasSpecificInfectionCreator(setOf(IcdCode(IcdConstants.ACUTE_HEPATITIS_B_CODE), IcdCode(IcdConstants.CHRONIC_HEPATITIS_B_CODE)), evaluationLabels.infection.descriptionHepatitisBVirus()),
            EligibilityRule.HAS_KNOWN_HEPATITIS_C_INFECTION to hasSpecificInfectionCreator(setOf(IcdCode(IcdConstants.ACUTE_HEPATITIS_C_CODE), IcdCode(IcdConstants.CHRONIC_HEPATITIS_C_CODE)), evaluationLabels.infection.descriptionHepatitisCVirus()),
            EligibilityRule.HAS_KNOWN_HIV_INFECTION to hasSpecificInfectionCreator(setOf(IcdCode(IcdConstants.HIV_BLOCK)), evaluationLabels.infection.descriptionHiv()),
            EligibilityRule.HAS_KNOWN_HSV_INFECTION to hasSpecificInfectionCreator(setOf(IcdCode(IcdConstants.HSV_CODE), IcdCode(IcdConstants.ANOGENITAL_HERPES_SIMPLEX_INFECTION_CODE)), evaluationLabels.infection.descriptionHsv()),
            EligibilityRule.HAS_KNOWN_CYTOMEGALOVIRUS_INFECTION to hasSpecificInfectionCreator(setOf(IcdCode(IcdConstants.CYTOMEGALOVIRAL_DISEASE_CODE)), evaluationLabels.infection.descriptionCytomegalovirus()),
            EligibilityRule.HAS_KNOWN_TUBERCULOSIS_INFECTION to hasSpecificInfectionCreator(setOf(IcdCode(IcdConstants.TUBERCULOSIS_BLOCK)), evaluationLabels.infection.descriptionTuberculosis()),
            EligibilityRule.MEETS_COVID_19_INFECTION_REQUIREMENTS to meetsCovid19InfectionRequirementsCreator(),
            EligibilityRule.HAS_RECEIVED_LIVE_VACCINE_WITHIN_X_MONTHS to hasReceivedLiveVaccineWithinMonthsCreator(),
            EligibilityRule.HAS_RECEIVED_NON_LIVE_VACCINE_WITHIN_X_WEEKS to hasReceivedNonLiveVaccineWithinWeeksCreator(),
            EligibilityRule.ADHERENCE_TO_PROTOCOL_REGARDING_ATTENUATED_VACCINE_USE to canAdhereToAttenuatedVaccineUseCreator(),
        )
    }

    private fun hasActiveInfectionCreator(): FunctionCreator {
        return { HasActiveInfection(atcTree, referenceDateProvider.date(), icdModel, evaluationLabels.infection) }
    }

    private fun hasSpecificInfectionCreator(icdCodes: Set<IcdCode>, term: String): FunctionCreator {
        return { HasSpecificInfection(icdModel, icdCodes, term, evaluationLabels.infection) }
    }

    private fun meetsCovid19InfectionRequirementsCreator(): FunctionCreator {
        return { MeetsCovid19InfectionRequirements(evaluationLabels.infection) }
    }

    private fun hasReceivedLiveVaccineWithinMonthsCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minMonths = function.param<IntegerParameter>(0).value
            HasReceivedLiveVaccineWithinMonths(minMonths, evaluationLabels.infection)
        }
    }

    private fun hasReceivedNonLiveVaccineWithinWeeksCreator(): FunctionCreator {
        return { function: EligibilityFunction ->
            val minWeeks = function.param<IntegerParameter>(0).value
            HasReceivedNonLiveVaccineWithinWeeks(minWeeks, evaluationLabels.infection)
        }
    }

    private fun canAdhereToAttenuatedVaccineUseCreator(): FunctionCreator {
        return { CanAdhereToAttenuatedVaccineUse(evaluationLabels.infection) }
    }
}
