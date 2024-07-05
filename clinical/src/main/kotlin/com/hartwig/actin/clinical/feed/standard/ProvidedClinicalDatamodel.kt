package com.hartwig.actin.clinical.feed.standard

import com.fasterxml.jackson.annotation.JsonPropertyDescription
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
data class ProvidedPatientRecord(
    val allergies: List<ProvidedAllergy> = emptyList(),
    val bloodTransfusions: List<ProvidedBloodTransfusion> = emptyList(),
    val complications: List<ProvidedComplication> = emptyList(),
    val labValues: List<ProvidedLabValue> = emptyList(),
    val medications: List<ProvidedMedication>? = emptyList(),
    val molecularTestHistory: List<ProvidedMolecularTest> = emptyList(),
    val patientDetails: ProvidedPatientDetail,
    val priorOtherConditions: List<ProvidedPriorOtherCondition> = emptyList(),
    val surgeries: List<ProvidedSurgery> = emptyList(),
    val toxicities: List<ProvidedToxicity> = emptyList(),
    val treatmentHistory: List<ProvidedTreatmentHistory> = emptyList(),
    val tumorDetails: ProvidedTumorDetail,
    val priorPrimaries: List<ProvidedPriorPrimary> = emptyList(),
    val measurements: List<ProvidedMeasurement> = emptyList(),
    val whoEvaluations: List<ProvidedWhoEvaluation> = emptyList()
)

@JacksonSerializable
data class ProvidedPatientDetail(
    @JsonPropertyDescription("Year of birth of this patient (eg. 1940)") val birthYear: Int,
    @JsonPropertyDescription("Year of birth of this patient (eg. Male, Female, Other)") val gender: String,
    @JsonPropertyDescription("Registration data of this patient with ACTIN") val registrationDate: LocalDate,
    @JsonPropertyDescription("Base64 encoded SHA-256 hash of source hospital's identifier.") val hashedId: String
)

@JacksonSerializable
data class ProvidedTumorDetail(
    @JsonPropertyDescription("Date of diagnosis") val diagnosisDate: LocalDate,
    @JsonPropertyDescription("Tumor localization details (eg. Lung)") val tumorLocation: String,
    @JsonPropertyDescription("Tumor type details (eg. Adenocarcinoma)") val tumorType: String,
    @JsonPropertyDescription("Tumor grade/differentiation details (eg. Poorly differentiated)") val tumorGradeDifferentiation: String?,
    @JsonPropertyDescription("Tumor stage (eg. 4, IV)") val tumorStage: String? = null,
    @JsonPropertyDescription("Date associated with tumor stage diagnosis") val tumorStageDate: LocalDate? = null,
    @JsonPropertyDescription("Has measurable disease") val measurableDisease: Boolean? = null,
    val measurableDiseaseDate: LocalDate? = null,
    val lesions: List<ProvidedLesion>? = null,
    @JsonPropertyDescription("Deprecated: currently use to store radiology report. Should move to lesions") val lesionSite: String? = null
)

@JacksonSerializable
data class ProvidedTreatmentHistory(
    @JsonPropertyDescription("Name of the treatment given (eg. Gemcitabine+Cisplatin)") val treatmentName: String,
    @JsonPropertyDescription("Intention of the treatment given (eg. Palliative)") val intention: String? = null,
    @JsonPropertyDescription("Date of the start of treatment") val startDate: LocalDate,
    @JsonPropertyDescription("Date of the end of treatment") val endDate: LocalDate? = null,
    @JsonPropertyDescription("Reason of stopping treatment (eg. Progressive disease)") val stopReason: String? = null,
    val stopReasonDate: LocalDate? = null,
    @JsonPropertyDescription("Response to treatment (eg. Partial Response)") val response: String? = null,
    val responseDate: LocalDate? = null,
    @JsonPropertyDescription("Intended number of cycles (eg. 6)") val intendedCycles: Int,
    @JsonPropertyDescription("Administered number of cycles (eg. 6)") val administeredCycles: Int,
    val modifications: List<ProvidedTreatmentModification>? = null,
    @JsonPropertyDescription("Treatment administered in clinical study") val administeredInStudy: Boolean
)

