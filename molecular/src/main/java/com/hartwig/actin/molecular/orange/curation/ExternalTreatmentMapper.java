package com.hartwig.actin.molecular.orange.curation;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ExternalTreatmentMapper {

    private static final Logger LOGGER = LogManager.getLogger(ExternalTreatmentMapper.class);

    @NotNull
    private final List<ExternalTreatmentMapping> mappings;

    public ExternalTreatmentMapper(@NotNull final List<ExternalTreatmentMapping> mappings) {
        this.mappings = mappings;
    }

    @NotNull
    public String map(@NotNull String externalTreatmentToMap) {
        for (ExternalTreatmentMapping mapping : mappings) {
            if (mapping.externalTreatment().equals(externalTreatmentToMap)) {
                LOGGER.debug("Mapping external treatment '{}' to ACTIN treatment '{}'",
                        mapping.externalTreatment(),
                        mapping.actinTreatment());
                return mapping.actinTreatment();
            }
        }

        return externalTreatmentToMap;
    }
}
