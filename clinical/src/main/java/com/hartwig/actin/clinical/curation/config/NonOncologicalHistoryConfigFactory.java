package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public class NonOncologicalHistoryConfigFactory implements CurationConfigFactory<NonOncologicalHistoryConfig> {

    @NotNull
    @Override
    public NonOncologicalHistoryConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        boolean ignore = CurationUtil.isIgnoreString(parts[fields.get("name")]);

        return ImmutableNonOncologicalHistoryConfig.builder()
                .input(parts[fields.get("input")])
                .ignore(ignore)
                .curated(!ignore ? toCuratedObject(fields, parts) : null)
                .build();
    }

    @NotNull
    private static Object toCuratedObject(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        if (parts[fields.get("isLVEF")].equals("1")) {
            return Double.valueOf(parts[fields.get("lvefValue")]);
        } else {
            return ImmutablePriorOtherCondition.builder()
                    .name(parts[fields.get("name")])
                    .year(ResourceFile.optionalInteger(parts[fields.get("year")]))
                    .month(ResourceFile.optionalInteger(parts[fields.get("month")]))
                    .doids(CurationUtil.toDOIDs(parts[fields.get("doids")]))
                    .category(parts[fields.get("category")])
                    .isContraindicationForTherapy(ResourceFile.bool(parts[fields.get("isContraindicationForTherapy")]))
                    .build();
        }
    }
}
