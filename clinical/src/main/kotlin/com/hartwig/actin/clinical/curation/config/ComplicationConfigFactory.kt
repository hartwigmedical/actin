package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.Complication

class ComplicationConfigFactory : CurationConfigFactory<ComplicationConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<ComplicationConfig> {
        val ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!])
        val input = parts[fields["input"]!!]
        val (impliesUnknownComplicationState, impliesUnknownComplicationStateValidationErrors) = validateBoolean(
            CurationCategory.COMPLICATION,
            input,
            "impliesUnknownComplicationState",
            fields,
            parts
        )
        val (year, yearValidationErrors) = validateInteger(CurationCategory.COMPLICATION, input, "year", fields, parts)
        val (month, monthValidationErrors) = validateInteger(CurationCategory.COMPLICATION, input, "month", fields, parts)
        val curated = toCuratedComplication(fields, parts, year, month)
        return ValidatedCurationConfig(
            ComplicationConfig(
                input = input,
                ignore = ignore,
                impliesUnknownComplicationState = impliesUnknownComplicationState,
                curated = if (!ignore) curated else null
            ), impliesUnknownComplicationStateValidationErrors + yearValidationErrors + monthValidationErrors
        )
    }

    private fun toCuratedComplication(fields: Map<String, Int>, parts: Array<String>, year: Int?, month: Int?) = Complication(
        name = parts[fields["name"]!!],
        categories = CurationUtil.toCategories(parts[fields["categories"]!!]),
        year = year,
        month = month
    )
}
