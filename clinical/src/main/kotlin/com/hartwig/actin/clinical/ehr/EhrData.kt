package com.hartwig.actin.clinical.ehr

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.hartwig.actin.clinical.feed.JacksonSerializable
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.reflect.KClass


data class EnumeratedInput<T : Enum<T>>(val input: String, val acceptedValues: T)

open class EnumInputDeserializer<T : Enum<T>>(private val enumClass: KClass<T>) : JsonDeserializer<EnumeratedInput<T>>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): EnumeratedInput<T> {
        val input = p.text
        val acceptedValues = enumClass.java.enumConstants.firstOrNull { it.name == input.uppercase().replace(" ", "_") }
            ?: enumClass.java.enumConstants.first { it.name == "OTHER" }
        return EnumeratedInput(input, acceptedValues)
    }
}

@JacksonSerializable
data class EhrPatientRecord(
    val allergies: List<EhrAllergy>,
    val bloodTransfusions: List<EhrBloodTransfusion>,
    val complications: List<EhrComplication>,
    val labValues: List<EhrLabValue>,
    val medications: List<EhrMedication>,
    val patientDetails: EhrPatientDetail,
    val priorOtherConditions: List<EhrPriorOtherCondition>,
    val surgeries: List<EhrSurgery>,
    val toxicities: List<EhrToxicity>,
    val treatmentHistory: List<EhrTreatmentHistory>,
    val tumorDetails: EhrTumorDetail,
    val priorPrimaries: List<EhrPriorPrimary>,
    val measurements: List<EhrMeasurement>,
    val whoEvaluations: List<EhrWhoEvaluation>
)

@JacksonSerializable
data class EhrAllergy(
    val name: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val category: EnumeratedInput<EhrAllergyCategory>,
    @JsonDeserialize(using = EhrAllergySeverityDeserializer::class)
    val severity: EnumeratedInput<EhrAllergySeverity>,
    val clinicalStatus: EnumeratedInput<EhrAllergyClinicalStatus>,
    val verificationStatus: EnumeratedInput<EhrAllergyVerificationStatus>
)

class EhrAllergyCategoryDeserializer : EnumInputDeserializer<EhrAllergyCategory>(EhrAllergyCategory::class)
class EhrAllergySeverityDeserializer : EnumInputDeserializer<EhrAllergySeverity>(EhrAllergySeverity::class)
class EhrAllergyClinicalStatuDeserializer : EnumInputDeserializer<EhrAllergyClinicalStatus>(EhrAllergyClinicalStatus::class)
class EhrAllergyVerificationStatusDeserializer : EnumInputDeserializer<EhrAllergyVerificationStatus>(EhrAllergyVerificationStatus::class)

enum class EhrAllergyCategory {
    MEDICATION,
    OTHER
}

enum class EhrAllergySeverity {
    HIGH,
    LOW,
    UNKNOWN,
    OTHER
}

enum class EhrAllergyClinicalStatus {
    ACTIVE,
    INACTIVE,
    OTHER
}

enum class EhrAllergyVerificationStatus {
    CONFIRMED,
    UNCONFIRMED,
    OTHER
}

@JacksonSerializable
data class EhrBloodTransfusion(
    val evaluationTime: LocalDateTime,
    val product: EnumeratedInput<EhrBloodTransfusionProduct>
)

enum class EhrBloodTransfusionProduct {
    PLASMA_A,
    PLASMA_B,
    PLASMA_O,
    PLASMA_AB,
    PLATELETS_POOLED,
    PLATELETS_POOLED_RADIATED,
    ERYTHROCYTES_RADIATED,
    APHERESIS_PLASMA,
    ERTHROCYTES_FILTERED,
    OTHER,
    PLATELETS_APHERESIS
}

@JacksonSerializable
data class EhrComplication(
    val name: String,
    val categories: List<String>,
    val startDate: LocalDate,
    val endDate: LocalDate
)

@JacksonSerializable
data class EhrLabValue(
    val evaluationTime: LocalDateTime,
    val name: String,
    val code: String,
    val value: Double,
    val unit: String,
    val refUpperBound: Double,
    val refLowerBound: Double,
    val refFlag: String,
)

@JacksonSerializable
data class EhrMedication(
    val name: String,
    val atcCode: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val administrationRoute: String,
    val dosage: Double,
    val dosageUnit: String,
    val frequency: Double,
    val frequencyUnit: String,
    val periodBetweenDosagesValue: Double,
    val periodBetweenDosagesUnit: String,
    val administrationOnlyIfNeeded: Boolean,
    val isTrial: Boolean,
    val isSelfCare: Boolean
)

