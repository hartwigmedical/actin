package com.hartwig.actin.algo.evaluation.toxicity;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.util.EvaluationFactory;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;
import com.hartwig.actin.treatment.interpretation.single.OneIntegerManyStrings;
import com.hartwig.actin.treatment.interpretation.single.OneIntegerOneString;

import org.jetbrains.annotations.NotNull;

public final class ToxicityRuleMapping {

    private ToxicityRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_ALLERGY_OF_NAME_X, function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_ALLERGY_BELONGING_TO_DOID_X,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_ALLERGY_TO_TAXANE, function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_ALLERGY_RELATED_TO_STUDY_MEDICATION, hasAllergyRelatedToStudyMedicationCreator());
        map.put(EligibilityRule.HAS_HISTORY_OF_ANAPHYLAXIS, hasHistoryAnaphylaxisCreator());
        map.put(EligibilityRule.HAS_EXPERIENCED_IMMUNE_RELATED_ADVERSE_EVENTS,
                function -> record -> EvaluationFactory.create(EvaluationResult.NOT_IMPLEMENTED));
        map.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X, hasToxicityWithGradeCreator());
        map.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IN_Y, hasToxicityWithGradeAndNameCreator());
        map.put(EligibilityRule.HAS_TOXICITY_OF_AT_LEAST_GRADE_X_IGNORING_Y, hasToxicityWithGradeIgnoringNamesCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasAllergyRelatedToStudyMedicationCreator() {
        return function -> new HasAllergyRelatedToStudyMedication();
    }

    @NotNull
    private static FunctionCreator hasHistoryAnaphylaxisCreator() {
        return function -> new HasHistoryAnaphylaxis();
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
