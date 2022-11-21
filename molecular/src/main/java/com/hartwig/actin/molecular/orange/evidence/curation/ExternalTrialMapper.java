package com.hartwig.actin.molecular.orange.evidence.curation;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ExternalTrialMapper {

    private static final Logger LOGGER = LogManager.getLogger(ExternalTrialMapper.class);

    @NotNull
    private final List<ExternalTrialMapping> mappings;

    public ExternalTrialMapper(@NotNull final List<ExternalTrialMapping> mappings) {
        this.mappings = mappings;
    }

    @NotNull
    public String map(@NotNull String externalTrialToMap) {
        for (ExternalTrialMapping mapping : mappings) {
            if (mapping.externalTrial().equals(externalTrialToMap)) {
                LOGGER.debug("Mapping external trial '{}' to ACTIN trial '{}'", mapping.externalTrial(), mapping.actinTrial());
                return mapping.actinTrial();
            }
        }

        return externalTrialToMap;
    }
}
