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

        map.put(EligibilityRule.HAS_ANY_COMPLICATION, hasAnyComplicationCreator());
        map.put(EligibilityRule.HAS_COMPLICATION_X, hasSpecificComplicationCreator());
        map.put(EligibilityRule.HAS_COMPLICATION_OF_CATEGORY_X, hasComplicationOfCategoryCreator());
        map.put(EligibilityRule.HAS_POTENTIAL_UNCONTROLLED_TUMOR_RELATED_PAIN, hasPotentialUncontrolledTumorRelatedPainCreator());
        map.put(EligibilityRule.HAS_LEPTOMENINGEAL_DISEASE, hasLeptomeningealDiseaseCreator());

        return map;
    }

    @NotNull
    private FunctionCreator hasAnyComplicationCreator() {
        return function -> new HasAnyComplication();
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
        return function -> {
            String categoryToFind = functionInputResolver().createOneStringInput(function);
            return new HasComplicationOfCategory(categoryToFind);
        };
    }

    @NotNull
    private FunctionCreator hasPotentialUncontrolledTumorRelatedPainCreator() {
        MedicationStatusInterpreter interpreter = new MedicationStatusInterpreterOnEvaluationDate(referenceDateProvider().date());
        return function -> new HasPotentialUncontrolledTumorRelatedPain(interpreter);
    }

    @NotNull
    private FunctionCreator hasLeptomeningealDiseaseCreator() {
        return function -> new HasLeptomeningealDisease();
    }

}
