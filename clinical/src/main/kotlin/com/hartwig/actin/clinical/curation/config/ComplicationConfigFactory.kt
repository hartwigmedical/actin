package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.ImmutableComplication

class ComplicationConfigFactory : CurationConfigFactory<ComplicationConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<ComplicationConfig> {
        val ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!])
        val impliesUnknownComplicationState = parts[fields["impliesUnknownComplicationState"]!!].toValidatedBoolean()
        val (curatedComplication, complicationValidationErrors) = toCuratedComplication(fields, parts)
        return createValidatedCurationConfig(
            ignore,
            impliesUnknownComplicationState,
            curatedComplication,
            complicationValidationErrors,
            fields,
            parts
        )
    }

    private fun createValidatedCurationConfig(
        ignore: Boolean,
        impliesUnknownComplicationState: Boolean?,
        curatedComplication: Complication?,
        complicationValidationErrors: List<CurationConfigValidationError>,
        fields: Map<String, Int>,
        parts: Array<String>
    ): ValidatedCurationConfig<ComplicationConfig> {

        val complicationConfig = ComplicationConfig(
            input = parts[fields["input"]!!],
            ignore = ignore,
            impliesUnknownComplicationState = impliesUnknownComplicationState,
            curated = if (!ignore) curatedComplication else null
        )

        val errors = if (impliesUnknownComplicationState == null)
            listOf(
                CurationConfigValidationError(
                    "impliesComplicationState had invalid value of '${parts[fields["impliesUnknownComplicationState"]!!]}' for input " +
                            "'${parts[fields["input"]!!]}"
                )
            ) + complicationValidationErrors
        else
            complicationValidationErrors

        return ValidatedCurationConfig(complicationConfig, errors)
    }
}

private fun toCuratedComplication(
    fields: Map<String, Int>,
    parts: Array<String>
): Pair<Complication, List<CurationConfigValidationError>> {
    val (year, yearValidationErrors) = validateInteger("year", fields, parts)
    val (month, monthValidationErrors) = validateInteger("month", fields, parts)
    return ImmutableComplication.builder()
        .name(parts[fields["name"]!!])
        .categories(CurationUtil.toCategories(parts[fields["categories"]!!]))
        .year(year)
        .month(month)
        .build() to (yearValidationErrors + monthValidationErrors)
}
