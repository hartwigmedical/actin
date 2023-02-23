package com.hartwig.actin.clinical.curation.config;

import java.util.Map;
import java.util.Optional;

import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
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
                .lvef(!ignore ? toCuratedLVEF(fields, parts) : Optional.empty())
                .priorOtherCondition(!ignore ? toCuratedPriorOtherCondition(fields, parts) : Optional.empty())
                .build();
    }

    @NotNull
    private static Optional<Double> toCuratedLVEF(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        if (isLVEF(fields, parts)) {
            return Optional.of(Double.valueOf(parts[fields.get("lvefValue")]));
        } else {
            return Optional.empty();
        }
    }

    @NotNull
    private static Optional<PriorOtherCondition> toCuratedPriorOtherCondition(@NotNull Map<String, Integer> fields,
            @NotNull String[] parts) {
        if (!isLVEF(fields, parts)) {
            return Optional.of(ImmutablePriorOtherCondition.builder()
                    .name(parts[fields.get("name")])
                    .year(ResourceFile.optionalInteger(parts[fields.get("year")]))
                    .month(ResourceFile.optionalInteger(parts[fields.get("month")]))
                    .doids(CurationUtil.toDOIDs(parts[fields.get("doids")]))
                    .category(parts[fields.get("category")])
                    .isContraindicationForTherapy(ResourceFile.bool(parts[fields.get("isContraindicationForTherapy")]))
                    .build());
        } else {
            return Optional.empty();
        }
    }

    private static boolean isLVEF(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return parts[fields.get("isLVEF")].equals("1");
    }
}
