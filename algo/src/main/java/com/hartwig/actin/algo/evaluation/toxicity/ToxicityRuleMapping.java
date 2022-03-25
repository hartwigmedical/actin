package com.hartwig.actin.algo.evaluation.toxicity;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;
import com.hartwig.actin.treatment.input.single.OneIntegerManyStrings;
import com.hartwig.actin.treatment.input.single.OneIntegerOneString;

import org.jetbrains.annotations.NotNull;

public final class ToxicityRuleMapping {

    private ToxicityRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull DoidModel doidModel) {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_INTOLERANCE_TO_NAME_X, hasIntoleranceWithSpecificNameCreator());
        map.put(EligibilityRule.HAS_INTOLERANCE_BELONGING_TO_DOID_X, hasIntoleranceWithSpecificDoidCreator(doidModel));
        map.put(EligibilityRule.HAS_INTOLERANCE_TO_TAXANE, hasIntoleranceToTaxaneCreator());
        map.put(EligibilityRule.HAS_INTOLERANCE_RELATED_TO_STUDY_MEDICATION, hasIntoleranceRelatedToStudyMedicationCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_ANAPHYLAXIS, hasHistoryAnaphylaxisCreator());
        map.put(EligibilityRule.HAS_EXPERIENCED_IMMUNE_RELATED_ADVERSE_EVENTS, hasExperiencedImmuneRelatedAdverseEventsCreator());
        map.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X, hasToxicityWithGradeCreator());
        map.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IN_Y, hasToxicityWithGradeAndNameCreator());
        map.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IGNORING_Y, hasToxicityWithGradeIgnoringNamesCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasIntoleranceWithSpecificNameCreator() {
        return function -> {
            String termToFind = FunctionInputResolver.createOneStringInput(function);
            return new HasIntoleranceWithSpecificName(termToFind);
        };
    }

    @NotNull
    private static FunctionCreator hasIntoleranceWithSpecificDoidCreator(@NotNull DoidModel doidModel) {
        return function -> {
            String doidToFind = FunctionInputResolver.createOneStringInput(function);
            return new HasIntoleranceWithSpecificDoid(doidModel, doidToFind);
        };
    }

    @NotNull
    private static FunctionCreator hasIntoleranceToTaxaneCreator() {
        return function -> new HasAllergyToTaxanes();
    }

    @NotNull
    private static FunctionCreator hasIntoleranceRelatedToStudyMedicationCreator() {
        return function -> new HasIntoleranceRelatedToStudyMedication();
    }

    @NotNull
    private static FunctionCreator hasHistoryAnaphylaxisCreator() {
        return function -> new HasHistoryOfAnaphylaxis();
    }

    @NotNull
    private static FunctionCreator hasExperiencedImmuneRelatedAdverseEventsCreator() {
        return function -> new HasExperiencedImmuneRelatedAdverseEvents();
    }

    @NotNull
    private static FunctionCreator hasToxicityWithGradeCreator() {
        return function -> {
            int minGrade = FunctionInputResolver.createOneIntegerInput(function);
            return new HasToxicityWithGrade(minGrade, null, Sets.newHashSet());
        };
    }

    @NotNull
    private static FunctionCreator hasToxicityWithGradeAndNameCreator() {
        return function -> {
            OneIntegerOneString input = FunctionInputResolver.createOneIntegerOneStringInput(function);
            return new HasToxicityWithGrade(input.integer(), input.string(), Sets.newHashSet());
        };
    }

    @NotNull
    private static FunctionCreator hasToxicityWithGradeIgnoringNamesCreator() {
        return function -> {
            OneIntegerManyStrings input = FunctionInputResolver.createOneIntegerManyStringsInput(function);
            return new HasToxicityWithGrade(input.integer(), null, Sets.newHashSet(input.strings()));
        };
    }
}
