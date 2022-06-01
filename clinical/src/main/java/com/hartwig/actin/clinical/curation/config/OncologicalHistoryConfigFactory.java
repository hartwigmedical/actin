package com.hartwig.actin.clinical.curation.config;

import java.util.Map;

import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary;
import com.hartwig.actin.clinical.datamodel.ImmutablePriorTumorTreatment;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;
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
                .curated(!ignore ? curateObject(fields, parts) : null)
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
                    .diagnosedYear(ResourceFile.optionalInteger(parts[fields.get("startYear")]))
                    .diagnosedMonth(ResourceFile.optionalInteger(parts[fields.get("startMonth")]))
                    .treatmentHistory(parts[fields.get("treatmentHistoryPreviousPrimary")])
                    .lastTreatmentYear(ResourceFile.optionalInteger(parts[fields.get("lastTreatmentYear")]))
                    .lastTreatmentMonth(ResourceFile.optionalInteger(parts[fields.get("lastTreatmentMonth")]))
                    .isActive(ResourceFile.bool(parts[fields.get("isSecondPrimaryActive")]))
                    .build();
        } else {
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
}
