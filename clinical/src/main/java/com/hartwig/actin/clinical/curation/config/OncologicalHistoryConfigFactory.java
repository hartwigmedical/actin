package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;
import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public class OncologicalHistoryConfigFactory implements CurationConfigFactory<OncologicalHistoryConfig> {

    @NotNull
    @Override
    public OncologicalHistoryConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        boolean ignore = CurationUtil.isIgnoreString(parts[fields.get("name")]);
        return ImmutableOncologicalHistoryConfig.builder()
                .input(parts[fields.get("input")])
                .ignore(ignore)
                .curated(!ignore ? curateObject(fields, parts) : null)
                .build();
    }

    @NotNull
    private static PriorTumorTreatment curateObject(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutablePriorTumorTreatment.builder()
                .name(parts[fields.get("name")])
                .startYear(ResourceFile.optionalInteger(parts[fields.get("startYear")]))
                .startMonth(ResourceFile.optionalInteger(parts[fields.get("startMonth")]))
                .stopYear(ResourceFile.optionalInteger(parts[fields.get("stopYear")]))
                .stopMonth(ResourceFile.optionalInteger(parts[fields.get("stopMonth")]))
                .bestResponse(ResourceFile.optionalString(parts[fields.get("bestResponse")]))
                .stopReason(ResourceFile.optionalString(parts[fields.get("stopReason")]))
                .categories(TreatmentCategoryResolver.fromStringList(parts[fields.get("category")]))
                .isSystemic(ResourceFile.bool(parts[fields.get("isSystemic")]))
                .chemoType(ResourceFile.optionalString(parts[fields.get("chemoType")]))
                .immunoType(ResourceFile.optionalString(parts[fields.get("immunoType")]))
                .targetedType(ResourceFile.optionalString(parts[fields.get("targetedType")]))
                .hormoneType(ResourceFile.optionalString(parts[fields.get("hormoneType")]))
                .radioType(ResourceFile.optionalString(parts[fields.get("radioType")]))
                .carTType(ResourceFile.optionalString(parts[fields.get("carTType")]))
                .transplantType(ResourceFile.optionalString(parts[fields.get("transplantType")]))
                .supportiveType(ResourceFile.optionalString(parts[fields.get("supportiveType")]))
                .trialAcronym(ResourceFile.optionalString(parts[fields.get("trialAcronym")]))
                .build();
    }
}
