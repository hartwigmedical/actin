package com.hartwig.actin.database.historic.serialization

import com.google.common.collect.Sets
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.hartwig.actin.clinical.datamodel.BloodTransfusion
import com.hartwig.actin.clinical.datamodel.BodyHeight
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.ClinicalStatus
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.Dosage
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import com.hartwig.actin.clinical.datamodel.PatientDetails
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.QTProlongatingRisk
import com.hartwig.actin.clinical.datamodel.Surgery
import com.hartwig.actin.clinical.datamodel.SurgeryStatus
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.ToxicitySource
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.clinical.datamodel.TumorStatus
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.util.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.FileReader
import java.time.LocalDate
import java.time.LocalDateTime

object HistoricClinicalDeserializer {

    private val LOGGER: Logger = LogManager.getLogger(HistoricClinicalDeserializer::class.java)

    fun deserialize(clinicalJson: File): ClinicalRecord {
        val reader = JsonReader(FileReader(clinicalJson))
        val clinicalObject: JsonObject = JsonParser.parseReader(reader).asJsonObject

        val clinicalRecord = ClinicalRecord(
            extractPatientId(clinicalObject),
            extractPatientDetails(clinicalObject),
            extractTumorDetails(clinicalObject),
            extractClinicalStatus(clinicalObject),
            extractOncologicalHistory(clinicalObject),
            extractPriorSecondPrimaries(clinicalObject),
            extractPriorOtherConditions(clinicalObject),
            extractPriorMolecularTest(clinicalObject),
            extractComplications(clinicalObject),
            extractLabValues(clinicalObject),
            extractToxicities(clinicalObject),
            extractIntolerances(clinicalObject),
            extractSurgeries(clinicalObject),
            extractBodyWeights(clinicalObject),
            extractBodyHeights(clinicalObject),
            extractVitalFunctions(clinicalObject),
            extractBloodTransfusions(clinicalObject),
            extractMedications(clinicalObject)
        )

        if (reader.peek() != JsonToken.END_DOCUMENT) {
            LOGGER.warn("More data found in {} after reading main JSON object!", clinicalJson)
        }

        return clinicalRecord
    }

    private fun extractPatientId(clinical: JsonObject): String {
        val sample: String = Json.string(clinical, "sampleId")
        return sample.substring(0, 12)
    }

    private fun extractPatientDetails(clinical: JsonObject): PatientDetails {
        val patient: JsonObject = Json.`object`(clinical, "patient")
        return PatientDetails(
            gender = Gender.valueOf(Json.string(patient, "gender")),
            birthYear = Json.integer(patient, "birthYear"),
            registrationDate = date(patient, "registrationDate"),
            questionnaireDate = nullableDate(patient, "questionnaireDate")
        )
    }

    private fun extractTumorDetails(clinical: JsonObject): TumorDetails {
        val tumor: JsonObject = Json.`object`(clinical, "tumor")
        return TumorDetails(
            primaryTumorLocation = Json.nullableString(tumor, "primaryTumorLocation"),
            primaryTumorSubLocation = Json.nullableString(tumor, "primaryTumorSubLocation"),
            primaryTumorType = Json.nullableString(tumor, "primaryTumorType"),
            primaryTumorSubType = Json.nullableString(tumor, "primaryTumorSubType"),
            primaryTumorExtraDetails = Json.nullableString(tumor, "primaryTumorExtraDetails"),
            doids = Json.nullableArray(tumor, "doids")?.let { it -> it.map { it.asString }.toCollection(Sets.newHashSet()) },
            stage = Json.nullableString(tumor, "stage")?.let { TumorStage.valueOf(it) },
            derivedStages = null, // TODO (KD): Could reuse TumorStageDeriver here.
            hasMeasurableDisease = Json.nullableBool(tumor, "hasMeasurableDisease"),
            hasBrainLesions = Json.nullableBool(tumor, "hasBrainLesions"),
            brainLesionsCount = null,
            hasActiveBrainLesions = Json.nullableBool(tumor, "hasActiveBrainLesions"),
            hasCnsLesions = Json.nullableBool(tumor, "hasCnsLesions"),
            cnsLesionsCount = null,
            hasActiveCnsLesions = Json.nullableBool(tumor, "hasActiveCnsLesions"),
            hasBoneLesions = Json.nullableBool(tumor, "hasBoneLesions"),
            boneLesionsCount = null,
            hasLiverLesions = Json.nullableBool(tumor, "hasLiverLesions"),
            liverLesionsCount = null,
            hasLungLesions = Json.nullableBool(tumor, "hasLungLesions"),
            lungLesionsCount = null,
            hasLymphNodeLesions = null,
            lymphNodeLesionsCount = null,
            otherLesions = Json.nullableStringList(tumor, "otherLesions"),
            biopsyLocation = Json.nullableString(tumor, "biopsyLocation")
        )
    }

