package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public class OncologicalHistoryConfigFactory implements CurationConfigFactory<OncologicalHistoryConfig> {

    private static final String SECOND_PRIMARY_STRING = "second primary";

    @NotNull
    @Override
    public OncologicalHistoryConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        boolean ignore = CurationUtil.isIgnoreString(parts[fields.get("name")]);
        return ImmutableOncologicalHistoryConfig.builder()
                .input(parts[fields.get("input")])
                .ignore(ignore)
                .curatedObject(!ignore ? curateObject(fields, parts) : null)
                .build();

    }

    @NotNull
    private static Object curateObject(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        if (parts[fields.get("category")].equalsIgnoreCase(SECOND_PRIMARY_STRING)) {
            return ImmutablePriorSecondPrimary.builder()
                    .tumorLocation(parts[fields.get("tumorLocation")])
                    .tumorSubLocation(parts[fields.get("tumorSubLocation")])
                    .tumorType(parts[fields.get("tumorType")])
                    .tumorSubType(parts[fields.get("tumorSubType")])
                    .doids(CurationUtil.toDOIDs(parts[fields.get("doids")]))
                    .diagnosedYear(ResourceFile.optionalInteger(parts[fields.get("year")]))
                    .treatmentHistory(parts[fields.get("treatmentHistoryPreviousPrimary")])
                    .isActive(ResourceFile.bool(parts[fields.get("isSecondPrimaryActive")]))
                    .build();
        } else {
            return ImmutablePriorTumorTreatment.builder()
                    .name(parts[fields.get("name")])
                    .year(ResourceFile.optionalInteger(parts[fields.get("year")]))
                    .category(parts[fields.get("category")])
                    .isSystemic(ResourceFile.bool(parts[fields.get("isSystemic")]))
                    .chemoType(ResourceFile.optionalString(parts[fields.get("chemoType")]))
                    .immunoType(ResourceFile.optionalString(parts[fields.get("immunoType")]))
                    .targetedType(ResourceFile.optionalString(parts[fields.get("targetedType")]))
                    .hormoneType(ResourceFile.optionalString(parts[fields.get("hormoneType")]))
                    .stemCellTransType(ResourceFile.optionalString(parts[fields.get("stemCellTransplantType")]))
                    .build();
        }
    }
}