@JacksonSerializable
data class ProvidedTreatmentModification(
    @JsonPropertyDescription("Name of the modified treatment given (eg. Gemcitabine+Cisplatin)") val name: String,
    @JsonPropertyDescription("Date of the start of modification of treatment") val date: LocalDate,
    @JsonPropertyDescription("Modified number of cycles (eg. 6)") val administeredCycles: Int,
)

@JacksonSerializable
data class ProvidedMolecularTest(
    @JsonPropertyDescription("Type of test administered (eg. IHC)") val type: String,
    @JsonPropertyDescription("Measured gene or proteint (eg. HER2)") val measure: String?,
    @JsonPropertyDescription("Result of the test (eg. Negative/3+)") val result: String,
    val resultType: String,
    val resultDate: LocalDate,
)

@JacksonSerializable
data class ProvidedPriorPrimary(
    @JsonPropertyDescription("Diagnosis date") val diagnosisDate: LocalDate?,
    @JsonPropertyDescription("Tumor localization details (eg. Colon)") val tumorLocation: String,
    @JsonPropertyDescription("Tumor type details (eg. Carcinoma)") val tumorType: String,
    @JsonPropertyDescription("Observed status of tumor (eg. Active/Inactive") val status: String? = null,
    val statusDate: LocalDate? = null
)

@JacksonSerializable
data class ProvidedPriorOtherCondition(
    @field:JsonDeserialize(using = RemoveNewlinesAndCarriageReturns::class)
    @JsonPropertyDescription("Name of condition (eg. Pancreatis)") val name: String,
    @JsonPropertyDescription("Start date of condition") val startDate: LocalDate? = null,
    @JsonPropertyDescription("End date of condition if applicable") val endDate: LocalDate? = null
)

@JacksonSerializable
data class ProvidedComplication(
    @JsonPropertyDescription("Name of complication (eg. Ascites)") val name: String,
    @JsonPropertyDescription("Start date of complication") val startDate: LocalDate,
    @JsonPropertyDescription("End date of complication") val endDate: LocalDate?
)

@JacksonSerializable
data class ProvidedToxicity(
    @JsonPropertyDescription("Name of toxicity (eg. Neuropathy)") val name: String,
    @JsonPropertyDescription("Date of evaluation") val evaluatedDate: LocalDate,
    @JsonPropertyDescription("Grade (eg. 2)") val grade: Int
)

@JacksonSerializable
data class ProvidedMedication(
    @JsonPropertyDescription("Drug name (eg. Paracetamol)") val name: String,
    @JsonPropertyDescription("ATC code, required if not trial or self care (eg. N02BE01)") val atcCode: String?,
    @JsonPropertyDescription("Start date of use") val startDate: LocalDate?,
    @JsonPropertyDescription("End date of use") val endDate: LocalDate?,
    @JsonPropertyDescription("Administration route (eg. Oral)") val administrationRoute: String?,
    @JsonPropertyDescription("Dosage (eg. 500)") val dosage: Double?,
    @JsonPropertyDescription("Dosage unit (eg. mg)") val dosageUnit: String?,
    @JsonPropertyDescription("Frequency (eg. 2)") val frequency: Double?,
    @JsonPropertyDescription("Frequency unit (eg. day)") val frequencyUnit: String?,
    @JsonPropertyDescription("Period between dosages value ") val periodBetweenDosagesValue: Double?,
    @JsonPropertyDescription("Period between dosages unit") val periodBetweenDosagesUnit: String?,
    @JsonPropertyDescription("Administration only if needed") val administrationOnlyIfNeeded: Boolean?,
    @JsonPropertyDescription("Drug is still in clinical study") val isTrial: Boolean,
    @JsonPropertyDescription("Drug is administered as self-care") val isSelfCare: Boolean
)

