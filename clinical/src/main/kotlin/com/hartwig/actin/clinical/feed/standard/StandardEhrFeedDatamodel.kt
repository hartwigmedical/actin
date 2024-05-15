package com.hartwig.actin.clinical.feed.standard

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.hartwig.actin.clinical.feed.JacksonSerializable
import java.time.LocalDate
import java.time.LocalDateTime


class RemoveNewlinesAndCarriageReturns : JsonDeserializer<String>() {
    override fun deserialize(p0: JsonParser, p1: DeserializationContext?): String {
        return p0.text?.replace("\n", "")?.replace("\r", "") ?: ""
    }
}

@JacksonSerializable
/**
 * Data class representing a patient record in the EHR
 * @property allergies List of allergies
 * @property bloodTransfusions List of blood transfusions
 * @property complications List of complications
 * @property labValues List of lab values
 * @property medications List of medications
 * @property molecularTestHistory List of molecular tests
 * @property patientDetails Details of the patient
 * @property priorOtherConditions List of prior other conditions
 * @property surgeries List of surgeries
 * @property toxicities List of toxicities
 * @property treatmentHistory List of treatment history
 * @property tumorDetails Details of the tumor
 * @property priorPrimaries List of prior primaries
 * @property measurements List of measurements
 * @property whoEvaluations List of WHO evaluations
 */
data class EhrPatientRecord(
    val allergies: List<EhrAllergy> = emptyList(),
    val bloodTransfusions: List<EhrBloodTransfusion> = emptyList(),
    val complications: List<EhrComplication> = emptyList(),
    val labValues: List<EhrLabValue> = emptyList(),
    val medications: List<EhrMedication>? = emptyList(),
    val molecularTestHistory: List<EhrMolecularTest> = emptyList(),
    val patientDetails: EhrPatientDetail,
    val priorOtherConditions: List<EhrPriorOtherCondition> = emptyList(),
    val surgeries: List<EhrSurgery> = emptyList(),
    val toxicities: List<EhrToxicity> = emptyList(),
    val treatmentHistory: List<EhrTreatmentHistory> = emptyList(),
    val tumorDetails: EhrTumorDetail,
    val priorPrimaries: List<EhrPriorPrimary> = emptyList(),
    val measurements: List<EhrMeasurement> = emptyList(),
    val whoEvaluations: List<EhrWhoEvaluation> = emptyList()
)

/**
 * Data class representing an allergy in the EHR
 * @property name Name of the allergy
 * @property startDate Start date of the allergy
 * @property endDate End date of the allergy
 * @property category Category of the allergy
 * @property severity Severity of the allergy
 * @property clinicalStatus Clinical status of the allergy
 * @property verificationStatus Verification status of the allergy
 */
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

/**
 * Data class representing a blood transfusion in the EHR
 * @property evaluationTime Evaluation time of the blood transfusion
 * @property product Product of the blood transfusion
 */
@JacksonSerializable
data class EhrBloodTransfusion(
    val evaluationTime: LocalDateTime,
    val product: String
)

/**
 * Data class representing a complication in the EHR
 * @property name Name of the complication
 * @property categories Categories of the complication
 * @property startDate Start date of the complication
 * @property endDate End date of the complication
 */
@JacksonSerializable
data class EhrComplication(
    val name: String,
    val categories: List<String> = emptyList(),
    val startDate: LocalDate,
    val endDate: LocalDate?
)

/**
 * Data class representing a lab value in the EHR
 * @property evaluationTime Evaluation time of the lab value
 * @property measure Measure of the lab value
 * @property measureCode Measure code of the lab value
 * @property value Value of the lab value
 * @property unit Unit of the lab value
 * @property refUpperBound Reference upper bound of the lab value
 * @property refLowerBound Reference lower bound of the lab value
 * @property comparator Comparator of the lab value
 * @property refFlag Reference flag of the lab value
 */
