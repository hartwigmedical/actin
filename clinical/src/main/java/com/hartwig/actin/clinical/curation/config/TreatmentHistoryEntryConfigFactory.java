package com.hartwig.actin.clinical.curation.config;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.hartwig.actin.clinical.curation.CurationUtil;
import com.hartwig.actin.clinical.datamodel.ImmutableObservedToxicity;
import com.hartwig.actin.clinical.datamodel.ObservedToxicity;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableChemotherapy;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableCombinedTherapy;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableImmunotherapy;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableOtherTherapy;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableRadiotherapy;
import com.hartwig.actin.clinical.datamodel.treatment.ImmutableSurgicalTreatment;
import com.hartwig.actin.clinical.datamodel.treatment.Therapy;
import com.hartwig.actin.clinical.datamodel.treatment.Treatment;
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory;
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTherapyHistoryDetails;
import com.hartwig.actin.clinical.datamodel.treatment.history.ImmutableTreatmentHistoryEntry;
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason;
import com.hartwig.actin.clinical.datamodel.treatment.history.TherapyHistoryDetails;
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry;
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse;
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver;
import com.hartwig.actin.util.ApplicationConfig;
import com.hartwig.actin.util.ResourceFile;

import org.jetbrains.annotations.NotNull;

public class TreatmentHistoryEntryConfigFactory implements CurationConfigFactory<TreatmentHistoryEntryConfig> {

    @NotNull
    @Override
    public TreatmentHistoryEntryConfig create(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
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
        boolean isTrial = categories.contains(TreatmentCategory.TRIAL);

        Set<Therapy> therapies =
                categories.stream().filter(cat -> cat != TreatmentCategory.SURGERY && cat != TreatmentCategory.TRIAL).map(cat -> {
                    switch (cat) {
                        case CHEMOTHERAPY:
                            return chemotherapy(fields, parts, isTrial);
                        case RADIOTHERAPY:
                            return radiotherapy(fields, parts, isTrial);
                        case IMMUNOTHERAPY:
                            return immunotherapy(fields, parts, isTrial);
                        default:
                            return otherTherapy(fields, parts, cat, isTrial);
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

            Set<ObservedToxicity> toxicities;
            if (stopReasonDetail != null) {
                toxicities = stopReasonDetail.toLowerCase(ApplicationConfig.LOCALE).contains("toxicity")
                        ? Set.of(ImmutableObservedToxicity.builder().name(stopReasonDetail).categories(Collections.emptySet()).build())
                        : Collections.emptySet();
            } else {
                toxicities = null;
            }

            therapyHistoryDetails = ImmutableTherapyHistoryDetails.builder()
                    .stopYear(ResourceFile.optionalInteger(parts[fields.get("stopYear")]))
                    .stopMonth(ResourceFile.optionalInteger(parts[fields.get("stopMonth")]))
                    .cycles(ResourceFile.optionalInteger(parts[fields.get("cycles")]))
                    .bestResponse(bestResponse)
                    .stopReasonDetail(stopReasonDetail)
                    .stopReason((stopReasonDetail != null) ? StopReason.createFromString(stopReasonDetail) : null)
                    .toxicities(toxicities)
                    .build();

        } else if (categories.contains(TreatmentCategory.SURGERY)) {
            treatments = Set.of(surgery(fields, parts));
            therapyHistoryDetails = null;
        } else if (categories.contains(TreatmentCategory.TRIAL)) {
            treatments = Set.of(otherTherapy(fields, parts, TreatmentCategory.TRIAL, true));
            therapyHistoryDetails = null;
        } else {
            throw new IllegalStateException("No treatment category resolved for input " + parts[fields.get("name")]);
        }

        return ImmutableTreatmentHistoryEntry.builder()
                .treatments(treatments)
                .startYear(ResourceFile.optionalInteger(parts[fields.get("startYear")]))
                .startMonth(ResourceFile.optionalInteger(parts[fields.get("startMonth")]))
                .isTrial(categories.contains(TreatmentCategory.TRIAL))
                .trialAcronym(ResourceFile.optionalString(parts[fields.get("trialAcronym")]))
                .therapyHistoryDetails(therapyHistoryDetails)
                .build();
    }

    private static Treatment surgery(@NotNull Map<String, Integer> fields, @NotNull String[] parts) {
        return ImmutableSurgicalTreatment.builder().name(parts[fields.get("name")]).build();
    }

    private static Therapy chemotherapy(@NotNull Map<String, Integer> fields, @NotNull String[] parts, boolean isTrial) {
        ImmutableChemotherapy.Builder builder = ImmutableChemotherapy.builder()
                .name(parts[fields.get("name")])
                .isSystemic(true)
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .drugs(Collections.emptySet())
                .synonyms(Collections.emptySet());

        if (isTrial) {
            builder.addCategories(TreatmentCategory.TRIAL);
        }

        return builder.build();
    }

    private static Therapy radiotherapy(@NotNull Map<String, Integer> fields, @NotNull String[] parts, boolean isTrial) {
        ImmutableRadiotherapy.Builder builder = ImmutableRadiotherapy.builder()
                .name(parts[fields.get("name")])
                .addCategories(TreatmentCategory.RADIOTHERAPY)
                .synonyms(Collections.emptySet())
                .radioType(ResourceFile.optionalString(parts[fields.get("radioType")]));

        if (isTrial) {
            builder.addCategories(TreatmentCategory.TRIAL);
        }

        return builder.build();
    }

    private static Therapy immunotherapy(@NotNull Map<String, Integer> fields, @NotNull String[] parts, boolean isTrial) {
        ImmutableImmunotherapy.Builder builder = ImmutableImmunotherapy.builder()
                .name(parts[fields.get("name")])
                .addCategories(TreatmentCategory.IMMUNOTHERAPY)
                .drugs(Collections.emptySet())
                .synonyms(Collections.emptySet());

        if (isTrial) {
            builder.addCategories(TreatmentCategory.TRIAL);
        }

        return builder.build();
    }

    private static Therapy otherTherapy(@NotNull Map<String, Integer> fields, @NotNull String[] parts, TreatmentCategory category,
            boolean isTrial) {
        ImmutableOtherTherapy.Builder builder =
                ImmutableOtherTherapy.builder().name(parts[fields.get("name")]).addCategories(category).drugs(Collections.emptySet())
                .synonyms(Collections.emptySet());

        if (isTrial) {
            builder.addCategories(TreatmentCategory.TRIAL);
        }

        return builder.build();
    }
}
