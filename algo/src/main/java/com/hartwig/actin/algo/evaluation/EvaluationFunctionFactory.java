package com.hartwig.actin.algo.evaluation;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.algo.doid.DoidModel;
import com.hartwig.actin.algo.evaluation.composite.And;
import com.hartwig.actin.algo.evaluation.composite.Not;
import com.hartwig.actin.algo.evaluation.composite.Or;
import com.hartwig.actin.algo.evaluation.composite.WarnIf;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.interpretation.FunctionInputResolver;
import com.hartwig.actin.treatment.interpretation.composite.CompositeRules;

import org.jetbrains.annotations.NotNull;

public class EvaluationFunctionFactory {

    @NotNull
    private final Map<EligibilityRule, FunctionCreator> functionCreatorMap;

    @NotNull
    public static EvaluationFunctionFactory withDoidModel(@NotNull DoidModel doidModel) {
        return new EvaluationFunctionFactory(FunctionCreatorFactory.createFunctionCreatorMap(doidModel));
    }

    private EvaluationFunctionFactory(@NotNull final Map<EligibilityRule, FunctionCreator> functionCreatorMap) {
        this.functionCreatorMap = functionCreatorMap;
    }

    @NotNull
    public EvaluationFunction create(@NotNull EligibilityFunction function) {
        Boolean hasValidInputs = FunctionInputResolver.hasValidInputs(function);
        if (hasValidInputs == null || !hasValidInputs) {
            throw new IllegalStateException("No valid inputs defined for " + function);
        }

        if (CompositeRules.isComposite(function.rule())) {
            return createCompositeFunction(function);
        } else {
            return functionCreatorMap.get(function.rule()).create(function);
        }
    }

    @NotNull
    private EvaluationFunction createCompositeFunction(@NotNull EligibilityFunction function) {
        switch (function.rule()) {
            case AND:
                return new And(createMultipleCompositeParameters(function));
            case OR:
                return new Or(createMultipleCompositeParameters(function));
            case NOT:
                return new Not(createSingleCompositeParameter(function));
            case WARN_IF:
                return new WarnIf(createSingleCompositeParameter(function));
            default: {
                throw new IllegalStateException("Could not create evaluation function for composite rule '" + function.rule() + "'");
            }
        }
    }

    @NotNull
    private EvaluationFunction createSingleCompositeParameter(@NotNull EligibilityFunction function) {
        return create(FunctionInputResolver.createOneCompositeParameter(function));
    }

    @NotNull
    private List<EvaluationFunction> createMultipleCompositeParameters(@NotNull EligibilityFunction function) {
        List<EvaluationFunction> parameters = Lists.newArrayList();
        for (EligibilityFunction input : FunctionInputResolver.createAtLeastTwoCompositeParameters(function)) {
            parameters.add(create(input));
        }
        return parameters;
    }
}