    private fun extractClinicalStatus(clinical: JsonObject): ClinicalStatus {
        val clinicalStatus: JsonObject = Json.`object`(clinical, "clinicalStatus")
        return ClinicalStatus(
            who = Json.nullableInteger(clinicalStatus, "who"),
            infectionStatus = null,
            ecg = null,
            lvef = null,
            hasComplications = null
        )
    }

    private fun extractOncologicalHistory(clinical: JsonObject): List<TreatmentHistoryEntry> {
        val priorTumorTreatments: JsonArray = Json.array(clinical, "priorTumorTreatments")
        return priorTumorTreatments.mapNotNull { toTreatmentHistoryEntry(it) }
    }

    private fun toTreatmentHistoryEntry(priorTumorTreatmentElement: JsonElement): TreatmentHistoryEntry {
        val priorTumorTreatment: JsonObject = priorTumorTreatmentElement.asJsonObject
        return TreatmentHistoryEntry(
            treatments = extractTreatments(priorTumorTreatment),
            startYear = null,
            startMonth = null,
            intents = null,
            isTrial = false,
            trialAcronym = null,
            treatmentHistoryDetails = null
        )
    }

    private fun extractTreatments(priorTumorTreatment: JsonObject): Set<Treatment> {
        // TODO (KD) : Map to treatments.
        return setOf(

        )
    }

    private fun extractPriorSecondPrimaries(clinical: JsonObject): List<PriorSecondPrimary> {
        val priorSecondPrimaries: JsonArray = Json.array(clinical, "priorSecondPrimaries")
        return priorSecondPrimaries.mapNotNull { toPriorSecondPrimary(it) }
    }

    private fun toPriorSecondPrimary(priorSecondPrimaryElement: JsonElement): PriorSecondPrimary {
        val priorSecondPrimary: JsonObject = priorSecondPrimaryElement.asJsonObject
        return PriorSecondPrimary(
            tumorLocation = "",
            tumorSubLocation = "",
            tumorType = "",
            tumorSubType = "",
            doids = emptySet(),
            diagnosedYear = null,
            diagnosedMonth = null,
            treatmentHistory = "",
            lastTreatmentYear = null,
            lastTreatmentMonth = null,
            status = TumorStatus.UNKNOWN
        )
    }

    private fun extractPriorOtherConditions(clinical: JsonObject): List<PriorOtherCondition> {
        val priorOtherConditions: JsonArray = Json.array(clinical, "priorOtherConditions")
        return priorOtherConditions.mapNotNull { toPriorOtherCondition(it) }
    }

    private fun toPriorOtherCondition(priorOtherConditionElement: JsonElement): PriorOtherCondition {
        val priorOtherCondition: JsonObject = priorOtherConditionElement.asJsonObject
        return PriorOtherCondition(
            name = "",
            year = null,
            month = null,
            doids = emptySet(),
            category = "",
            isContraindicationForTherapy = false
        )
    }

    private fun extractPriorMolecularTest(clinical: JsonObject): List<PriorMolecularTest> {
        val priorMolecularTests: JsonArray = Json.array(clinical, "priorMolecularTests")
        return priorMolecularTests.mapNotNull { toPriorMolecularTest(it) }
    }

