package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.datamodel.treatment.ImmutablePriorTumorTreatment

class OncologicalHistoryConfigFactory : CurationConfigFactory<OncologicalHistoryConfig> {
    override fun create(fields: Map<String?, Int?>, parts: Array<String>): OncologicalHistoryConfig {
        val ignore: Boolean = CurationUtil.isIgnoreString(parts[fields["name"]!!])
        return ImmutableOncologicalHistoryConfig.builder()
            .input(parts[fields["input"]!!])
            .ignore(ignore)
            .curated(if (!ignore) curateObject(fields, parts) else null)
            .build()
    }

    companion object {
        private fun curateObject(fields: Map<String?, Int?>, parts: Array<String>): PriorTumorTreatment {
            return ImmutablePriorTumorTreatment.builder()
                .name(parts[fields["name"]!!])
                .startYear(ResourceFile.optionalInteger(parts[fields["startYear"]!!]))
                .startMonth(ResourceFile.optionalInteger(parts[fields["startMonth"]!!]))
                .stopYear(ResourceFile.optionalInteger(parts[fields["stopYear"]!!]))
                .stopMonth(ResourceFile.optionalInteger(parts[fields["stopMonth"]!!]))
                .cycles(ResourceFile.optionalInteger(parts[fields["cycles"]!!]))
                .bestResponse(ResourceFile.optionalString(parts[fields["bestResponse"]!!]))
                .stopReason(ResourceFile.optionalString(parts[fields["stopReason"]!!]))
                .categories(TreatmentCategoryResolver.fromStringList(parts[fields["category"]!!]))
                .isSystemic(ResourceFile.bool(parts[fields["isSystemic"]!!]))
                .chemoType(ResourceFile.optionalString(parts[fields["chemoType"]!!]))
                .immunoType(ResourceFile.optionalString(parts[fields["immunoType"]!!]))
                .targetedType(ResourceFile.optionalString(parts[fields["targetedType"]!!]))
                .hormoneType(ResourceFile.optionalString(parts[fields["hormoneType"]!!]))
                .radioType(ResourceFile.optionalString(parts[fields["radioType"]!!]))
                .carTType(ResourceFile.optionalString(parts[fields["carTType"]!!]))
                .transplantType(ResourceFile.optionalString(parts[fields["transplantType"]!!]))
                .supportiveType(ResourceFile.optionalString(parts[fields["supportiveType"]!!]))
                .trialAcronym(ResourceFile.optionalString(parts[fields["trialAcronym"]!!]))
                .ablationType(ResourceFile.optionalString(parts[fields["ablationType"]!!]))
                .build()
        }
    }
}