package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.extraction.BooleanValueParser
import com.hartwig.actin.datamodel.clinical.Ecg
import com.hartwig.actin.datamodel.clinical.EcgMeasure
import com.hartwig.actin.util.Either

class EcgConfigFactory : CurationConfigFactory<ComorbidityConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<ComorbidityConfig> {
        val input = parts[fields["input"]!!]
        val isQtcf = parts[fields["isQTCF"]!!] == "1"
        val (qtcfMeasure, qtcfValidationErrors) = extractMeasurement(input, "qtcf", isQtcf, parts, fields)
        val isJtc = parts[fields["isJTC"]!!] == "1"
        val (jtcMeasure, jtcValidationErrors) = extractMeasurement(input, "jtc", isJtc, parts, fields)
        val interpretation = parts[fields["interpretation"]!!].trim().ifEmpty { null }
        val hasSigAberrationLatestEcg = when (val parsed = BooleanValueParser.parseBoolean(interpretation)) {
            is Either.Right -> parsed.value
            else -> true
        }
        val ignore = interpretation == "NULL" || hasSigAberrationLatestEcg == null
        return ValidatedCurationConfig(
            ComorbidityConfig(
                input = input,
                ignore = ignore,
                curated = hasSigAberrationLatestEcg?.let {
                    Ecg(
                        name = interpretation,
                        qtcfMeasure = qtcfMeasure,
                        jtcMeasure = jtcMeasure
                    )
                },
            ), qtcfValidationErrors + jtcValidationErrors
        )
    }

    private fun extractMeasurement(
        input: String, measurementPrefix: String, isOfType: Boolean, parts: Array<String>, fields: Map<String, Int>
    ): Pair<EcgMeasure?, List<CurationConfigValidationError>> = if (isOfType) {
        val fieldName = "${measurementPrefix}Value"
        val (value, errors) = validateInteger(CurationCategory.ECG, input, fieldName, fields, parts)
        Pair(value?.let { EcgMeasure(it, parts[fields["${measurementPrefix}Unit"]!!]) }, errors)
    } else Pair(null, emptyList())
}