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
    val complications: List<ProvidedComplication> = emptyList(),
    val labValues: List<ProvidedLabValue> = emptyList(),
    val medications: List<ProvidedMedication>? = emptyList(),
    val molecularTests: List<ProvidedMolecularTest> = emptyList(),
    val patientDetails: ProvidedPatientDetail,
    val priorOtherConditions: List<ProvidedOtherCondition> = emptyList(),
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
    @Description("Year of birth of this patient (eg. 1940)")
    val birthYear: Int,
    @Description("Year of birth of this patient (eg. Male, Female, Other)")
    val gender: String,
    @Description("Registration data of this patient with ACTIN")
    val registrationDate: LocalDate,
    @Description("Base64 encoded SHA-256 hash of source hospital's identifier.")
    val hashedId: String,
    @Description("Flag to indicate there is pending Hartwig analysis data for this patient")
    val hartwigMolecularDataExpected: Boolean
)

@JacksonSerializable
data class ProvidedTumorDetail(
    @Description("Date of diagnosis")
    val diagnosisDate: LocalDate?,
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
    val lesions: List<ProvidedLesion>? = null,
    @Description("Deprecated: currently use to store radiology report. Should move to lesions")
    val lesionSite: String? = null,
    @Description("Raw pathology report of molecular test results.")
    val rawPathologyReport: String? = null
)

@JacksonSerializable
data class ProvidedTreatmentHistory(
    @Description("Name of the treatment given (eg. Gemcitabine+Cisplatin)")
    val treatmentName: String,
    @Description("Intention of the treatment given (eg. Palliative)")
    val intention: String? = null,
    @Description("Date of the start of treatment")
    val startDate: LocalDate,
    @Description("Date of the end of treatment")
    val endDate: LocalDate? = null,
    @Description("Reason of stopping treatment (eg. Progressive disease)")
    val stopReason: String? = null,
    val stopReasonDate: LocalDate? = null,
    @Description("Response to treatment (eg. Partial Response)")
    val response: String? = null,
    val responseDate: LocalDate? = null,
    @Description("Intended number of cycles (eg. 6)")
    val intendedCycles: Int? = null,
    @Description("Administered number of cycles (eg. 6)")
    val administeredCycles: Int? = null,
    val modifications: List<ProvidedTreatmentModification>? = null,
    @Description("Treatment administered in clinical study")
    val administeredInStudy: Boolean,
    @Description("Trial acronym or other short identifier if administered in study")
    val trialAcronym: String? = null
)

@JacksonSerializable
data class ProvidedTreatmentModification(
    @Description("Name of the modified treatment given (eg. Gemcitabine+Cisplatin)")
    val name: String,
    @Description("Date of the start of modification of treatment")
    val date: LocalDate,
    @Description("Modified number of cycles (eg. 6)")
    val administeredCycles: Int,
)

@JacksonSerializable
data class ProvidedMolecularTest(
    @Description("Name of the test administered, as specific as possible (eg. Archer, NGS, IHC)")
    val test: String,
    @Description("Date the test was administered")
    val date: LocalDate? = null,
    @Description("Name of the source system from which the data came (eg. PALGA, DNA-DB)")
    val datasource: String? = null,
    @Description("List of genes that were tested.")
    val testedGenes: Set<String>? = null,
    val results: Set<ProvidedMolecularTestResult>
)

