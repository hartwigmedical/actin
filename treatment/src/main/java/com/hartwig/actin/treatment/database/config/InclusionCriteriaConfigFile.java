package com.hartwig.actin.treatment.database.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.treatment.database.TreatmentDatabaseUtil;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public final class InclusionCriteriaConfigFile {

    private static final String DELIMITER = "\t";

    private InclusionCriteriaConfigFile() {
    }

    @NotNull
    public static List<InclusionCriteriaConfig> read(@NotNull String inclusionCriteriaTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(inclusionCriteriaTsv).toPath());

        List<InclusionCriteriaConfig> configs = Lists.newArrayList();
        Map<String, Integer> fieldIndexMap = ResourceFile.createFields(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            configs.add(fromParts(fieldIndexMap, line.split(DELIMITER, -1)));
        }
        return configs;
    }

    @NotNull
    private static InclusionCriteriaConfig fromParts(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableInclusionCriteriaConfig.builder()
                .trialId(parts[fields.get("trialId")])
                .appliesToCohorts(TreatmentDatabaseUtil.toCohorts(parts[fields.get("appliesToCohorts")]))
                .eligibilityRule(EligibilityRule.valueOf(parts[fields.get("eligibilityRule")]))
                .eligibilityParameters(TreatmentDatabaseUtil.toParameters(parts[fields.get("eligibilityParameters")]))
                .build();
    }
}
