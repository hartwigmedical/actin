package com.hartwig.actin.clinical.curation.config;

import java.util.Map;
import java.util.Set;

import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.curation.CurationValidator;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.util.ResourceFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class SecondPrimaryConfigFactory implements CurationConfigFactory<SecondPrimaryConfig> {

    private static final Logger LOGGER = LogManager.getLogger(SecondPrimaryConfigFactory.class);

    private final CurationValidator curationValidator;

    public SecondPrimaryConfigFactory(CurationValidator curationValidator) {
        this.curationValidator = curationValidator;
    }

    @NotNull
    @Override
    public SecondPrimaryConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        String input = parts[fields.get("input")];
        boolean ignore = CurationUtil.isIgnoreString(parts[fields.get("name")]);

        return ImmutableSecondPrimaryConfig.builder()
                .input(input)
                .ignore(ignore)
                .curated(!ignore ? curatedPriorSecondPrimary(fields, input, parts) : null)
                .build();
    }

    @NotNull
    private PriorSecondPrimary curatedPriorSecondPrimary(@NotNull Map<String, Integer> fields, @NotNull String input,
            @NotNull String[] parts) {
        Set<String> doids = CurationUtil.toDOIDs(parts[fields.get("doids")]);
        if (!curationValidator.isValidCancerDoidSet(doids)) {
            LOGGER.warn("Second primary config with input '{}' contains at least one invalid doid: '{}'", input, doids);
        }

        return ImmutablePriorSecondPrimary.builder()
                .tumorLocation(parts[fields.get("tumorLocation")])
                .tumorSubLocation(parts[fields.get("tumorSubLocation")])
                .tumorType(parts[fields.get("tumorType")])
                .tumorSubType(parts[fields.get("tumorSubType")])
                .doids(doids)
                .diagnosedYear(ResourceFile.optionalInteger(parts[fields.get("diagnosedYear")]))
                .diagnosedMonth(ResourceFile.optionalInteger(parts[fields.get("diagnosedMonth")]))
                .treatmentHistory(parts[fields.get("treatmentHistory")])
                .lastTreatmentYear(ResourceFile.optionalInteger(parts[fields.get("lastTreatmentYear")]))
                .lastTreatmentMonth(ResourceFile.optionalInteger(parts[fields.get("lastTreatmentMonth")]))
                .isActive(ResourceFile.bool(parts[fields.get("isActive")]))
                .build();
    }
}
