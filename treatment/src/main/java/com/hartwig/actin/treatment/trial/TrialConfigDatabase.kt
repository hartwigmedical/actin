package com.hartwig.actin.treatment.trial;

import java.util.List;

import com.hartwig.actin.treatment.trial.config.CohortDefinitionConfig;
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaConfig;
import com.hartwig.actin.treatment.trial.config.InclusionCriteriaReferenceConfig;
import com.hartwig.actin.treatment.trial.config.TrialDefinitionConfig;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TrialConfigDatabase {

    @NotNull
    public abstract List<TrialDefinitionConfig> trialDefinitionConfigs();

    @NotNull
    public abstract List<CohortDefinitionConfig> cohortDefinitionConfigs();

    @NotNull
    public abstract List<InclusionCriteriaConfig> inclusionCriteriaConfigs();

    @NotNull
    public abstract List<InclusionCriteriaReferenceConfig> inclusionCriteriaReferenceConfigs();
}
