package com.hartwig.actin.treatment.database;

import java.util.List;

import com.hartwig.actin.treatment.database.config.CohortDefinitionConfig;
import com.hartwig.actin.treatment.database.config.InclusionCriteriaConfig;
import com.hartwig.actin.treatment.database.config.TrialDefinitionConfig;

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
}
