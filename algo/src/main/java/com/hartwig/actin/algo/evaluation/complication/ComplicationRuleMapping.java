package com.hartwig.actin.algo.evaluation.complication;

import java.time.LocalDate;
import java.util.Map;

import com.google.common.collect.Maps;
import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.algo.evaluation.FunctionCreator;
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter;
import com.hartwig.actin.algo.medication.MedicationStatusInterpreterOnEvaluationDate;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public final class ComplicationRuleMapping {

    private ComplicationRuleMapping() {
    }

    @NotNull
    public static Map<EligibilityRule, FunctionCreator> create(@NotNull ReferenceDateProvider referenceDateProvider) {
        Map<EligibilityRule, FunctionCreator> map = Maps.newHashMap();

        map.put(EligibilityRule.HAS_COMPLICATION_X, hasSpecificComplicationCreator());
        map.put(EligibilityRule.HAS_UNCONTROLLED_TUMOR_RELATED_PAIN, hasUncontrolledTumorRelatedPainCreator(referenceDateProvider.date()));
        map.put(EligibilityRule.HAS_LEPTOMENINGEAL_DISEASE, hasLeptomeningealDiseaseCreator());
        map.put(EligibilityRule.HAS_SPINAL_CORD_COMPRESSION, hasSpinalCordCompressionCreator());
        map.put(EligibilityRule.HAS_URINARY_INCONTINENCE, hasUrinaryIncontinenceCreator());
        map.put(EligibilityRule.HAS_BLADDER_OUTFLOW_OBSTRUCTION, hasBladderOutflowObstructionCreator());

        return map;
    }

    @NotNull
    private static FunctionCreator hasSpecificComplicationCreator() {
        return function -> {
            String termToFind = FunctionInputResolver.createOneStringInput(function);
            return new HasSpecificComplication(termToFind);
        };
    }

    @NotNull
    private static FunctionCreator hasUncontrolledTumorRelatedPainCreator(@NotNull LocalDate evaluationDate) {
        MedicationStatusInterpreter interpreter = new MedicationStatusInterpreterOnEvaluationDate(evaluationDate);
        return function -> new HasUncontrolledTumorRelatedPain(interpreter);
    }

    @NotNull
    private static FunctionCreator hasLeptomeningealDiseaseCreator() {
        return function -> new HasLeptomeningealDisease();
    }

    @NotNull
    private static FunctionCreator hasSpinalCordCompressionCreator() {
        return function -> new HasSpinalCordCompression();
    }

    @NotNull
    private static FunctionCreator hasUrinaryIncontinenceCreator() {
        return function -> new HasUrinaryIncontinence();
    }

    @NotNull
    private static FunctionCreator hasBladderOutflowObstructionCreator() {
        return function -> new HasBladderOutflowObstruction();
    }
}
