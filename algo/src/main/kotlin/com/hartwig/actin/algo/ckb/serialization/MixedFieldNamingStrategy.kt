package com.hartwig.actin.algo.ckb.serialization

import com.google.gson.FieldNamingPolicy
import com.google.gson.FieldNamingStrategy
import java.lang.reflect.Field

class MixedFieldNamingStrategy : FieldNamingStrategy {

    override fun translateName(field: Field): String {
        if (field.name == "id2") {
            throw IllegalStateException("Unexpected field in java datamodel found: $field")
        }

        if (SNAKE_CASE_FIELDS.contains(field.name)) {
            return FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES.translateName(field)
        }

        return FieldNamingPolicy.IDENTITY.translateName(field)
    }

    companion object {
        private val SNAKE_CASE_FIELDS =
            setOf("trialReferences", "cancerStage", "diseaseAssessment", "diseaseAssessmentCriteria", "therapeuticSetting")
    }

}