package com.hartwig.actin.algo.evaluation.complication;

import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.evaluation.RuleMapper;
import com.hartwig.actin.algo.evaluation.RuleMappingResources;
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter;
import com.hartwig.actin.algo.medication.MedicationStatusInterpreterOnEvaluationDate;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.jetbrains.annotations.NotNull;

public class ComplicationRuleMapper extends RuleMapper {

    public ComplicationRuleMapper(@NotNull final RuleMappingResources resources) {
        super(resources);
    }

    @NotNull
    @Override
    public Map<EligibilityRule, FunctionCreator> createMappings() {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_COMPLICATION_X, hasSpecificComplicationCreator());
        map.put(EligibilityRule.HAS_COMPLICATION_OF_CATEGORY_X, hasComplicationOfCategoryCreator());
        map.put(EligibilityRule.HAS_UNCONTROLLED_TUMOR_RELATED_PAIN, hasUncontrolledTumorRelatedPainCreator());
        map.put(EligibilityRule.HAS_LEPTOMENINGEAL_DISEASE, hasLeptomeningealDiseaseCreator());
        map.put(EligibilityRule.HAS_SPINAL_CORD_COMPRESSION, hasSpinalCordCompressionCreator());
        map.put(EligibilityRule.HAS_URINARY_INCONTINENCE, hasUrinaryIncontinenceCreator());
        map.put(EligibilityRule.HAS_BLADDER_OUTFLOW_OBSTRUCTION, hasBladderOutflowObstructionCreator());

        return map;
    }

    @NotNull
    private FunctionCreator hasSpecificComplicationCreator() {
        return function -> {
            String termToFind = functionInputResolver().createOneStringInput(function);
            return new HasSpecificComplication(termToFind);
        };
    }

    @NotNull
    private FunctionCreator hasComplicationOfCategoryCreator() {
        return function -> new HasComplicationOfCategory();
    }

    @NotNull
    private FunctionCreator hasUncontrolledTumorRelatedPainCreator() {
        MedicationStatusInterpreter interpreter = new MedicationStatusInterpreterOnEvaluationDate(referenceDateProvider().date());
        return function -> new HasUncontrolledTumorRelatedPain(interpreter);
    }

    @NotNull
    private FunctionCreator hasLeptomeningealDiseaseCreator() {
        return function -> new HasLeptomeningealDisease();
    }

    @NotNull
    private FunctionCreator hasSpinalCordCompressionCreator() {
        return function -> new HasSpinalCordCompression();
    }

    @NotNull
    private FunctionCreator hasUrinaryIncontinenceCreator() {
        return function -> new HasUrinaryIncontinence();
    }

    @NotNull
    private FunctionCreator hasBladderOutflowObstructionCreator() {
        return function -> new HasBladderOutflowObstruction();
    }
}
