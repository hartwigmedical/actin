package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import com.hartwig.actin.clinical.curation.CurationUtil;

import org.jetbrains.annotations.NotNull;

public class PrimaryTumorConfigFactory implements CurationConfigFactory<PrimaryTumorConfig> {

    @NotNull
    @Override
    public PrimaryTumorConfig create(@NotNull final Map<String, Integer> fields, @NotNull final String[] parts) {
        return ImmutablePrimaryTumorConfig.builder()
                .input(parts[fields.get("input")])
                .primaryTumorLocation(parts[fields.get("primaryTumorLocation")])
                .primaryTumorSubLocation(parts[fields.get("primaryTumorSubLocation")])
                .primaryTumorType(parts[fields.get("primaryTumorType")])
                .primaryTumorSubType(parts[fields.get("primaryTumorSubType")])
                .primaryTumorExtraDetails(parts[fields.get("primaryTumorExtraDetails")])
                .doids(CurationUtil.toDOIDs(parts[fields.get("doids")]))
                .build();
    }
}
