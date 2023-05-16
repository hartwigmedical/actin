package com.hartwig.actin.clinical.curation.config;

import com.hartwig.actin.clinical.datamodel.TreatmentHistoryEntry;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class TreatmentHistoryEntryConfig implements CurationConfig {

    @Nullable
    public abstract TreatmentHistoryEntry curated();
}
