package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory


class ComorbidityConfigFactory(private val icdModel: IcdModel) : CurationConfigFactory<ComorbidityConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<ComorbidityConfig> {
        val type = parts[fields["type"]!!]
        val input = parts[fields["input"]!!]
        val ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!])
        val curationConfig = when (type) {
            "complication" -> ComplicationConfigFactory(icdModel).create(fields, parts)
            "other_condition" -> OtherConditionConfigFactory(icdModel).create(fields, parts)
            "infection" -> InfectionConfigFactory(icdModel).create(fields, parts)
            "intolerance" -> IntoleranceConfigFactory(icdModel).create(fields, parts)
            "ecg" -> EcgConfigFactory(icdModel).create(fields, parts)
            "toxicity" -> ToxicityConfigFactory(icdModel).create(fields, parts)
            else -> generateNoTypeError(type, input, ignore)
        }
        return curationConfig
    }


    fun generateNoTypeError(type: String, input: String, ignore: Boolean): ValidatedCurationConfig<ComorbidityConfig> {
        return ValidatedCurationConfig(
            ComorbidityConfig(
                input = input,
                ignore = ignore,
                curated = null
            ), listOf(
                CurationConfigValidationError(
                    CurationCategory.COMORBIDITY,
                    input,
                    "type",
                    type,
                    String::class.java.simpleName,
                    "Unknown comorbidity type: $type. Expected one of: complication, other_condition, infection."
                )
            )
        )
    }
}