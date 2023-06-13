package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.datamodel.ImmutableObservedToxicity
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import java.util.function.Function
import java.util.function.Predicate

class TreatmentHistoryEntryConfigFactory : CurationConfigFactory<TreatmentHistoryEntryConfig> {
    override fun create(fields: Map<String?, Int?>, parts: Array<String>): TreatmentHistoryEntryConfig {
        val ignore: Boolean = CurationUtil.isIgnoreString(parts[fields["name"]!!])
        return ImmutableTreatmentHistoryEntryConfig.builder()
            .input(parts[fields["input"]!!])
            .ignore(ignore)
            .curated(if (!ignore) curateObject(fields, parts) else null)
            .build()
    }

    companion object {
        private fun curateObject(fields: Map<String?, Int?>, parts: Array<String>): TreatmentHistoryEntry {
            val categories: Set<TreatmentCategory> = TreatmentCategoryResolver.fromStringList(parts[fields["category"]!!])
            val isTrial = categories.contains(TreatmentCategory.TRIAL)
            val therapies: Set<Therapy> = categories.stream()
                .filter(Predicate<TreatmentCategory> { cat: TreatmentCategory -> cat != TreatmentCategory.SURGERY && cat != TreatmentCategory.TRIAL })
                .map(
                    Function<TreatmentCategory, Therapy> { cat: TreatmentCategory ->
                        when (cat) {
                            TreatmentCategory.CHEMOTHERAPY -> return@map chemotherapy(fields, parts, isTrial)
                            TreatmentCategory.RADIOTHERAPY -> return@map radiotherapy(fields, parts, isTrial)
                            TreatmentCategory.IMMUNOTHERAPY -> return@map immunotherapy(fields, parts, isTrial)
                            else -> return@map otherTherapy(fields, parts, cat, isTrial)
                        }
                    }).collect(Collectors.toSet<Therapy>())
            val treatments: Set<Treatment>
            val therapyHistoryDetails: TherapyHistoryDetails?
            if (!therapies.isEmpty()) {
                val therapy: Treatment = if (therapies.size == 1) therapies.iterator().next() else ImmutableCombinedTherapy.builder()
                    .name(parts[fields["name"]!!])
                    .therapies(therapies)
                    .isSystemic(therapies.stream().anyMatch(Predicate<Therapy> { obj: Therapy -> obj.isSystemic() }))
                    .build()
                treatments = if (categories.contains(TreatmentCategory.SURGERY)) java.util.Set.of(
                    therapy,
                    surgery(fields, parts)
                ) else java.util.Set.of(therapy)
                val bestResponseString: String = ResourceFile.optionalString(parts[fields["bestResponse"]!!])
                val bestResponse: TreatmentResponse? =
                    if (bestResponseString != null) TreatmentResponse.createFromString(bestResponseString) else null
                val stopReasonDetail: String = ResourceFile.optionalString(parts[fields["stopReason"]!!])
                val toxicities: Set<ObservedToxicity>?
                toxicities = if (stopReasonDetail != null) {
                    if (stopReasonDetail.lowercase(ApplicationConfig.LOCALE).contains("toxicity")) java.util.Set.of(
                        ImmutableObservedToxicity.builder().name(stopReasonDetail).categories(
                            emptySet()
                        ).build()
                    ) else emptySet<ObservedToxicity>()
                } else {
                    null
                }
                therapyHistoryDetails = ImmutableTherapyHistoryDetails.builder()
                    .stopYear(ResourceFile.optionalInteger(parts[fields["stopYear"]!!]))
                    .stopMonth(ResourceFile.optionalInteger(parts[fields["stopMonth"]!!]))
                    .cycles(ResourceFile.optionalInteger(parts[fields["cycles"]!!]))
                    .bestResponse(bestResponse)
                    .stopReasonDetail(stopReasonDetail)
                    .stopReason(if (stopReasonDetail != null) StopReason.createFromString(stopReasonDetail) else null)
                    .toxicities(toxicities)
                    .build()
            } else if (categories.contains(TreatmentCategory.SURGERY)) {
                treatments = java.util.Set.of(surgery(fields, parts))
                therapyHistoryDetails = null
            } else if (categories.contains(TreatmentCategory.TRIAL)) {
                treatments = java.util.Set.of(otherTherapy(fields, parts, TreatmentCategory.TRIAL, true))
                therapyHistoryDetails = null
            } else {
                throw IllegalStateException("No treatment category resolved for input " + parts[fields["name"]!!])
            }
            return ImmutableTreatmentHistoryEntry.builder()
                .treatments(treatments)
                .startYear(ResourceFile.optionalInteger(parts[fields["startYear"]!!]))
                .startMonth(ResourceFile.optionalInteger(parts[fields["startMonth"]!!]))
                .isTrial(categories.contains(TreatmentCategory.TRIAL))
                .trialAcronym(ResourceFile.optionalString(parts[fields["trialAcronym"]!!]))
                .therapyHistoryDetails(therapyHistoryDetails)
                .build()
        }

        private fun surgery(fields: Map<String?, Int?>, parts: Array<String>): Treatment {
            return ImmutableSurgicalTreatment.builder().name(parts[fields["name"]!!]).build()
        }

        private fun chemotherapy(fields: Map<String?, Int?>, parts: Array<String>, isTrial: Boolean): Therapy {
            val builder: ImmutableChemotherapy.Builder = ImmutableChemotherapy.builder()
                .name(parts[fields["name"]!!])
                .isSystemic(true)
                .addCategories(TreatmentCategory.CHEMOTHERAPY)
                .drugs(emptySet())
            if (isTrial) {
                builder.addCategories(TreatmentCategory.TRIAL)
            }
            return builder.build()
        }

        private fun radiotherapy(fields: Map<String?, Int?>, parts: Array<String>, isTrial: Boolean): Therapy {
            val builder: ImmutableRadiotherapy.Builder = ImmutableRadiotherapy.builder().name(parts[fields["name"]!!]).isSystemic(false)
                .addCategories(TreatmentCategory.RADIOTHERAPY)
                .radioType(ResourceFile.optionalString(parts[fields["radioType"]!!]))
            if (isTrial) {
                builder.addCategories(TreatmentCategory.TRIAL)
            }
            return builder.build()
        }

        private fun immunotherapy(fields: Map<String?, Int?>, parts: Array<String>, isTrial: Boolean): Therapy {
            val builder: ImmutableImmunotherapy.Builder = ImmutableImmunotherapy.builder().name(parts[fields["name"]!!]).isSystemic(true)
                .addCategories(TreatmentCategory.IMMUNOTHERAPY)
                .drugs(emptySet())
            if (isTrial) {
                builder.addCategories(TreatmentCategory.TRIAL)
            }
            return builder.build()
        }

        private fun otherTherapy(
            fields: Map<String?, Int?>, parts: Array<String>, category: TreatmentCategory,
            isTrial: Boolean
        ): Therapy {
            val builder: ImmutableOtherTherapy.Builder = ImmutableOtherTherapy.builder()
                .name(parts[fields["name"]!!])
                .isSystemic(
                    !java.util.Set.of<TreatmentCategory>(TreatmentCategory.ABLATION, TreatmentCategory.TRANSPLANTATION).contains(category)
                )
                .addCategories(category)
                .drugs(emptySet())
            if (isTrial) {
                builder.addCategories(TreatmentCategory.TRIAL)
            }
            return builder.build()
        }
    }
}