    private fun toPriorMolecularTest(priorMolecularTestElement: JsonElement): PriorMolecularTest {
        val priorMolecularTest: JsonObject = priorMolecularTestElement.asJsonObject
        return PriorMolecularTest(
            test = "",
            item = null,
            measure = null,
            measureDate = null,
            scoreText = null,
            scoreValuePrefix = null,
            scoreValue = null,
            scoreValueUnit = null,
            impliesPotentialIndeterminateStatus = false
        )
    }

    private fun extractComplications(clinical: JsonObject): List<Complication>? {
        val complication: JsonArray? = Json.optionalArray(clinical, "complications")
        return complication?.let { it -> it.mapNotNull { toComplication(it) } }
    }

    private fun toComplication(complicationElement: JsonElement): Complication {
        val complication: JsonObject = complicationElement.asJsonObject
        return Complication(
            name = "",
            categories = emptySet(),
            year = null,
            month = null
        )
    }

    private fun extractLabValues(clinical: JsonObject): List<LabValue> {
        val labValues: JsonArray = Json.array(clinical, "labValues")
        return labValues.mapNotNull { toLabValue(it) }
    }

    private fun toLabValue(labValueElement: JsonElement): LabValue {
        val labValue: JsonObject = labValueElement.asJsonObject
        return LabValue(
            date = date(labValue, "date"),
            code = Json.string(labValue, "code"),
            name = Json.string(labValue, "name"),
            comparator = Json.string(labValue, "comparator"),
            value = Json.double(labValue, "value"),
            unit = LabUnit.valueOf(Json.string(labValue, "unit")),
            refLimitLow = Json.nullableDouble(labValue, "refLimitLow"),
            refLimitUp = Json.nullableDouble(labValue, "refLimitUp"),
            isOutsideRef = Json.nullableBool(labValue, "isOutsideRef")
        )
    }

    private fun extractToxicities(clinical: JsonObject): List<Toxicity> {
        val toxicities: JsonArray = Json.array(clinical, "toxicities")
        return toxicities.mapNotNull { toToxicity(it) }
    }

    private fun toToxicity(toxicityElement: JsonElement): Toxicity {
        val toxicity: JsonObject = toxicityElement.asJsonObject
        return Toxicity(
            name = "",
            categories = emptySet(),
            evaluatedDate = LocalDate.of(1, 1, 1),
            source = ToxicitySource.EHR,
            grade = null
        )
    }

    private fun extractIntolerances(clinical: JsonObject): List<Intolerance> {
        val intolerances: JsonArray = Json.array(clinical, "intolerances")
        return intolerances.mapNotNull { toIntolerance(it) }
    }

    private fun toIntolerance(intoleranceElement: JsonElement): Intolerance {
        val intolerance: JsonObject = intoleranceElement.asJsonObject
        return Intolerance(
            name = Json.string(intolerance, "name"),
            doids = HashSet(Json.stringList(intolerance, "doids")),
            category = Json.nullableString(intolerance, "category"),
            subcategories = Json.nullableStringList(intolerance, "subcategories")?.let { HashSet(it) },
            type = Json.nullableString(intolerance, "type"),
            clinicalStatus = Json.nullableString(intolerance, "clinicalStatus"),
            verificationStatus = Json.nullableString(intolerance, "verificationStatus"),
            criticality = Json.nullableString(intolerance, "criticality"),
            treatmentCategories = null // TODO (KD) See if we can populate this field for older datamodels.
        )
    }

    private fun extractSurgeries(clinical: JsonObject): List<Surgery> {
        val surgeries: JsonArray = Json.array(clinical, "surgeries")
        return surgeries.mapNotNull { toSurgery(it) }
    }

    private fun toSurgery(surgeryElement: JsonElement): Surgery {
        val surgery: JsonObject = surgeryElement.asJsonObject
        return Surgery(
            endDate = LocalDate.of(1, 1, 1),
            status = SurgeryStatus.UNKNOWN
        )
    }

    private fun extractBodyWeights(clinical: JsonObject): List<BodyWeight> {
        val bodyWeights: JsonArray = Json.array(clinical, "bodyWeights")
        return bodyWeights.mapNotNull { toBodyWeight(it) }
    }