@JacksonSerializable
data class EhrLabValue(
    val evaluationTime: LocalDateTime,
    val measure: String,
    val measureCode: String,
    val value: Double,
    val unit: String?,
    val refUpperBound: Double,
    val refLowerBound: Double,
    val comparator: String?,
    val refFlag: String,
)

/**
 * Data class representing a medication in the EHR
 * @property name Name of the medication
 * @property atcCode ATC code of the medication
 * @property startDate Start date of the medication
 * @property endDate End date of the medication
 * @property administrationRoute Administration route of the medication
 * @property dosage Dosage of the medication
 * @property dosageUnit Dosage unit of the medication
 * @property frequency Frequency of the medication
 * @property frequencyUnit Frequency unit of the medication
 * @property periodBetweenDosagesValue Period between dosages value of the medication
 * @property periodBetweenDosagesUnit Period between dosages unit of the medication
 * @property administrationOnlyIfNeeded Administration only if needed of the medication
 * @property isTrial Is trial of the medication
 * @property isSelfCare Is self care of the medication
 */
@JacksonSerializable
data class EhrMedication(
    val name: String,
    val atcCode: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val administrationRoute: String?,
    val dosage: Double?,
    val dosageUnit: String?,
    val frequency: Double?,
    val frequencyUnit: String?,
    val periodBetweenDosagesValue: Double?,
    val periodBetweenDosagesUnit: String?,
    val administrationOnlyIfNeeded: Boolean?,
    val isTrial: Boolean,
    val isSelfCare: Boolean
)

/**
 * Data class representing a molecular test in the EHR
 * @property type Type of the molecular test
 * @property measure Measure of the molecular test
 * @property result Result of the molecular test
 * @property resultDate Result date of the molecular test
 */
@JacksonSerializable
data class EhrMolecularTest(
    val type: String,
    val measure: String?,
    val result: String,
    val resultDate: LocalDate,
)

/**
 * Data class representing the details of a patient in the EHR
 * @property birthYear Birth year of the patient
 * @property gender gender of the patient
 * @property registrationDate Registration date of the patient
 * @property hashedId Hashed ID of the patient
 */
@JacksonSerializable
data class EhrPatientDetail(
    val birthYear: Int,
    val gender: String,
    val registrationDate: LocalDate,
    val hashedId: String
)

/**
 * Data class representing a WHO evaluation in the EHR
 * @property status Status of the WHO evaluation
 * @property evaluationDate Evaluation date of the WHO evaluation
 */
@JacksonSerializable
data class EhrWhoEvaluation(
    val status: String,
    val evaluationDate: LocalDate
)

/**
 * Data class representing a prior other condition in the EHR
 * @property name Name of the prior other condition
 * @property category Category of the prior other condition
 * @property startDate Start date of the prior other condition
 * @property endDate End date of the prior other condition
 */
@JacksonSerializable
data class EhrPriorOtherCondition(
    @field:JsonDeserialize(using = RemoveNewlinesAndCarriageReturns::class)
    val name: String,
    val category: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate? = null
)

/**
 * Data class representing a surgery in the EHR
 * @property name Name of the surgery
 * @property endDate End date of the surgery
 * @property status Status of the surgery
 */
@JacksonSerializable
data class EhrSurgery(
    val name: String?,
    val endDate: LocalDate,
    val status: String
)

/**
 * Data class representing a toxicity in the EHR
 * @property name Name of the toxicity
 * @property categories Categories of the toxicity
 * @property evaluatedDate Evaluated date of the toxicity
 * @property grade Grade of the toxicity
 */
@JacksonSerializable
data class EhrToxicity(
    val name: String,
    val categories: List<String>,
    val evaluatedDate: LocalDate,
    val grade: Int
)

/**
 * Data class representing a treatment history in the EHR
 * @property treatmentName Name of the treatment
 * @property intention Intention of the treatment
 * @property startDate Start date of the treatment
 * @property endDate End date of the treatment
 * @property stopReason Stop reason of the treatment
 * @property stopReasonDate Stop reason date of the treatment
 * @property response Response of the treatment
 * @property responseDate Response date of the treatment
 * @property intendedCycles Intended cycles of the treatment
 * @property administeredCycles Administered cycles of the treatment
 * @property modifications Modifications of the treatment
 * @property administeredInStudy Administered in study of the treatment
 */
