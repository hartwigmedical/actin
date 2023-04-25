package com.hartwig.actin.algo.evaluation;

import java.util.Map;

import com.hartwig.actin.algo.calendar.ReferenceDateProvider;
import com.hartwig.actin.doid.DoidModel;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.input.FunctionInputResolver;

import org.jetbrains.annotations.NotNull;

public abstract class RuleMapper {

    @NotNull
    private final RuleMappingResources resources;

    public RuleMapper(@NotNull final RuleMappingResources resources) {
        this.resources = resources;
    }

    @NotNull
    protected ReferenceDateProvider referenceDateProvider() {
        return resources.referenceDateProvider();
    }

    @NotNull
    protected DoidModel doidModel() {
        return resources.doidModel();
    }

    @NotNull
    protected FunctionInputResolver functionInputResolver() {
        return resources.functionInputResolver();
    }

    @NotNull
    public abstract Map<EligibilityRule, FunctionCreator> createMappings();

}
