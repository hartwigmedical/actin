package com.hartwig.actin.clinical.curation.config;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.curation.CurationValidator;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition;
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition;
import com.hartwig.actin.util.ResourceFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class NonOncologicalHistoryConfigFactory implements CurationConfigFactory<NonOncologicalHistoryConfig> {

    private static final Logger LOGGER = LogManager.getLogger(NonOncologicalHistoryConfigFactory.class);

    private final CurationValidator curationValidator;

    public NonOncologicalHistoryConfigFactory(CurationValidator curationValidator) {
        this.curationValidator = curationValidator;
    }

    @NotNull
    @Override
    public NonOncologicalHistoryConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        String input = parts[fields.get("input")];
        boolean ignore = CurationUtil.isIgnoreString(parts[fields.get("name")]);

        return ImmutableNonOncologicalHistoryConfig.builder()
                .input(input)
                .ignore(ignore)
                .lvef(!ignore ? toCuratedLVEF(fields, parts) : Optional.empty())
                .priorOtherCondition(!ignore ? toCuratedPriorOtherCondition(fields, input, parts) : Optional.empty())
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
    private Optional<PriorOtherCondition> toCuratedPriorOtherCondition(@NotNull Map<String, Integer> fields, @NotNull String input,
            @NotNull String[] parts) {
        if (!isLVEF(fields, parts)) {
            Set<String> doids = CurationUtil.toDOIDs(parts[fields.get("doids")]);
            if (!curationValidator.isValidDiseaseDoidSet(doids)) {
                LOGGER.warn("Non-oncological history config with input '{}' contains at least one invalid doid: '{}'", input, doids);
            }

            return Optional.of(ImmutablePriorOtherCondition.builder()
                    .name(parts[fields.get("name")])
                    .year(ResourceFile.optionalInteger(parts[fields.get("year")]))
                    .month(ResourceFile.optionalInteger(parts[fields.get("month")]))
                    .doids(doids)
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
