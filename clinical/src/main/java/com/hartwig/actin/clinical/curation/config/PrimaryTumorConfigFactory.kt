package com.hartwig.actin.clinical.curation.config;

import java.util.Map;
import java.util.Set;

import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.curation.CurationValidator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class PrimaryTumorConfigFactory implements CurationConfigFactory<PrimaryTumorConfig> {

    private static final Logger LOGGER = LogManager.getLogger(PrimaryTumorConfigFactory.class);

    private final CurationValidator curationValidator;

    public PrimaryTumorConfigFactory(CurationValidator curationValidator) {
        this.curationValidator = curationValidator;
    }

    @NotNull
    @Override
    public PrimaryTumorConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        String input = parts[fields.get("input")];
        Set<String> doids = CurationUtil.toDOIDs(parts[fields.get("doids")]);
        if (!curationValidator.isValidCancerDoidSet(doids)) {
            LOGGER.warn("Primary tumor config with input '{}' contains at least one invalid doid: '{}'", input, doids);
        }

        return ImmutablePrimaryTumorConfig.builder()
                .input(input)
                .primaryTumorLocation(parts[fields.get("primaryTumorLocation")])
                .primaryTumorSubLocation(parts[fields.get("primaryTumorSubLocation")])
                .primaryTumorType(parts[fields.get("primaryTumorType")])
                .primaryTumorSubType(parts[fields.get("primaryTumorSubType")])
                .primaryTumorExtraDetails(parts[fields.get("primaryTumorExtraDetails")])
                .doids(doids)
                .build();
    }
}
