package com.hartwig.actin.algo.evaluation.infection;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public class InfectionRuleMapper extends RuleMapper {

    public InfectionRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);
    }

    @NotNull
    @Override
    public Map<EligibilityRule, FunctionCreator> createMappings() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_ACTIVE_INFECTION, hasActiveInfectionCreator());
        map.put(EligibilityRule.HAS_KNOWN_EBV_INFECTION, hasKnownEBVInfectionCreator());
        map.put(EligibilityRule.HAS_KNOWN_HEPATITIS_A_INFECTION, hasSpecificInfectionCreator(DoidConstants.HEPATITIS_A_DOID));
        map.put(EligibilityRule.HAS_KNOWN_HEPATITIS_B_INFECTION, hasSpecificInfectionCreator(DoidConstants.HEPATITIS_B_DOID));
        map.put(EligibilityRule.HAS_KNOWN_HEPATITIS_C_INFECTION, hasSpecificInfectionCreator(DoidConstants.HEPATITIS_C_DOID));
        map.put(EligibilityRule.HAS_KNOWN_HIV_INFECTION, hasSpecificInfectionCreator(DoidConstants.HIV_DOID));
        map.put(EligibilityRule.HAS_KNOWN_CYTOMEGALOVIRUS_INFECTION, hasSpecificInfectionCreator(DoidConstants.CYTOMEGALOVIRUS_DOID));
        map.put(EligibilityRule.HAS_KNOWN_TUBERCULOSIS_INFECTION, hasSpecificInfectionCreator(DoidConstants.TUBERCULOSIS_DOID));
        map.put(EligibilityRule.MEETS_COVID_19_INFECTION_REQUIREMENTS, meetsCovid19InfectionRequirementsCreator());
        map.put(EligibilityRule.MEETS_COVID_19_VACCINATION_REQUIREMENTS, meetsCovid19VaccinationRequirementsCreator());
        map.put(EligibilityRule.IS_FULLY_VACCINATED_AGAINST_COVID_19, isFullyVaccinatedCovid19Creator());
        map.put(EligibilityRule.HAS_RECEIVED_LIVE_VACCINE_WITHIN_X_MONTHS, hasReceivedLiveVaccineWithinMonthsCreator());
        map.put(EligibilityRule.ADHERENCE_TO_PROTOCOL_REGARDING_ATTENUATED_VACCINE_USE, canAdhereToAttenuatedVaccineUseCreator());

        return map;
    }

    @NotNull
    private FunctionCreator hasActiveInfectionCreator() {
        return function -> new HasActiveInfection();
    }

    @NotNull
    private FunctionCreator hasKnownEBVInfectionCreator() {
        return function -> new HasKnownEBVInfection();
    }

    @NotNull
    private FunctionCreator hasSpecificInfectionCreator(@NotNull String doidToFind) {
        return function -> new HasSpecificInfection(doidModel(), doidToFind);
    }

    @NotNull
    private FunctionCreator meetsCovid19InfectionRequirementsCreator() {
        return function -> new MeetsCovid19InfectionRequirements();
    }

    @NotNull
    private FunctionCreator meetsCovid19VaccinationRequirementsCreator() {
        return function -> new MeetsCovid19VaccinationRequirements();
    }

    @NotNull
    private FunctionCreator isFullyVaccinatedCovid19Creator() {
        return function -> new IsFullyVaccinatedCovid19();
    }

    @NotNull
    private FunctionCreator hasReceivedLiveVaccineWithinMonthsCreator() {
        return function -> new HasReceivedLiveVaccineWithinMonths();
    }

    @NotNull
    private FunctionCreator canAdhereToAttenuatedVaccineUseCreator() {
        return function -> new CanAdhereToAttenuatedVaccineUse();
    }
}