@JacksonSerializable
data class ProvidedMolecularTestResult(
    @Description("Gene involved in this result. (eg. KRAS)")
    val gene: String? = null,
    @Description("Full result string of IHC test ie. (eg. PD-L1 weak positive 20%)")
    val ihcResult: String? = null,
    @Description("HGVS notation describing protein impact ie. (eg. p.G12V)")
    val hgvsProteinImpact: String? = null,
    @Description("HGVS notation describing coding impact ie. (eg. c.4375C>T)")
    val hgvsCodingImpact: String? = null,
    @Description("Transcript referenced in other positional attributes (eg. NM_004304.5)")
    val transcript: String? = null,
    @Description("Upstream gene of a fusion (eg. EML4)")
    val fusionGeneUp: String? = null,
    @Description("Downstream gene of a fusion (eg. ALK)")
    val fusionGeneDown: String? = null,
    @Description("Upstream transcript of a fusion (eg. NM_019063.5)")
    val fusionTranscriptUp: String? = null,
    @Description("Downstream transcript of a fusion (eg. NM_004304.5)")
    val fusionTranscriptDown: String? = null,
    @Description("Upstream exon of a fusion (eg. 13)")
    val fusionExonUp: Int? = null,
    @Description("Downstream exon of a  fusion (eg. 20)")
    val fusionExonDown: Int? = null,
    @Description("Exon involved in this result (eg. 19)")
    val exon: Int? = null,
    @Description("Codon involved in this result (eg. 1)")
    val codon: Int? = null,
    @Description("Exons skipped in a structural variant start (eg. 18)")
    val exonSkipStart: Int? = null,
    @Description("Exons skipped in a structural variant end (eg. 20)")
    val exonSkipEnd: Int? = null,
    @Description("Gene detected as amplified (eg. MET)")
    val amplifiedGene: String? = null,
    @Description("Gene detected as fully deleted (eg. MET)")
    val deletedGene: String? = null,
    @Description("Flag should be set to indicate a negative result for a gene (ie. nothing was found)")
    val noMutationsFound: Boolean? = null,
    @Description("Free text for a test result which does not fit into any of the other fields. This value will be curated.")
    val freeText: String? = null,
    @Description("Result of microsatellite instability test.")
    val msi: Boolean? = null,
    @Description("Tumor mutational burden in m/MB (eg. 8.0)")
    val tmb: Double? = null,
    @Description("Variant allele frequency as a fraction (eg. 0.01 is interpreted as 1%)")
    val vaf: Double? = null
)

@JacksonSerializable
data class ProvidedPriorPrimary(
    @Description("Diagnosis date")
    val diagnosisDate: LocalDate?,
    @Description("Tumor localization details (eg. Colon)")
    val tumorLocation: String,
    @Description("Tumor type details (eg. Carcinoma)")
    val tumorType: String,
    @Description("Observed status of tumor (eg. Active/Inactive - null if unknown)")
    val status: String? = null,
    @Description("Date of last treatment")
    val lastTreatmentDate: LocalDate? = null
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
data class ProvidedComplication(
    @Description("Name of complication (eg. Ascites)")
    override val name: String,
    @Description("Start date of complication")
    override val startDate: LocalDate,
    @Description("End date of complication")
    val endDate: LocalDate?
) : ProvidedComorbidity

@JacksonSerializable
data class ProvidedToxicity(
    @Description("Name of toxicity (eg. Neuropathy)")
    override val name: String,
    @Description("Date of evaluation")
    val evaluatedDate: LocalDate,
    @Description("Grade (eg. 2)")
    val grade: Int,
    @Description("End date")
    val endDate: LocalDate?
) : ProvidedComorbidity {
    override val startDate: LocalDate
        get() = evaluatedDate
}

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
    @Description("Date of WHO evaluation.")
    val evaluationDate: LocalDate
)

@JacksonSerializable
data class ProvidedSurgery(
    @Description("Name of surgery (eg. Diagnostics stomach)")
    val surgeryName: String?,
    @Description("Date of completion, if applicable.")
    val endDate: LocalDate,
    @Description("Status of surgery (eg. complete)")
    val status: String
)

@JacksonSerializable
data class ProvidedLesion(
    @Description("Location of lesion (eg. brain)")
    val location: String,
    @Description("Diagnosis date of the lesion")
    val diagnosisDate: LocalDate,
    @Description("Whether this lesion considered active, only applicable to brain or CNS lesions.")
    val active: Boolean? = null
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
