package com.hartwig.actin.clinical.nki

import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
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
    val molecularTestHistory: List<EhrMolecularTestHistory>,
    val patientDetails: EhrPatientDetail,
    val priorOtherConditions: List<EhrPriorOtherCondition>,
    val surgeries: List<EhrSurgery>,
    val toxicities: List<EhrToxicity>,
    val treatmentHistory: List<EhrTreatmentHistory>,
    val tumorDetails: EhrTumorDetail,
    val priorPrimaries: List<EhrPriorPrimary>,
    val vitalFunctions: List<EhrVitalFunction>,
    val bodyWeights: List<EhrBodyWeight>,
    val whoEvaluations: List<EhrWhoEvaluation>
)

@Serializable
data class EhrAllergy(
    val description: String,
    @Contextual
    val startDate: LocalDate,
    @Contextual
    val endDate: LocalDate,
    val category: String,
    val severity: String,
    val clinicalStatus: String,
    val verificationStatus: String
)

@Serializable
data class EhrBloodTransfusion(
    @Contextual
    val dateTime: LocalDateTime,
    val bloodType: String,
    val product: String
)

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
    val dateTime: LocalDateTime,
    val measure: String,
    val code: String,
    val category: String,
    val value: Double,
    val unit: String,
    val refRange: String,
    val refFlag: String,
    val note: String
)

@Serializable
data class EhrMedication(
    val drugName: String,
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
data class EhrMolecularTestHistory(
    val type: String,
    val measure: String,
    val result: String,
    @Contextual
    val resultDate: LocalDate,
    val biopsyLocation: String,
    @Contextual
    val biopsyDate: LocalDate
)

@Serializable
data class EhrPatientDetail(
    val birthYear: Int,
    val gender: String,
    @Contextual
    val registrationDate: LocalDate,
    val patientId: String,
    val hashedId: String
)

@Serializable
data class EhrWhoEvaluation(
    val status: Int,
    @Contextual
    val evaluationDate: LocalDate
)

@Serializable
data class EhrPriorOtherCondition(
    val diagnosis: String,
    val treatment: String,
    val remarks: String,
    @Contextual
    val startDate: LocalDate,
    @Contextual
    val endDate: LocalDate
)

@Serializable
data class EhrSurgery(
    val surgeryName: String,
    @Contextual
    val endDate: LocalDate,
    val status: String
)

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
    val intention: String,
    @Contextual
    val startDate: LocalDate,
    @Contextual
    val endDate: LocalDate,
    val stopReason: String,
    @Contextual
    val stopReasonDate: LocalDate,
    val response: String,
    @Contextual
    val responseDate: LocalDate,
    val intendedCycles: Int,
    val administeredCycles: Int,
    val modifications: List<EhrTreatmentModification>,
    val administeredInStudy: Boolean
)

@Serializable
data class EhrTreatmentModification(
    val treatmentName: String,
    @Contextual
    val date: LocalDate,
    val administeredCycles: Int,
    val note: String
)

@Serializable
data class EhrTumorDetail(
    @Contextual
    val diagnosisDate: LocalDate,
    val tumorLocalization: String,
    val tumorTypeDetails: String,
    val tumorGradeDifferentationDetails: String,
    val tumorStage: String,
    @Contextual
    val tumorStageDate: LocalDate,
    val lesionSite: String,
    @Contextual
    val lesionSiteDate: LocalDate,
    val measurableDisease: Boolean,
    @Contextual
    val measurableDiseaseDate: LocalDate
)

@Serializable
data class EhrPriorPrimary(
    @Contextual
    val diagnosisDate: LocalDate,
    val tumorLocalization: String,
    val tumorTypeDetails: String,
    val statusDetails: String,
    @Contextual
    val statusDetailsDate: LocalDate
)

@Serializable
data class EhrVitalFunction(
    @Contextual
    val date: LocalDate,
    val measure: String,
    val subcategory: String,
    val value: Double,
    val unit: String
)

@Serializable
data class EhrBodyWeight(
    @Contextual
    val date: LocalDate,
    val value: Double,
    val unit: String
)