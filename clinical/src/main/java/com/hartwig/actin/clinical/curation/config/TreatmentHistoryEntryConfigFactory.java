package com.hartwig.actin.clinical.curation.config;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.datamodel.ImmutableChemotherapy;
import com.hartwig.actin.clinical.datamodel.ImmutableCombinedTherapy;
import com.hartwig.actin.clinical.datamodel.ImmutableImmunotherapy;
import com.hartwig.actin.clinical.datamodel.ImmutableOtherTherapy;
import com.hartwig.actin.clinical.datamodel.ImmutableRadiotherapy;
import com.hartwig.actin.clinical.datamodel.ImmutableSurgery;
import com.hartwig.actin.clinical.datamodel.ImmutableTherapyHistoryDetails;
import com.hartwig.actin.clinical.datamodel.ImmutableTreatmentHistoryEntry;
import com.hartwig.actin.clinical.datamodel.StopReason;
import com.hartwig.actin.clinical.datamodel.Therapy;
import com.hartwig.actin.clinical.datamodel.TherapyHistoryDetails;
import com.hartwig.actin.clinical.datamodel.Treatment;
import com.hartwig.actin.clinical.datamodel.TreatmentCategory;
import com.hartwig.actin.clinical.datamodel.TreatmentHistoryEntry;
import com.hartwig.actin.clinical.datamodel.TreatmentResponse;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;
import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public class TreatmentHistoryEntryConfigFactory implements CurationConfigFactory<ImmutableTreatmentHistoryEntryConfig> {

    @NotNull
    @Override
    public ImmutableTreatmentHistoryEntryConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        boolean ignore = CurationUtil.isIgnoreString(parts[fields.get("name")]);
        return ImmutableTreatmentHistoryEntryConfig.builder()
                .input(parts[fields.get("input")])
                .ignore(ignore)
                .curated(!ignore ? curateObject(fields, parts) : null)
                .build();
    }

    @NotNull
    private static TreatmentHistoryEntry curateObject(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        Set<TreatmentCategory> categories = TreatmentCategoryResolver.fromStringList(parts[fields.get("category")]);

        Set<Therapy> therapies =
                categories.stream().filter(cat -> cat != TreatmentCategory.SURGERY && cat != TreatmentCategory.TRIAL).map(cat -> {
                    switch (cat) {
                        case CHEMOTHERAPY:
                            return chemotherapy(fields, parts);
                        case RADIOTHERAPY:
                            return radiotherapy(fields, parts);
                        case IMMUNOTHERAPY:
                            return immunotherapy(fields, parts);
                        default:
                            return otherTherapy(fields, parts, cat);
                    }
                }).collect(Collectors.toSet());

        Set<Treatment> treatments;
        TherapyHistoryDetails therapyHistoryDetails;
        if (!therapies.isEmpty()) {
            Treatment therapy = (therapies.size() == 1)
                    ? therapies.iterator().next()
                    : ImmutableCombinedTherapy.builder()
                            .name(parts[fields.get("name")])
                            .therapies(therapies)
                            .isSystemic(therapies.stream().anyMatch(Treatment::isSystemic))
                            .synonyms(Collections.emptySet())
                            .build();

            treatments = categories.contains(TreatmentCategory.SURGERY) ? Set.of(therapy, surgery(fields, parts)) : Set.of(therapy);

            String bestResponseString = ResourceFile.optionalString(parts[fields.get("bestResponse")]);
            TreatmentResponse bestResponse = (bestResponseString != null) ? TreatmentResponse.createFromString(bestResponseString) : null;
            String stopReasonDetail = ResourceFile.optionalString(parts[fields.get("stopReason")]);

            therapyHistoryDetails = ImmutableTherapyHistoryDetails.builder()
                    .stopYear(ResourceFile.optionalYear(parts[fields.get("stopYear")]))
                    .stopMonth(ResourceFile.optionalMonth(parts[fields.get("stopMonth")]))
                    .cycles(ResourceFile.optionalInteger(parts[fields.get("cycles")]))
                    .bestResponse(bestResponse)
                    .stopReasonDetail(stopReasonDetail)
                    .stopReason((stopReasonDetail != null) ? StopReason.createFromString(stopReasonDetail) : null)
                    // TODO: toxicities
                    .build();

        } else if (categories.contains(TreatmentCategory.SURGERY)) {
            treatments = Set.of(surgery(fields, parts));
            therapyHistoryDetails = null;
        } else if (categories.contains(TreatmentCategory.TRIAL)) {
            treatments = Set.of(otherTherapy(fields, parts, TreatmentCategory.TRIAL));
            therapyHistoryDetails = null;
        } else {
            throw new IllegalStateException("No treatment category resolved for input " + parts[fields.get("name")]);
        }

        return ImmutableTreatmentHistoryEntry.builder()
                .treatments(treatments)
                .startYear(ResourceFile.optionalYear(parts[fields.get("startYear")]))
                .startMonth(ResourceFile.optionalMonth(parts[fields.get("startMonth")]))
                .isTrial(categories.contains(TreatmentCategory.TRIAL))
                .trialAcronym(ResourceFile.optionalString(parts[fields.get("trialAcronym")]))
                .therapyHistoryDetails(therapyHistoryDetails)
                .build();
    }

    private static Treatment surgery(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableSurgery.builder().name(parts[fields.get("name")]).build();
    }

    // TODO: Set TRIAL category on all treatments when included
    private static Therapy chemotherapy(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableChemotherapy.builder()
                .name(parts[fields.get("name")])
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .drugs(Collections.emptySet())  // TODO
                .synonyms(Collections.emptySet())
                .build();
    }

    private static Therapy radiotherapy(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableRadiotherapy.builder()
                .name(parts[fields.get("name")])
                .addCategories(TreatmentCategory.RADIOTHERAPY)
                .synonyms(Collections.emptySet())
                .radioType(ResourceFile.optionalString(parts[fields.get("radioType")]))
                .build();
    }

    private static Therapy immunotherapy(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableImmunotherapy.builder()
                .name(parts[fields.get("name")])
                .addCategories(TreatmentCategory.IMMUNOTHERAPY)
                .drugs(Collections.emptySet())  // TODO
                .synonyms(Collections.emptySet())
                .build();
    }

    private static Therapy otherTherapy(@NotNull Map<String, Integer> fields, @NotNull String[] parts, TreatmentCategory category) {
        return ImmutableOtherTherapy.builder()
                .name(parts[fields.get("name")])
                .addCategories(category)
                .drugs(Collections.emptySet())  // TODO
                .synonyms(Collections.emptySet())
                .build();
    }
}