@JacksonSerializable
data class EhrTreatmentHistory(
    val treatmentName: String,
    val intention: String? = null,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val stopReason: String? = null,
    val stopReasonDate: LocalDate? = null,
    val response: String? = null,
    val responseDate: LocalDate? = null,
    val intendedCycles: Int,
    val administeredCycles: Int,
    val modifications: List<EhrTreatmentModification>? = null,
    val administeredInStudy: Boolean
)

/**
 * Data class representing a treatment modification in the EHR
 * @property name Name of the treatment modification
 * @property date Date of the treatment modification
 * @property administeredCycles Administered cycles of the treatment modification
 */
@JacksonSerializable
data class EhrTreatmentModification(
    val name: String,
    val date: LocalDate,
    val administeredCycles: Int,
)

/**
 * Data class representing a tumor detail in the EHR
 * @property diagnosisDate Diagnosis date of the tumor
 * @property tumorLocation Location of the tumor
 * @property tumorType Type of the tumor
 * @property tumorGradeDifferentiation Grade differentiation of the tumor
 * @property tumorStage Stage of the tumor
 * @property tumorStageDate Stage date of the tumor
 * @property measurableDisease Measurable disease of the tumor
 * @property measurableDiseaseDate Measurable disease date of the tumor
 * @property lesions Lesions of the tumor
 * @property lesionSite Lesion site of the tumor
 */
@JacksonSerializable
data class EhrTumorDetail(
    val diagnosisDate: LocalDate,
    val tumorLocation: String,
    val tumorType: String,
    val tumorGradeDifferentiation: String?,
    val tumorStage: String? = null,
    val tumorStageDate: LocalDate? = null,
    val measurableDisease: Boolean? = null,
    val measurableDiseaseDate: LocalDate? = null,
    val lesions: List<EhrLesion>? = null,
    val lesionSite: String? = null
)

/**
 * Data class representing a lesion in the EHR
 * @property location Location of the lesion
 * @property subLocation Sub location of the lesion
 * @property diagnosisDate Diagnosis date of the lesion
 */
@JacksonSerializable
data class EhrLesion(val location: String, val subLocation: String?, val diagnosisDate: LocalDate)

/**
 * Data class representing a prior primary in the EHR
 * @property diagnosisDate Diagnosis date of the prior primary
 * @property tumorLocation Location of the prior primary
 * @property tumorType Type of the prior primary
 * @property status Status of the prior primary
 * @property statusDate Status date of the prior primary
 */
@JacksonSerializable
data class EhrPriorPrimary(
    val diagnosisDate: LocalDate?,
    val tumorLocation: String,
    val tumorType: String,
    val status: String? = null,
    val statusDate: LocalDate? = null
)

/**
 * Data class representing a measurement in the EHR
 * @property date Date of the measurement
 * @property category Category of the measurement
 * @property subcategory Subcategory of the measurement
 * @property value Value of the measurement
 * @property unit Unit of the measurement
 */
@JacksonSerializable
data class EhrMeasurement(
    val date: LocalDate,
    val category: String,
    val subcategory: String?,
    val value: Double,
    val unit: String
)

enum class EhrGender{
    MALE,
    FEMALE,
    OTHER
}

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
    PLATELETS_APHERESIS
}

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
        fun fromString(input: String?): EhrLabUnit {
            return input?.let { inputString ->
                values().firstOrNull {
                    it.externalFormats.map { f -> f.lowercase() }.contains(inputString.lowercase())
                } ?: OTHER
            } ?: NONE
        }
    }
}

inline fun <reified T : Enum<T>> enumeratedInput(input: String) =
    enumValues<T>().firstOrNull { it.name == input.uppercase().replace(" ", "_") } ?: { enumValueOf<T>("OTHER") }
