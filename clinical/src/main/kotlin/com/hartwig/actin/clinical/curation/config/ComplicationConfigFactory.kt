package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.ImmutableComplication
import com.hartwig.actin.util.ResourceFile

class ComplicationConfigFactory : CurationConfigFactory<ComplicationConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): CurationConfigValidatedResponse<ComplicationConfig> {
        val ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!])
        val impliesUnknownComplicationState = parts[fields["impliesUnknownComplicationState"]!!].toValidatedBoolean()
        return CurationConfigValidatedResponse(
            ComplicationConfig(
                input = parts[fields["input"]!!],
                ignore = ignore,
                impliesUnknownComplicationState = impliesUnknownComplicationState,
                curated = if (!ignore) toCuratedComplication(fields, parts) else null
            ),
            impliesUnknownComplicationState?.let {
                listOf(
                    CurationConfigValidationError(
                        "impliesComplicationState had invalid input of [${parts[fields["impliesUnknownComplicationState"]!!]}]"
                    )
                )
            } ?: emptyList())
    }

    companion object {
        private fun toCuratedComplication(fields: Map<String, Int>, parts: Array<String>): Complication {
            return ImmutableComplication.builder()
                .name(parts[fields["name"]!!])
                .categories(CurationUtil.toCategories(parts[fields["categories"]!!]))
                .year(ResourceFile.optionalInteger(parts[fields["year"]!!]))
                .month(ResourceFile.optionalInteger(parts[fields["month"]!!]))
                .build()
        }
    }
}