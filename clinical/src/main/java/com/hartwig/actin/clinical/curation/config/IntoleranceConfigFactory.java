package com.hartwig.actin.clinical.curation.config;

import java.util.Map;
import java.util.Set;

import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.curation.CurationValidator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class IntoleranceConfigFactory implements CurationConfigFactory<IntoleranceConfig> {

    private static final Logger LOGGER = LogManager.getLogger(IntoleranceConfigFactory.class);

    private final CurationValidator curationValidator;

    public IntoleranceConfigFactory(CurationValidator curationValidator) {
        this.curationValidator = curationValidator;
    }

    @NotNull
    @Override
    public IntoleranceConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        String input = parts[fields.get("input")];
        Set<String> doids = CurationUtil.toDOIDs(parts[fields.get("doids")]);
        if (!curationValidator.isValidDiseaseDoidSet(doids)) {
            LOGGER.warn("Intolerance config with input '{}' contains at least one invalid doid: '{}'", input, doids);
        }

        return ImmutableIntoleranceConfig.builder().input(input).name(parts[fields.get("name")]).doids(doids).build();
    }
}
