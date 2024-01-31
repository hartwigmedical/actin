package com.hartwig.actin.clinical.nki

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind.STRING
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.modules.SerializersModule
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = LocalDateTime::class)
object LocalDateTimeFromZonedDateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return ZonedDateTime.parse(decoder.decodeString(), formatter).toLocalDateTime()
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Serializer(forClass = LocalDate::class)
object LocalDateFromZonedDateSerializer : KSerializer<LocalDate> {

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString())
    }
}

inline fun <reified T : Enum<T>> enumSerializer(): KSerializer<T> {
    return object : KSerializer<T> {
        override val descriptor = PrimitiveSerialDescriptor(T::class.simpleName!!, STRING)

        override fun serialize(encoder: Encoder, value: T) {
            encoder.encodeString(value.name)
        }

        override fun deserialize(decoder: Decoder): T {
            return runCatching {
                enumValueOf<T>(
                    decoder.decodeString().uppercase().replace(" ", "_")
                )
            }.getOrDefault(enumValueOf<T>("UNKNOWN"))
        }
    }
}

val module = SerializersModule {
    contextual(LocalDate::class, LocalDateFromZonedDateSerializer)
    contextual(LocalDateTime::class, LocalDateTimeFromZonedDateTimeSerializer)
}

@OptIn(ExperimentalSerializationApi::class)
val feedJson = Json {
    serializersModule = module
    ignoreUnknownKeys = true
    namingStrategy = JsonNamingStrategy.SnakeCase
}

@Serializable
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
    val vitalFunctions: List<EhrMeasurement>,
    val whoEvaluations: List<EhrWhoEvaluation>
)

@Serializable
data class EhrAllergy(
    val name: String,
    @Contextual
    val startDate: LocalDate,
    @Contextual
    val endDate: LocalDate,
    val category: EhrAllergyCategory,
    val severity: EhrAllergySeverity,
    val clinicalStatus: EhrAllergyClinicalStatus,
    val verificationStatus: EhrAllergyVerificationStatus
)

@Serializable(with = EhrAllergyCategorySerializer::class)
enum class EhrAllergyCategory {
    MEDICATION,
    OTHER
}

object EhrAllergyCategorySerializer : KSerializer<EhrAllergyCategory> by enumSerializer()

@Serializable(with = EhrAllergySeveritySerializer::class)
enum class EhrAllergySeverity {
    MILD,
    MODERATE,
    SEVERE
}

object EhrAllergySeveritySerializer : KSerializer<EhrAllergySeverity> by enumSerializer()

@Serializable(with = EhrAllergyClinicalStatusSerializer::class)
enum class EhrAllergyClinicalStatus {
    ACTIVE,
    INACTIVE,
    RESOLVED
}

object EhrAllergyClinicalStatusSerializer : KSerializer<EhrAllergyClinicalStatus> by enumSerializer()

@Serializable(with = EhrAllergyVerificationStatusSerializer::class)
enum class EhrAllergyVerificationStatus {
    VERIFIED,
    UNVERIFIED
}

object EhrAllergyVerificationStatusSerializer : KSerializer<EhrAllergyVerificationStatus> by enumSerializer()

@Serializable
data class EhrBloodTransfusion(
    @Contextual
    val evaluationTime: LocalDateTime,
    val product: EhrBloodTransfusionProduct
)

@Serializable(with = EhrBloodTransfusionProductSerializer::class)
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

object EhrBloodTransfusionProductSerializer : KSerializer<EhrBloodTransfusionProduct> by enumSerializer()

@Serializable
data class EhrComplication(
    val name: String,
    val categories: List<String>,
    @Contextual
    val startDate: LocalDate,
    @Contextual
    val endDate: LocalDate
)

@Serializable
data class EhrLabValue(
    @Serializable(with = LocalDateTimeFromZonedDateTimeSerializer::class)
    val evaluationTime: LocalDateTime,
    val name: String,
    val code: String,
    val value: Double,
    val unit: String,
    val refUpperBound: Double,
    val refLowerBound: Double,
    val refFlag: String,
)