    private fun toBodyWeight(bodyWeightElement: JsonElement): BodyWeight {
        val bodyWeight: JsonObject = bodyWeightElement.asJsonObject
        return BodyWeight(
            date = toDateTime(date(bodyWeight, "date")),
            value = Json.double(bodyWeight, "value"),
            unit = Json.string(bodyWeight, "unit"),
            valid = false // TODO (KD): See if we can populate this value.
        )
    }

    private fun extractBodyHeights(clinical: JsonObject): List<BodyHeight> {
        // TODO (KD): See if we need to populate this field.
        return listOf()
    }

    private fun extractVitalFunctions(clinical: JsonObject): List<VitalFunction> {
        val vitalFunctions: JsonArray = Json.array(clinical, "vitalFunctions")
        return vitalFunctions.mapNotNull { toVitalFunction(it) }
    }

    private fun toVitalFunction(vitalFunctionElement: JsonElement): VitalFunction {
        val vitalFunction: JsonObject = vitalFunctionElement.asJsonObject
        return VitalFunction(
            date = toDateTime(date(vitalFunction, "date")),
            category = VitalFunctionCategory.valueOf(Json.string(vitalFunction, "category")),
            subcategory = Json.string(vitalFunction, "subcategory"),
            value = Json.double(vitalFunction, "value"),
            unit = Json.string(vitalFunction, "unit"),
            valid = false // TODO (KD): See if we can populate this field.
        )
    }

    private fun extractBloodTransfusions(clinical: JsonObject): List<BloodTransfusion> {
        val bloodTransfusions: JsonArray = Json.array(clinical, "bloodTransfusions")
        return bloodTransfusions.mapNotNull { toBloodTransfusion(it) }
    }

    private fun toBloodTransfusion(bloodTransfusionElement: JsonElement): BloodTransfusion {
        val bloodTransfusion: JsonObject = bloodTransfusionElement.asJsonObject
        return BloodTransfusion(
            date = LocalDate.of(1, 1, 1),
            product = ""
        )
    }

    private fun extractMedications(clinical: JsonObject): List<Medication>? {
        val medications: JsonArray? = Json.optionalArray(clinical, "medications")
        return medications?.let { it -> it.mapNotNull { toMedication(it) } }
    }

    private fun toMedication(medicationElement: JsonElement): Medication {
        val medication: JsonObject = medicationElement.asJsonObject
        return Medication(
            name = Json.string(medication, "name"),
            status = Json.nullableString(medication, "status")?.let { MedicationStatus.valueOf(it) },
            administrationRoute = null, // TODO (KD): See if we can populate.
            dosage = Dosage(
                dosageMin = Json.nullableDouble(medication, "dosageMin"),
                dosageMax = Json.nullableDouble(medication, "dosageMax"),
                dosageUnit = Json.nullableString(medication, "dosageUnit"),
                frequency = Json.nullableDouble(medication, "frequency"),
                frequencyUnit = Json.nullableString(medication, "frequencyUnit"),
                periodBetweenValue = null, // TODO (KD): See if we can populate
                periodBetweenUnit = null, // TODO (KD): See if we can populate
                ifNeeded = Json.nullableBool(medication, "ifNeeded")
            ),
            startDate = nullableDate(medication, "startDate"),
            stopDate = nullableDate(medication, "stopDate"),
            cypInteractions = emptyList(), // TODO (KD): See if we can populate
            qtProlongatingRisk = QTProlongatingRisk.UNKNOWN, // TODO (KD): See if we can populate.
            atc = null, // TODO (KD): See if we can populate
            isSelfCare = false, // TODO (KD): See if we can populate
            isTrialMedication = false // TODO (KD): See if we can populate.
        )
    }

    private fun nullableDate(obj: JsonObject, field: String): LocalDate? {
        if (obj.get(field).isJsonNull) {
            return null;
        }
        return date(obj, field)
    }

    private fun date(obj: JsonObject, field: String): LocalDate {
        val jsonDate: JsonObject = Json.`object`(obj, field)
        return LocalDate.of(Json.integer(jsonDate, "year"), Json.integer(jsonDate, "month"), Json.integer(jsonDate, "day"))
    }

    private fun toDateTime(date: LocalDate): LocalDateTime {
        return LocalDateTime.of(date.year, date.month, date.dayOfMonth, 0, 0)
    }
}