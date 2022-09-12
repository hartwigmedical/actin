package com.hartwig.actin.algo.evaluation.infection;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public class InfectionRuleMapper extends RuleMapper {

    private static final String HEPATITIS_A_DOID = "12549";
    private static final String HEPATITIS_B_DOID = "2043";
    private static final String HEPATITIS_C_DOID = "1883";
    private static final String HIV_DOID = "526";
    private static final String CYTOMEGALOVIRUS_DOID = "0080827";
    private static final String TUBERCULOSIS_DOID = "399";

    public InfectionRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);
    }

    @NotNull
    @Override
    public Map<EligibilityRule, FunctionCreator> createMappings() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_ACTIVE_INFECTION, hasActiveInfectionCreator());
        map.put(EligibilityRule.HAS_KNOWN_EBV_INFECTION, hasKnownEBVInfectionCreator());
        map.put(EligibilityRule.HAS_KNOWN_HEPATITIS_A_INFECTION, hasSpecificInfectionCreator(HEPATITIS_A_DOID));
        map.put(EligibilityRule.HAS_KNOWN_HEPATITIS_B_INFECTION, hasSpecificInfectionCreator(HEPATITIS_B_DOID));
        map.put(EligibilityRule.HAS_KNOWN_HEPATITIS_C_INFECTION, hasSpecificInfectionCreator(HEPATITIS_C_DOID));
        map.put(EligibilityRule.HAS_KNOWN_HIV_INFECTION, hasSpecificInfectionCreator(HIV_DOID));
        map.put(EligibilityRule.HAS_KNOWN_HTLV_INFECTION, hasKnownHTLVInfectionCreator());
        map.put(EligibilityRule.HAS_KNOWN_CYTOMEGALOVIRUS_INFECTION, hasSpecificInfectionCreator(CYTOMEGALOVIRUS_DOID));
        map.put(EligibilityRule.HAS_KNOWN_TUBERCULOSIS_INFECTION, hasSpecificInfectionCreator(TUBERCULOSIS_DOID));
        map.put(EligibilityRule.MEETS_COVID_19_INFECTION_REQUIREMENTS, meetsCovid19InfectionRequirementsCreator());
        map.put(EligibilityRule.MEETS_COVID_19_VACCINATION_REQUIREMENTS, meetsCovid19VaccinationRequirementsCreator());
        map.put(EligibilityRule.IS_FULLY_VACCINATED_AGAINST_COVID_19, isFullyVaccinatedCovid19Creator());
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
    private FunctionCreator hasKnownHTLVInfectionCreator() {
        return function -> new HasKnownHTLVInfection();
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
    private FunctionCreator canAdhereToAttenuatedVaccineUseCreator() {
        return function -> new CanAdhereToAttenuatedVaccineUse();
    }
}
