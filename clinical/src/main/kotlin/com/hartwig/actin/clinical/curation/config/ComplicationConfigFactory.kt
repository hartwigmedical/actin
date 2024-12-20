package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class ComplicationConfigFactory(private val icdModel: IcdModel) : CurationConfigFactory<ComplicationConfig> {
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
        val (icdCodes, icdValidationErrors) = validateIcd(CurationCategory.COMPLICATION, input, "icd", fields, parts, icdModel)
        val (year, yearValidationErrors) = validateInteger(CurationCategory.COMPLICATION, input, "year", fields, parts)
        val (month, monthValidationErrors) = validateInteger(CurationCategory.COMPLICATION, input, "month", fields, parts)
        val curated = toCuratedComplication(icdCodes, fields, parts, year, month)
        return ValidatedCurationConfig(
            ComplicationConfig(
                input = input,
                ignore = ignore,
                impliesUnknownComplicationState = impliesUnknownComplicationState,
                curated = if (!ignore) curated else null
            ), impliesUnknownComplicationStateValidationErrors + icdValidationErrors + yearValidationErrors + monthValidationErrors
        )
    }

    private fun toCuratedComplication(icdCodes: Set<IcdCode>, fields: Map<String, Int>, parts: Array<String>, year: Int?, month: Int?) =
        Complication(
            name = parts[fields["name"]!!],
            icdCodes = icdCodes,
            year = year,
            month = month
        )
}
