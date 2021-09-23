package com.hartwig.actin.clinical.curation;

import java.util.List;

import com.hartwig.actin.clinical.curation.config.OncologicalHistoryConfig;
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class CurationDatabase {

    @NotNull
    public abstract List<PrimaryTumorConfig> primaryTumorConfigs();

    @NotNull
    public abstract List<OncologicalHistoryConfig> oncologicalHistoryConfigs();

}