@JacksonSerializable
data class EhrPatientDetail(
    val birthYear: Int,
    val gender: EnumeratedInput<EhrGender>,
    val registrationDate: LocalDate,
    val patientId: String,
    val hashedId: String
)

enum class EhrGender {
    MALE, FEMALE, OTHER
}

@JacksonSerializable
data class EhrWhoEvaluation(
    val status: Int,
    val evaluationDate: LocalDate
)

@JacksonSerializable
data class EhrPriorOtherCondition(
    val name: String,
    val category: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

@JacksonSerializable
data class EhrSurgery(
    val name: String,
    val endDate: LocalDate,
    val status: EnumeratedInput<EhrSurgeryStatus>
)

enum class EhrSurgeryStatus {
    PLANNED,
    IN_PROGRESS,
    FINISHED,
    CANCELLED,
    UNKNOWN,
    OTHER
}

@JacksonSerializable
data class EhrToxicity(
    val name: String,
    val categories: List<String>,
    val evaluatedDate: LocalDate,
    val grade: Int
)

@JacksonSerializable
data class EhrTreatmentHistory(
    val treatmentName: String,
    val intention: EnumeratedInput<EhrTreatmentIntention>,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val stopReason: EnumeratedInput<EhrStopReason>,
    val stopReasonDate: LocalDate,
    val response: EnumeratedInput<EhrTreatmentResponse>,
    val responseDate: LocalDate,
    val intendedCycles: Int,
    val administeredCycles: Int,
    val modifications: List<EhrTreatmentModification>,
    val administeredInStudy: Boolean
)

enum class EhrTreatmentIntention {
    ADJUVANT,
    NEOADJUVANT,
    INDUCTION,
    CONSOLIDATION,
    MAINTENANCE,
    PALLIATIVE,
    OTHER
}

enum class EhrStopReason {
    PROGRESSIVE_DISEASE,
    TOXICITY,
    OTHER
}

enum class EhrTreatmentResponse {
    COMPLETE_RESPONSE,
    PARTIAL_RESPONSE,
    STABLE_DISEASE,
    PROGRESSIVE_DISEASE,
    NOT_EVALUATED,
    OTHER
}

@JacksonSerializable
data class EhrTreatmentModification(
    val name: String,
    val date: LocalDate,
    val administeredCycles: Int,
)

@JacksonSerializable
data class EhrTumorDetail(
    val diagnosisDate: LocalDate,
    val tumorLocation: String,
    val tumorType: String,
    val tumorGradeDifferentiation: String,
    val tumorStage: EnumeratedInput<EhrTumorStage>,
    val tumorStageDate: LocalDate,
    val measurableDisease: Boolean,
    val measurableDiseaseDate: LocalDate,
    val lesions: List<EhrLesion>
)

@JacksonSerializable
data class EhrLesion(val location: EnumeratedInput<EhrLesionLocation>, val subLocation: String?, val diagnosisDate: LocalDate)

enum class EhrLesionLocation {
    BRAIN,
    CNS,
    BONE,
    LIVER,
    LUNG,
    LYMPH_NODE,
    OTHER
}

@JacksonSerializable
data class EhrPriorPrimary(
    val diagnosisDate: LocalDate,
    val tumorLocation: String,
    val tumorType: String,
    val status: EnumeratedInput<EhrTumorStatus>,
    val statusDate: LocalDate
)

enum class EhrTumorStatus {
    ACTIVE,
    INACTIVE,
    EXPECTATIVE,
    OTHER
}

enum class EhrTumorStage {
    I,
    II,
    IIA,
    IIB,
    III,
    IIIA,
    IIIB,
    IIIC,
    IV,
    OTHER
}

@JacksonSerializable
data class EhrMeasurement(
    val date: LocalDate,
    val category: EnumeratedInput<EhrMeasurementCategory>,
    val subcategory: EnumeratedInput<EhrMeasurementSubcategory>,
    val value: Double,
    val unit: EnumeratedInput<EhrMeasurementUnit>
)

enum class EhrMeasurementCategory {
    HEART_RATE,
    PULSE_OXIMETRY,
    NON_INVASIVE_BLOOD_PRESSURE,
    ARTERIAL_BLOOD_PRESSURE,
    BODY_WEIGHT,
    BODY_HEIGHT,
    BMI,
    OTHER
}

enum class EhrMeasurementSubcategory {
    NA,
    SYSTOLIC_BLOOD_PRESSURE,
    DIASTOLIC_BLOOD_PRESSURE,
    MEAN_BLOOD_PRESSURE,
    OTHER
}

enum class EhrMeasurementUnit {
    BPM,
    PERCENT,
    MMHG,
    KG,
    CM,
    KG_M2,
    OTHER
}


