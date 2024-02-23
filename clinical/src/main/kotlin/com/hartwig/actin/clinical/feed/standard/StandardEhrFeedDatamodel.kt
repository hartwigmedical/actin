package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.feed.JacksonSerializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

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
    val category: String,
    val severity: String,
    val clinicalStatus: String,
    val verificationStatus: String
)

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
    val product: String
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
    val measure: String,
    val measureCode: String,
    val value: Double,
    val unit: String,
    val refUpperBound: Double,
    val refLowerBound: Double,
    val comparator: String?,
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

fun String.toBase64(): String {
    return Base64.getEncoder().encodeToString(this.toByteArray(Charsets.UTF_8))
}

@JacksonSerializable
data class EhrPatientDetail(
    val birthYear: Int,
    val gender: String,
    val registrationDate: LocalDate,
    val hashedId: String
) {
    fun hashedIdBase64(): String {
        return hashedId.toBase64()
    }
}

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
    val status: String
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
    val intention: String?,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val stopReason: String?,
    val stopReasonDate: LocalDate?,
    val response: String?,
    val responseDate: LocalDate?,
    val intendedCycles: Int,
    val administeredCycles: Int,
    val modifications: List<EhrTreatmentModification>?,
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
    val tumorStage: String?,
    val tumorStageDate: LocalDate,
    val measurableDisease: Boolean,
    val measurableDiseaseDate: LocalDate,
    val lesions: List<EhrLesion>
)

@JacksonSerializable
data class EhrLesion(val location: String, val subLocation: String?, val diagnosisDate: LocalDate)

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
    val diagnosisDate: LocalDate?,
    val tumorLocation: String,
    val tumorType: String,
    val status: String,
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
    val category: String,
    val subcategory: String?,
    val value: Double,
    val unit: String
)

enum class EhrMeasurementCategory {
    HEART_RATE,
    PULSE_OXIMETRY,
    `NON-INVASIVE_BLOOD_PRESSURE`,
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
    KILOGRAMS,
    CENTIMETERS,
    KG_M2,
    OTHER
}

enum class EhrLabUnit(vararg val externalFormats: String) {
    NANOGRAMS_PER_LITER("ng/L"),
    NANOGRAMS_PER_MILLILITER("ng/mL"),
    MICROGRAMS_PER_LITER("ug/L"),
    MICROGRAMS_PER_MICROLITER("µg/µL"),
    MILLIGRAMS_PER_DECILITER("mg/dL"),
    MILLIGRAMS_PER_MILLIMOLE("mg/mmol"),
    MILLIGRAMS_PER_LITER("mg/L"),
    GRAMS_PER_DECILITER("g/dL"),
    GRAMS_PER_LITER("g/L"),
    GRAMS_PER_MOLE("g/mol"),
    KILOGRAMS_PER_LITER("kg/L"),
    MICROGRAMS_PER_GRAM("µg/g"),
    GRAMS("g"),
    PICOMOLES_PER_LITER("pmol/L"),
    NANOMOLES_PER_LITER("nmol/L"),
    MICROMOLES_PER_LITER("umol/L"),
    MILLIMOLES_PER_LITER("mmol/L"),
    MILLIMOLES_PER_MOLE("mmol/mol"),
    CELLS_PER_CUBIC_MILLIMETER("cells/mm3"),
    MILLIONS_PER_LITER("10E6/L"),
    MILLIONS_PER_MILLILITER("10E6/mL"),
    BILLIONS_PER_LITER("10E9/L"),
    TRILLIONS_PER_LITER("10E12/L"),
    MILLIUNITS_PER_LITER("mU/L"),
    UNITS_PER_LITER("U/L"),
    UNITS_PER_MILLILITER("U/mL"),
    KILOUNITS_PER_LITER("kU/L"),
    INTERNATIONAL_UNITS_PER_LITER("IU/L"),
    UNITS_OF_INR("INR"),
    NANOMOLES_PER_DAY("nmol/24h"),
    MILLIMOLES_PER_DAY("mmol/24h"),
    MILLIMETERS_PER_HOUR("mm/hr"),
    MILLILITERS_PER_MINUTE("mL/min"),
    FEMTOLITERS("fL"),
    MILLILITERS("mL"),
    KILO_PASCAL("kPa"),
    SECONDS("sec"),
    PERCENTAGE("%"),
    PERCENTAGE_OF_LEUKOCYTES("% of leukocytes"),
    PERCENTAGE_OF_T_CELLS("% of T-cells"),
    MILLI_OSMOLE_PER_KILOGRAM("mOsm/kg"),
    INTERNATIONAL_UNITS_PER_MILLILITER("IU/ml"),
    PRNT50("PRNT50"),
    OTHER,
    NONE("");

    companion object {
        fun fromString(input: String): EhrLabUnit {
            return values().firstOrNull { it.externalFormats.map { f -> f.lowercase() }.contains(input.lowercase()) } ?: OTHER
        }
    }
}

inline fun <reified T : Enum<T>> enumeratedInput(input: String) =
    enumValues<T>().firstOrNull { it.name == input.uppercase().replace(" ", "_") } ?: { enumValueOf<T>("OTHER") }