@JacksonSerializable
data class ProvidedLabValue(
    @JsonPropertyDescription("Time of evaluation") val evaluationTime: LocalDateTime,
    @JsonPropertyDescription("Measure (eg. Carcinoembryonic antigen)") val measure: String,
    @JsonPropertyDescription("Measure code (eg. CEA)") val measureCode: String,
    @JsonPropertyDescription("Value (eg. 3.5)") val value: Double,
    @JsonPropertyDescription("Unit (eg. ug/L)") val unit: String?,
    @JsonPropertyDescription("Institutional upper reference limit") val refUpperBound: Double,
    @JsonPropertyDescription("Institutional lower reference limit") val refLowerBound: Double,
    @JsonPropertyDescription("Comparator if applicable (eg. >)") val comparator: String?
)

@JacksonSerializable
data class ProvidedBloodTransfusion(
    @JsonPropertyDescription("Time of transfusion") val evaluationTime: LocalDateTime,
    @JsonPropertyDescription("Product (eg. Thrombocyte concentrate)") val product: String
)

@JacksonSerializable
data class ProvidedMeasurement(
    @JsonPropertyDescription("Date of measurement") val date: LocalDate,
    @JsonPropertyDescription("Measurement category (eg. Body weight, Arterial blood pressure)") val category: String,
    @JsonPropertyDescription("Measurement subcategory (eg. Mean blood pressure)") val subcategory: String?,
    @JsonPropertyDescription("Value (eg. 70)") val value: Double,
    @JsonPropertyDescription("Unit (eg. kilograms)") val unit: String
)

@JacksonSerializable
data class ProvidedAllergy(
    @JsonPropertyDescription("Name of allergy (eg. Pembrolizumab)") val name: String,
    @JsonPropertyDescription("Start date of appearance of allergy") val startDate: LocalDate,
    @JsonPropertyDescription("End date of appearance of allergy, if applicable") val endDate: LocalDate?,
    @JsonPropertyDescription("Category of allergy (eg. medication)") val category: String,
    @JsonPropertyDescription("Severity of allergy (eg. low)") val severity: String,
    @JsonPropertyDescription("Clinical status of allergy (eg. active)") val clinicalStatus: String,
    @JsonPropertyDescription("Verification status of allergy (eg. confirmed)") val verificationStatus: String
)

@JacksonSerializable
data class ProvidedWhoEvaluation(
    @JsonPropertyDescription("WHO performance status (eg. 1)") val status: String,
    @JsonPropertyDescription("Date of WHO evaluation.") val evaluationDate: LocalDate
)

@JacksonSerializable
data class ProvidedSurgery(
    @JsonPropertyDescription("Name of surgery (eg. Diagnostics stomach)") val name: String?,
    @JsonPropertyDescription("Date of completion, if applicable.") val endDate: LocalDate,
    @JsonPropertyDescription("Status of surgery (eg. complete)") val status: String
)

@JacksonSerializable
data class ProvidedLesion(val location: String, val subLocation: String?, val diagnosisDate: LocalDate)

enum class ProvidedGender {
    MALE,
    FEMALE,
    OTHER
}

enum class ProvidedBloodTransfusionProduct {
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

enum class ProvidedMeasurementCategory {
    HEART_RATE,
    PULSE_OXIMETRY,
    `NON-INVASIVE_BLOOD_PRESSURE`,
    ARTERIAL_BLOOD_PRESSURE,
    BODY_WEIGHT,
    BODY_HEIGHT,
    BMI,
    OTHER
}

enum class ProvidedMeasurementSubcategory {
    NA,
    SYSTOLIC_BLOOD_PRESSURE,
    DIASTOLIC_BLOOD_PRESSURE,
    MEAN_BLOOD_PRESSURE,
    OTHER
}

enum class ProvidedMeasurementUnit {
    BPM,
    PERCENT,
    MMHG,
    KILOGRAMS,
    CENTIMETERS,
    KG_M2,
    OTHER
}

enum class ProvidedLabUnit(vararg val externalFormats: String) {
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
        fun fromString(input: String?): ProvidedLabUnit {
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
