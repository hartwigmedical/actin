package com.hartwig.actin.clinical.feed.emc.medication

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.hartwig.actin.clinical.feed.JacksonSerializable
import com.hartwig.actin.clinical.feed.emc.EuropeanDecimalDeserializer
import com.hartwig.actin.clinical.feed.emc.FeedEntry
import com.hartwig.actin.clinical.feed.emc.FeedSubjectDeserializer
import java.time.LocalDate

class ActiveDeserializer : JsonDeserializer<Boolean>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Boolean? {
        val activeField = p.text
        return if (activeField.equals("stopped", ignoreCase = true)) {
            false
        } else if (activeField == "active") {
            true
        } else {
            null
        }
    }
}

@JacksonSerializable
data class MedicationEntry(
    @JsonProperty("subject")
    @JsonDeserialize(using = FeedSubjectDeserializer::class)
    override val subject: String,

    @JsonProperty("code_text")
    val codeText: String,

    @JsonProperty("code5_ATC_code")
    val code5ATCCode: String,

    @JsonProperty("code5_ATC_display")
    val code5ATCDisplay: String,

    @JsonProperty("chemical_subgroup_display")
    val chemicalSubgroupDisplay: String,

    @JsonProperty("pharmacological_subgroup_display")
    val pharmacologicalSubgroupDisplay: String,

    @JsonProperty("therapeutic_subgroup_display")
    val therapeuticSubgroupDisplay: String,

    @JsonProperty("anatomical_main_group_display")
    val anatomicalMainGroupDisplay: String,

    @JsonProperty("dosageInstruction_route_display")
    val dosageInstructionRouteDisplay: String,

    @JsonProperty("dosageInstruction_doseQuantity_unit")
    val dosageInstructionDoseQuantityUnit: String,

    @JsonProperty("dosageInstruction_doseQuantity_value")
    @JsonDeserialize(using = EuropeanDecimalDeserializer::class)
    val dosageInstructionDoseQuantityValue: Double,

    @JsonProperty("dosageInstruction_frequency_unit")
    val dosageInstructionFrequencyUnit: String,

    @JsonProperty("dosageInstruction_frequency_value")
    @JsonDeserialize(using = EuropeanDecimalDeserializer::class)
    val dosageInstructionFrequencyValue: Double?,

    @JsonProperty("dosageInstruction_maxDosePerAdministration")
    @JsonDeserialize(using = EuropeanDecimalDeserializer::class)
    val dosageInstructionMaxDosePerAdministration: Double?,

    @JsonProperty("dosageInstruction_patientInstruction")
    val dosageInstructionPatientInstruction: String,

    @JsonProperty("dosageInstruction_asNeeded_display")
    val dosageInstructionAsNeededDisplay: String,

    @JsonProperty("dosageInstruction_period_between_dosages_unit")
    val dosageInstructionPeriodBetweenDosagesUnit: String,

    @JsonProperty("dosageInstruction_period_between_dosages_value")
    @JsonDeserialize(using = EuropeanDecimalDeserializer::class)
    val dosageInstructionPeriodBetweenDosagesValue: Double?,

    @JsonProperty("dosageInstruction_text")
    val dosageInstructionText: String,

    @JsonProperty("status")
    val status: String,

    @JsonProperty("active")
    @JsonDeserialize(using = ActiveDeserializer::class)
    val active: Boolean?,

    @JsonProperty("dosage_dose_value")
    val dosageDoseValue: String,

    @JsonProperty("dosage_rateQuantity_unit")
    val dosageRateQuantityUnit: String,

    @JsonProperty("dosage_rateQuantity_value")
    val dosageRateQuantityValue: Double,

    @JsonProperty("dosage_dose_unit_display_original")
    val dosageDoseUnitDisplayOriginal: String,

    @JsonProperty("dosage_dose_value_is_unreliable")
    val dosageDoseValueIsUnreliable: String,

    @JsonProperty("category_medicationRequestCategory_display_original")
    val categoryMedicationRequestCategoryDisplayOriginal: String,

    @JsonProperty("periodOfUse_valuePeriod_start")
    val periodOfUseValuePeriodStart: LocalDate,

    @JsonProperty("periodOfUse_valuePeriod_end")
    val periodOfUseValuePeriodEnd: LocalDate?,

    @JsonProperty("stopType_display")
    val stopTypeDisplay: String
) : FeedEntry