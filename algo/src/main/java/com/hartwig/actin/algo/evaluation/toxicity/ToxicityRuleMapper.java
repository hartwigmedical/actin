package com.hartwig.actin.algo.evaluation.toxicity;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.single.OneIntegerManyStrings;
import com.hartwig.actin.treatment.input.single.OneIntegerOneString;

import org.jetbrains.annotations.NotNull;

public class ToxicityRuleMapper extends RuleMapper {

    public ToxicityRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);
    }

    @NotNull
    @Override
    public Map<EligibilityRule, FunctionCreator> createMappings() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_INTOLERANCE_TO_NAME_X, hasIntoleranceWithSpecificNameCreator());
        map.put(EligibilityRule.HAS_INTOLERANCE_BELONGING_TO_DOID_TERM_X, hasIntoleranceWithSpecificDoidTermCreator());
        map.put(EligibilityRule.HAS_INTOLERANCE_TO_TAXANE, hasIntoleranceToTaxaneCreator());
        map.put(EligibilityRule.HAS_INTOLERANCE_RELATED_TO_STUDY_MEDICATION, hasIntoleranceRelatedToStudyMedicationCreator());
        map.put(EligibilityRule.HAS_INTOLERANCE_FOR_PD_1_OR_PD_L1_INHIBITORS, hasIntoleranceToPD1OrPDL1InhibitorsCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_ANAPHYLAXIS, hasHistoryAnaphylaxisCreator());
        map.put(EligibilityRule.HAS_EXPERIENCED_IMMUNE_RELATED_ADVERSE_EVENTS, hasExperiencedImmuneRelatedAdverseEventsCreator());
        map.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X, hasToxicityWithGradeCreator());
        map.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IN_Y, hasToxicityWithGradeAndNameCreator());
        map.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IGNORING_Y, hasToxicityWithGradeIgnoringNamesCreator());

        return map;
    }

    private FunctionCreator hasIntoleranceToPD1OrPDL1InhibitorsCreator() {
        return function -> new HasIntoleranceForPD1OrPDL1Inhibitors(doidModel());
    }

    @NotNull
    private FunctionCreator hasIntoleranceWithSpecificNameCreator() {
        return function -> {
            String termToFind = functionInputResolver().createOneStringInput(function);
            return new HasIntoleranceWithSpecificName(termToFind);
        };
    }

    @NotNull
    private FunctionCreator hasIntoleranceWithSpecificDoidTermCreator() {
        return function -> {
            String doidTermToFind = functionInputResolver().createOneDoidTermInput(function);
            return new HasIntoleranceWithSpecificDoid(doidModel(), doidModel().resolveDoidForTerm(doidTermToFind));
        };
    }

    @NotNull
    private FunctionCreator hasIntoleranceToTaxaneCreator() {
        return function -> new HasIntoleranceToTaxanes();
    }

    @NotNull
    private FunctionCreator hasIntoleranceRelatedToStudyMedicationCreator() {
        return function -> new HasIntoleranceRelatedToStudyMedication();
    }

    @NotNull
    private FunctionCreator hasHistoryAnaphylaxisCreator() {
        return function -> new HasHistoryOfAnaphylaxis();
    }

    @NotNull
    private FunctionCreator hasExperiencedImmuneRelatedAdverseEventsCreator() {
        return function -> new HasExperiencedImmuneRelatedAdverseEvents();
    }

    @NotNull
    private FunctionCreator hasToxicityWithGradeCreator() {
        return function -> {
            int minGrade = functionInputResolver().createOneIntegerInput(function);
            return new HasToxicityWithGrade(minGrade, null, Sets.newHashSet());
        };
    }

    @NotNull
    private FunctionCreator hasToxicityWithGradeAndNameCreator() {
        return function -> {
            OneIntegerOneString input = functionInputResolver().createOneIntegerOneStringInput(function);
            return new HasToxicityWithGrade(input.integer(), input.string(), Sets.newHashSet());
        };
    }

    @NotNull
    private FunctionCreator hasToxicityWithGradeIgnoringNamesCreator() {
        return function -> {
            OneIntegerManyStrings input = functionInputResolver().createOneIntegerManyStringsInput(function);
            return new HasToxicityWithGrade(input.integer(), null, Sets.newHashSet(input.strings()));
        };
    }
}
