package com.hartwig.actin.datamodel.clinical.provided

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import java.time.LocalDate
import java.time.LocalDateTime


class RemoveNewlinesAndCarriageReturns : JsonDeserializer<String>() {
    override fun deserialize(p0: JsonParser, p1: DeserializationContext?): String {
        return p0.text?.replace("\n", "")?.replace("\r", "") ?: ""
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class Description(val value: String)

@JacksonSerializable
data class ProvidedPatientRecord(
    val allergies: List<ProvidedAllergy> = emptyList(),
    val bloodTransfusions: List<ProvidedBloodTransfusion> = emptyList(),
    val labValues: List<ProvidedLabValue> = emptyList(),
    val medications: List<ProvidedMedication>? = emptyList(),
    val patientDetails: ProvidedPatientDetail,
    val priorOtherConditions: List<ProvidedOtherCondition> = emptyList(),
    val tumorDetails: ProvidedTumorDetail,
    val measurements: List<ProvidedMeasurement> = emptyList(),
    val whoEvaluations: List<ProvidedWhoEvaluation> = emptyList()
)

@JacksonSerializable
data class ProvidedPatientDetail(
    @Description("Year of birth of this patient (eg. 1940)")
    val birthYear: Int,
    @Description("Year of birth of this patient (eg. Male, Female, Other)")
    val gender: String,
    @Description("Registration data of this patient with ACTIN")
    val registrationDate: LocalDate,
    @Description("Base64 encoded SHA-256 hash of source hospital's identifier")
    val hashedId: String,
    @Description("Flag to indicate there is pending Hartwig analysis data for this patient")
    val hartwigMolecularDataExpected: Boolean,
    @Description("Hospital specific Patient Id")
    val hospitalPatientId: String? = null
)

@JacksonSerializable
data class ProvidedTumorDetail(
    @Description("Tumor localization details (eg. Lung)")
    val tumorLocation: String,
    @Description("Tumor type details (eg. Adenocarcinoma)")
    val tumorType: String,
    @Description("Tumor grade/differentiation details (eg. Poorly differentiated)")
    val tumorGradeDifferentiation: String?,
    @Description("Tumor stage (roman numeral, eg. IV)")
    val tumorStage: String? = null,
    @Description("Date associated with tumor stage diagnosis")
    val tumorStageDate: LocalDate? = null,
    @Description("Has measurable disease")
    val measurableDisease: Boolean? = null,
    val measurableDiseaseDate: LocalDate? = null,
    val lesions: ProvidedLesionDetail? = null,
    @Description("Raw pathology reports")
    val pathology: List<ProvidedPathologyReport>? = null,
)

@JacksonSerializable
data class ProvidedPathologyReport(
    @Description("Tissue Id")
    val tissueId: String? = null,
    @Description("Indication on whether the report was requested")
    val reportRequested: Boolean,
    @Description("Lab that performed the report")
    val lab: String,
    @Description("Diagnosis written in the pathology reports")
    val diagnosis: String,
    @Description("Date of tissue collection - present only when the source is internal")
    val tissueDate: LocalDate? = null,
    @Description("Latest date of report authorization - present only when the source is internal")
    val authorisationDate: LocalDate? = null,
    @Description("Date of the report (not clear what this data represents) - used when tissueDate and authorisationDate and not known")
    val reportDate: LocalDate? = null,
    @Description("Raw pathology report of molecular test results")
    val rawPathologyReport: String
)

interface ProvidedComorbidity {
    val name: String
    val startDate: LocalDate?
}

@JacksonSerializable
data class ProvidedOtherCondition(
    @field:JsonDeserialize(using = RemoveNewlinesAndCarriageReturns::class)
    @Description("Name of condition (eg. Pancreatis)")
    override val name: String,
    @Description("Start date of condition")
    override val startDate: LocalDate? = null,
    @Description("End date of condition if applicable")
    val endDate: LocalDate? = null
) : ProvidedComorbidity

@JacksonSerializable
data class ProvidedMedication(
    @Description("Drug name (eg. Paracetamol)")
    val name: String,
    @Description("ATC code, required if not trial or self care (eg. N02BE01)")
    val atcCode: String?,
    @Description("Start date of use")
    val startDate: LocalDate?,
    @Description("End date of use")
    val endDate: LocalDate?,
    @Description("Administration route (eg. Oral)")
    val administrationRoute: String?,
    @Description("Dosage (eg. 500)")
    val dosage: Double?,
    @Description("Dosage unit (eg. mg)")
    val dosageUnit: String?,
    @Description("Frequency (eg. 2)")
    val frequency: Double?,
    @Description("Frequency unit (eg. day)")
    val frequencyUnit: String?,
    @Description("Period between dosages value ")
    val periodBetweenDosagesValue: Double?,
    @Description("Period between dosages unit")
    val periodBetweenDosagesUnit: String?,
    @Description("Administration only if needed")
    val administrationOnlyIfNeeded: Boolean?,
    @Description("Drug is still in clinical study")
    val isTrial: Boolean,
    @Description("Drug is administered as self-care")
    val isSelfCare: Boolean
)

@JacksonSerializable
data class ProvidedLabValue(
    @Description("Time of evaluation")
    val evaluationTime: LocalDateTime,
    @Description("Measure (eg. Carcinoembryonic antigen)")
    val measure: String,
    @Description("Measure code (eg. CEA)")
    val measureCode: String,
    @Description("Value (eg. 3.5)")
    val value: Double,
    @Description("Unit (eg. ug/L)")
    val unit: String?,
    @Description("Institutional upper reference limit")
    val refUpperBound: Double?,
    @Description("Institutional lower reference limit")
    val refLowerBound: Double?,
    @Description("Comparator if applicable (eg. >)")
    val comparator: String?
)

@JacksonSerializable
data class ProvidedBloodTransfusion(
    @Description("Time of transfusion")
    val evaluationTime: LocalDateTime,
    @Description("Product (eg. Thrombocyte concentrate)")
    val product: String
)

@JacksonSerializable
data class ProvidedMeasurement(
    @Description("Date of measurement")
    val date: LocalDate,
    @Description("Measurement category (eg. Body weight, Arterial blood pressure)")
    val category: String,
    @Description("Measurement subcategory (eg. Mean blood pressure)")
    val subcategory: String?,
    @Description("Value (eg. 70)")
    val value: Double,
    @Description("Unit (eg. kilograms)")
    val unit: String
)

@JacksonSerializable
data class ProvidedAllergy(
    @Description("Name of allergy (eg. Pembrolizumab)")
    override val name: String,
    @Description("Start date of appearance of allergy")
    override val startDate: LocalDate,
    @Description("End date of appearance of allergy, if applicable")
    val endDate: LocalDate?,
    @Description("Category of allergy (eg. medication)")
    val category: String,
    @Description("Severity of allergy (eg. low)")
    val severity: String,
    @Description("Clinical status of allergy (eg. active)")
    val clinicalStatus: String,
    @Description("Verification status of allergy (eg. confirmed)")
    val verificationStatus: String
) : ProvidedComorbidity

@JacksonSerializable
data class ProvidedWhoEvaluation(
    @Description("WHO performance status (eg. 1)")
    val status: Int,
    @Description("Date of WHO evaluation")
    val evaluationDate: LocalDate
)

@JacksonSerializable
data class ProvidedLesionDetail(
    @Description("Patient has lesion in brain")
    val hasBrainLesions: Boolean? = null,
    @Description("Patient has active lesion in brain")
    val hasActiveBrainLesions: Boolean? = null,
    @Description("Patient has lesion in bone")
    val hasBoneLesions: Boolean? = null,
    @Description("Patient has lesion in liver")
    val hasLiverLesions: Boolean? = null,
    @Description("Date of questionnaire")
    val questionnaireDate: LocalDate
)

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
                ProvidedLabUnit.entries.firstOrNull {
                    it.externalFormats.map { f -> f.lowercase() }.contains(inputString.lowercase())
                } ?: OTHER
            } ?: NONE
        }
    }
}