@Serializable
data class EhrMedication(
    val name: String,
    val atcCode: String,
    @Contextual
    val startDate: LocalDate,
    @Contextual
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

@Serializable
data class EhrPatientDetail(
    val birthYear: Int,
    val gender: EhrGender,
    @Contextual
    val registrationDate: LocalDate,
    val patientId: String,
    val hashedId: String
)

@Serializable(with = EhrGenderSerializer::class)
enum class EhrGender {
    MALE, FEMALE, OTHER
}

object EhrGenderSerializer : KSerializer<EhrGender> by enumSerializer()

@Serializable
data class EhrWhoEvaluation(
    val status: Int,
    @Contextual
    val evaluationDate: LocalDate
)

@Serializable
data class EhrPriorOtherCondition(
    val name: String,
    val category: String,
    @Contextual
    val startDate: LocalDate,
    @Contextual
    val endDate: LocalDate
)

@Serializable
data class EhrSurgery(
    val name: String,
    @Contextual
    val endDate: LocalDate,
    val status: EhrSurgeryStatus
)

@Serializable(with = EhrSurgeryStatusSerializer::class)
enum class EhrSurgeryStatus {
    PLANNED,
    IN_PROGRESS,
    FINISHED,
    CANCELLED,
    UNKNOWN,
    OTHER
}

object EhrSurgeryStatusSerializer : KSerializer<EhrSurgeryStatus> by enumSerializer()

@Serializable
data class EhrToxicity(
    val name: String,
    val categories: List<String>,
    @Contextual
    val evaluatedDate: LocalDate,
    val grade: Int
)

@Serializable
data class EhrTreatmentHistory(
    val treatmentName: String,
    val intention: EhrTreatmentIntention,
    @Contextual
    val startDate: LocalDate,
    @Contextual
    val endDate: LocalDate,
    val stopReason: EhrStopReason,
    @Contextual
    val stopReasonDate: LocalDate,
    val response: EhrTreatmentResponse,
    @Contextual
    val responseDate: LocalDate,
    val intendedCycles: Int,
    val administeredCycles: Int,
    val modifications: List<EhrTreatmentModification>,
    val administeredInStudy: Boolean
)

@Serializable(with = EhrTreatmentIntentionSerializer::class)
enum class EhrTreatmentIntention {
    ADJUVANT,
    NEOADJUVANT,
    INDUCTION,
    CONSOLIDATION,
    MAINTENANCE,
    PALLIATIVE,
    OTHER
}

object EhrTreatmentIntentionSerializer : KSerializer<EhrTreatmentIntention> by enumSerializer()

@Serializable(with = EhrStopReasonSerializer::class)
enum class EhrStopReason {
    PROGRESSIVE_DISEASE,
    TOXICITY,
    OTHER
}

object EhrStopReasonSerializer : KSerializer<EhrStopReason> by enumSerializer()

@Serializable(with = EhrTreatmentResponseSerializer::class)
enum class EhrTreatmentResponse {
    COMPLETE_RESPONSE,
    PARTIAL_RESPONSE,
    STABLE_DISEASE,
    PROGRESSIVE_DISEASE,
    NOT_EVALUATED,
    OTHER
}

object EhrTreatmentResponseSerializer : KSerializer<EhrTreatmentResponse> by enumSerializer()

@Serializable
data class EhrTreatmentModification(
    val name: String,
    @Contextual
    val date: LocalDate,
    val administeredCycles: Int,
)

@Serializable
data class EhrTumorDetail(
    @Contextual
    val diagnosisDate: LocalDate,
    val tumorLocation: String,
    val tumorType: String,
    val tumorGradeDifferentiation: String,
    val tumorStage: EhrTumorStage,
    @Contextual
    val tumorStageDate: LocalDate,
    val measurableDisease: Boolean,
    @Contextual
    val measurableDiseaseDate: LocalDate,
    val lesions: List<EhrLesion>
)

@Serializable
data class EhrLesion(val location: EhrLesionLocation, val sublocation: String?, @Contextual val diagnosisDate: LocalDate)

enum class EhrLesionLocation {
    BRAIN,
    CNS,
    BONE,
    LIVER,
    LUNG,
    LYMPH_NODE,
    OTHER
}

@Serializable
data class EhrPriorPrimary(
    @Contextual
    val diagnosisDate: LocalDate,
    val tumorLocation: String,
    val tumorType: String,
    val status: EhrTumorStatus,
    @Contextual
    val statusDate: LocalDate
)

@Serializable(with = EhrTumorStatusSerializer::class)
enum class EhrTumorStatus {
    ACTIVE,
    INACTIVE,
    EXPECTATIVE,
    OTHER
}

object EhrTumorStatusSerializer : KSerializer<EhrTumorStatus> by enumSerializer()

@Serializable(with = EhrTumorStageSerializer::class)
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

object EhrTumorStageSerializer : KSerializer<EhrTumorStage> by enumSerializer()

@Serializable
data class EhrMeasurement(
    @Contextual
    val date: LocalDate,
    val category: EhrMeasurementCategory,
    val subcategory: EhrMeasurementSubcategory,
    val value: Double,
    val unit: EhrVitalFunctionUnit
)

@Serializable(with = EhrVitalFunctionCategorySerializer::class)
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

object EhrVitalFunctionCategorySerializer : KSerializer<EhrMeasurementCategory> by enumSerializer()

@Serializable(with = EhrVitalFunctionSubcategorySerializer::class)
enum class EhrMeasurementSubcategory {
    NA,
    SYSTOLIC_BLOOD_PRESSURE,
    DIASTOLIC_BLOOD_PRESSURE,
    MEAN_BLOOD_PRESSURE,
    OTHER
}

object EhrVitalFunctionSubcategorySerializer : KSerializer<EhrMeasurementSubcategory> by enumSerializer()

@Serializable(with = EhrVitalFunctionUnitSerializer::class)
enum class EhrVitalFunctionUnit {
    BPM,
    PERCENT,
    MMHG,
    KG,
    CM,
    KG_M2,
    OTHER
}

object EhrVitalFunctionUnitSerializer : KSerializer<EhrVitalFunctionUnit> by enumSerializer()

