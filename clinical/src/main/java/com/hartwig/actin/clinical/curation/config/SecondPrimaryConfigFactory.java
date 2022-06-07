package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary;
import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public class SecondPrimaryConfigFactory implements CurationConfigFactory<SecondPrimaryConfig> {

    @NotNull
    @Override
    public SecondPrimaryConfig create(@NotNull final Map<String, Integer> fields, @NotNull final String[] parts) {
        boolean ignore = CurationUtil.isIgnoreString(parts[fields.get("name")]);
        return ImmutableSecondPrimaryConfig.builder()
                .input(parts[fields.get("input")])
                .ignore(ignore)
                .curated(!ignore ? curatedObject(fields, parts) : null)
                .build();
    }

    @NotNull
    private static PriorSecondPrimary curatedObject(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutablePriorSecondPrimary.builder()
                .tumorLocation(parts[fields.get("tumorLocation")])
                .tumorSubLocation(parts[fields.get("tumorSubLocation")])
                .tumorType(parts[fields.get("tumorType")])
                .tumorSubType(parts[fields.get("tumorSubType")])
                .doids(CurationUtil.toDOIDs(parts[fields.get("doids")]))
                .diagnosedYear(ResourceFile.optionalInteger(parts[fields.get("diagnosedYear")]))
                .diagnosedMonth(ResourceFile.optionalInteger(parts[fields.get("diagnosedMonth")]))
                .treatmentHistory(parts[fields.get("treatmentHistory")])
                .lastTreatmentYear(ResourceFile.optionalInteger(parts[fields.get("lastTreatmentYear")]))
                .lastTreatmentMonth(ResourceFile.optionalInteger(parts[fields.get("lastTreatmentMonth")]))
                .isActive(ResourceFile.bool(parts[fields.get("isActive")]))
                .build();
    }
}
