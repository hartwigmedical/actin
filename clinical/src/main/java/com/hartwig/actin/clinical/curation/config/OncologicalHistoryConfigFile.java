package com.hartwig.actin.clinical.curation.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.util.TsvUtil;

import org.jetbrains.annotations.NotNull;

public final class OncologicalHistoryConfigFile {

    private static final String DELIMITER = "\t";

    private static final String SECOND_PRIMARY_STRING = "second primary";

    private OncologicalHistoryConfigFile() {
    }

    @NotNull
    public static List<OncologicalHistoryConfig> read(@NotNull String oncologicalHistoryTsv) throws IOException {
        List<String> lines = Files.readAllLines(new File(oncologicalHistoryTsv).toPath());

        List<OncologicalHistoryConfig> oncologicalHistories = Lists.newArrayList();
        Map<String, Integer> fieldIndexMap = TsvUtil.createFieldIndexMap(lines.get(0).split(DELIMITER));
        for (String line : lines.subList(1, lines.size())) {
            String[] parts = line.split(DELIMITER, -1);
            boolean ignore = CurationUtil.isIgnoreString(parts[fieldIndexMap.get("name")]);

            oncologicalHistories.add(ImmutableOncologicalHistoryConfig.builder()
                    .input(parts[fieldIndexMap.get("input")])
                    .ignore(ignore)
                    .curatedObject(!ignore ? curateObject(fieldIndexMap, parts) : null)
                    .build());
        }

        return oncologicalHistories;
    }

    @NotNull
    private static Object curateObject(@NotNull Map<String, Integer> fieldIndexMap, @NotNull String[] parts) {
        if (parts[fieldIndexMap.get("category")].equalsIgnoreCase(SECOND_PRIMARY_STRING)) {
            return ImmutablePriorSecondPrimary.builder()
                    .tumorLocation(parts[fieldIndexMap.get("tumorLocation")])
                    .tumorSubLocation(parts[fieldIndexMap.get("tumorSubLocation")])
                    .tumorType(parts[fieldIndexMap.get("tumorType")])
                    .tumorSubType(parts[fieldIndexMap.get("tumorSubType")])
                    .doids(CurationUtil.parseDOID(parts[fieldIndexMap.get("doids")]))
                    .diagnosedYear(CurationUtil.parseOptionalInteger(parts[fieldIndexMap.get("year")]))
                    .isSecondPrimaryActive(CurationUtil.parseBoolean(parts[fieldIndexMap.get("isSecondPrimaryActive")]))
                    .build();
        } else {
            return ImmutablePriorTumorTreatment.builder()
                    .name(parts[fieldIndexMap.get("name")])
                    .year(CurationUtil.parseOptionalInteger(parts[fieldIndexMap.get("year")]))
                    .category(parts[fieldIndexMap.get("category")])
                    .isSystemic(CurationUtil.parseBoolean(parts[fieldIndexMap.get("isSystemic")]))
                    .chemoType(CurationUtil.optionalString(parts[fieldIndexMap.get("chemoType")]))
                    .immunoType(CurationUtil.optionalString(parts[fieldIndexMap.get("immunoType")]))
                    .targetedType(CurationUtil.optionalString(parts[fieldIndexMap.get("targetedType")]))
                    .hormoneType(CurationUtil.optionalString(parts[fieldIndexMap.get("hormoneType")]))
                    .stemCellTransType(CurationUtil.optionalString(parts[fieldIndexMap.get("stemCellTransplantType")]))
                    .build();
        }
    }
}
