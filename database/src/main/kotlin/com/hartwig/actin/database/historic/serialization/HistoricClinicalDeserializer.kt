package com.hartwig.actin.database.historic.serialization

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.hartwig.actin.clinical.datamodel.BloodTransfusion
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.ClinicalStatus
import com.hartwig.actin.clinical.datamodel.Complication
import com.hartwig.actin.clinical.datamodel.Dosage
import com.hartwig.actin.clinical.datamodel.ECG
import com.hartwig.actin.clinical.datamodel.ECGMeasure
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.datamodel.InfectionStatus
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.MedicationStatus
import com.hartwig.actin.clinical.datamodel.PatientDetails
import com.hartwig.actin.clinical.datamodel.PriorIHCTest
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
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse
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
        val clinical = JsonParser.parseReader(reader).asJsonObject

        val clinicalRecord = ClinicalRecord(
            patientId = extractPatientId(clinical),
            patient = extractPatientDetails(clinical),
            tumor = extractTumorDetails(clinical),
            clinicalStatus = extractClinicalStatus(clinical),
            oncologicalHistory = extractOncologicalHistory(clinical),
            priorSecondPrimaries = extractPriorSecondPrimaries(clinical),
            priorOtherConditions = extractPriorOtherConditions(clinical),
            complications = extractComplications(clinical),
            labValues = extractLabValues(clinical),
            toxicities = extractToxicities(clinical),
            intolerances = extractIntolerances(clinical),
            surgeries = extractSurgeries(clinical),
            bodyWeights = extractBodyWeights(clinical),
            bodyHeights = emptyList(),
            vitalFunctions = extractVitalFunctions(clinical),
            bloodTransfusions = extractBloodTransfusions(clinical),
            medications = extractMedications(clinical),
            // TODO (KD): Check if this is correctly adjusted.
            priorSequencingTests = emptyList(),
            priorIHCTests = extractPriorIHCTests(clinical)
        )

        if (reader.peek() != JsonToken.END_DOCUMENT) {
            LOGGER.warn("More data found in {} after reading main clinical JSON object!", clinicalJson)
        }

        return clinicalRecord
    }

    private fun extractPatientId(clinical: JsonObject): String {
        return if (clinical.has("patientId")) {
            Json.string(clinical, "patientId")
        } else {
            Json.string(clinical, "sampleId").substring(0, 12)
        }
    }

    private fun extractPatientDetails(clinical: JsonObject): PatientDetails {
        val patient = Json.`object`(clinical, "patient")
        return PatientDetails(
            gender = Gender.valueOf(Json.string(patient, "gender")),
            birthYear = Json.integer(patient, "birthYear"),
            registrationDate = Json.date(patient, "registrationDate"),
            questionnaireDate = Json.nullableDate(patient, "questionnaireDate")
        )
    }

    private fun extractTumorDetails(clinical: JsonObject): TumorDetails {
        val tumor = Json.`object`(clinical, "tumor")
        return TumorDetails(
            primaryTumorLocation = Json.nullableString(tumor, "primaryTumorLocation"),
            primaryTumorSubLocation = Json.nullableString(tumor, "primaryTumorSubLocation"),
            primaryTumorType = Json.nullableString(tumor, "primaryTumorType"),
            primaryTumorSubType = Json.nullableString(tumor, "primaryTumorSubType"),
            primaryTumorExtraDetails = Json.nullableString(tumor, "primaryTumorExtraDetails"),
            doids = Json.nullableArray(tumor, "doids")?.map { it.asString }?.toSet(),
            stage = Json.nullableString(tumor, "stage")?.let(TumorStage::valueOf),
            derivedStages = null, // Could reuse TumorStageDeriver here?
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
        val clinicalStatus = Json.`object`(clinical, "clinicalStatus")
        return ClinicalStatus(
            who = Json.nullableInteger(clinicalStatus, "who"),
            infectionStatus = Json.nullableObject(clinicalStatus, "infectionStatus")?.let { extractInfectionStatus(it) },
            ecg = Json.nullableObject(clinicalStatus, "ecg")?.let { extractECG(it) },
            lvef = Json.nullableDouble(clinicalStatus, "lvef"),
            hasComplications = null
        )
    }

    private fun extractInfectionStatus(infectionStatus: JsonObject): InfectionStatus {
        return InfectionStatus(
            hasActiveInfection = Json.bool(infectionStatus, "hasActiveInfection"),
            description = Json.nullableString(infectionStatus, "description")
        )
    }

    private fun extractECG(ecg: JsonObject): ECG {
        return ECG(
            hasSigAberrationLatestECG = Json.bool(ecg, "hasSigAberrationLatestECG"),
            aberrationDescription = Json.nullableString(ecg, "aberrationDescription"),
            qtcfMeasure = extractQtcfMeasure(ecg),
            jtcMeasure = extractECGMeasureObject(Json.optionalObject(ecg, "jtcMeasure"))
        )
    }

    private fun extractQtcfMeasure(ecg: JsonObject): ECGMeasure? {
        if (ecg.has("qtcfMeasure")) {
            return extractECGMeasureObject(Json.nullableObject(ecg, "qtcfMeasure"))
        } else {
            val qtcfValue: Int? = Json.nullableInteger(ecg, "qtcfValue")
            val qtcfUnit: String? = Json.nullableString(ecg, "qtcfUnit")
            if (qtcfValue == null && qtcfUnit == null) {
                return null
            }

            return ECGMeasure(qtcfValue, qtcfUnit)
        }
    }

    private fun extractECGMeasureObject(ecgObject: JsonObject?): ECGMeasure? {
        return ecgObject?.let {
            ECGMeasure(value = Json.nullableInteger(ecgObject, "value"), unit = Json.nullableString(ecgObject, "unit"))
        }
    }

    private fun extractOncologicalHistory(clinical: JsonObject): List<TreatmentHistoryEntry> {
        return if (clinical.has("priorTumorTreatments")) {
            val priorTumorTreatments = Json.array(clinical, "priorTumorTreatments")
            priorTumorTreatments.mapNotNull { toTreatmentHistoryEntry(it) }
        } else {
            // TODO (KD): We switch to actual treatment history at some point which replaced prior tumor treatment.
            emptyList()
        }
    }

    private fun toTreatmentHistoryEntry(priorTumorTreatmentElement: JsonElement): TreatmentHistoryEntry {
        val priorTumorTreatment = priorTumorTreatmentElement.asJsonObject
        return TreatmentHistoryEntry(
            treatments = extractTreatments(priorTumorTreatment),
            startYear = Json.nullableInteger(priorTumorTreatment, "startYear"),
            startMonth = Json.nullableInteger(priorTumorTreatment, "startMonth"),
            intents = null,
            isTrial = Json.nullableString(priorTumorTreatment, "trialAcronym") != null, // Note (KD): Assumption!
            trialAcronym = Json.nullableString(priorTumorTreatment, "trialAcronym"),
            treatmentHistoryDetails = extractTreatmentHistoryDetails(priorTumorTreatment)
        )
    }

    private fun extractTreatments(priorTumorTreatment: JsonObject): Set<Treatment> {
        return setOf(
            DrugTreatment(
                name = Json.string(priorTumorTreatment, "name"),
                drugs = emptySet(),
                synonyms = emptySet(),
                displayOverride = null,
                isSystemic = Json.bool(priorTumorTreatment, "isSystemic"),
                maxCycles = null
            )
        )
    }

    private fun extractTreatmentHistoryDetails(priorTumorTreatment: JsonObject): TreatmentHistoryDetails {
        return TreatmentHistoryDetails(
            stopYear = Json.nullableInteger(priorTumorTreatment, "stopYear"),
            stopMonth = Json.nullableInteger(priorTumorTreatment, "stopMonth"),
            ongoingAsOf = null,
            cycles = null,
            bestResponse = Json.nullableString(priorTumorTreatment, "bestResponse")?.let { toTreatmentResponse(it) },
            stopReason = Json.nullableString(priorTumorTreatment, "stopReason")?.let { toStopReason(it) },
            stopReasonDetail = null,
            switchToTreatments = null,
            maintenanceTreatment = null,
            toxicities = null,
            bodyLocationCategories = null,
            bodyLocations = null
        )
    }

    private fun toStopReason(stopReasonString: String): StopReason? {
        val stopReason = StopReason.createFromString(stopReasonString)
        return when {
            stopReason != null -> stopReason
            sequenceOf("Allergic", "disfunction").any(stopReasonString::contains) -> StopReason.TOXICITY
            else -> {
                LOGGER.warn("  Could not convert stop reason string: {}", stopReasonString)
                null
            }
        }
    }

    private fun toTreatmentResponse(treatmentResponseString: String): TreatmentResponse? {
        val treatmentResponse = TreatmentResponse.createFromString(treatmentResponseString)
        if (treatmentResponse == null) {
            LOGGER.warn("  Could not convert treatment response string: {}", treatmentResponseString)
        }
        return treatmentResponse
    }

    private fun extractPriorSecondPrimaries(clinical: JsonObject): List<PriorSecondPrimary> {
        return Json.array(clinical, "priorSecondPrimaries").mapNotNull { toPriorSecondPrimary(it) }
    }

    private fun toPriorSecondPrimary(priorSecondPrimaryElement: JsonElement): PriorSecondPrimary {
        val priorSecondPrimary: JsonObject = priorSecondPrimaryElement.asJsonObject
        return PriorSecondPrimary(
            tumorLocation = Json.string(priorSecondPrimary, "tumorLocation"),
            tumorSubLocation = Json.string(priorSecondPrimary, "tumorSubLocation"),
            tumorType = Json.string(priorSecondPrimary, "tumorType"),
            tumorSubType = Json.string(priorSecondPrimary, "tumorSubType"),
            doids = HashSet(Json.stringList(priorSecondPrimary, "doids")),
            diagnosedYear = Json.nullableInteger(priorSecondPrimary, "diagnosedYear"),
            diagnosedMonth = Json.nullableInteger(priorSecondPrimary, "diagnosedMonth"),
            treatmentHistory = Json.string(priorSecondPrimary, "treatmentHistory"),
            lastTreatmentYear = Json.nullableInteger(priorSecondPrimary, "lastTreatmentYear"),
            lastTreatmentMonth = Json.nullableInteger(priorSecondPrimary, "lastTreatmentMonth"),
            status = determinePriorSecondPrimaryStatus(priorSecondPrimary)
        )
    }

    private fun determinePriorSecondPrimaryStatus(priorSecondPrimary: JsonObject): TumorStatus {
        return Json.optionalBool(priorSecondPrimary, "isActive")?.let {
            if (it) TumorStatus.ACTIVE else TumorStatus.INACTIVE
        } ?: TumorStatus.valueOf(Json.string(priorSecondPrimary, "status"))
    }

    private fun extractPriorOtherConditions(clinical: JsonObject): List<PriorOtherCondition> {
        return Json.array(clinical, "priorOtherConditions").mapNotNull(::toPriorOtherCondition)
    }

    private fun toPriorOtherCondition(priorOtherConditionElement: JsonElement): PriorOtherCondition {
        val priorOtherCondition = priorOtherConditionElement.asJsonObject
        return PriorOtherCondition(
            name = Json.string(priorOtherCondition, "name"),
            year = Json.nullableInteger(priorOtherCondition, "year"),
            month = Json.nullableInteger(priorOtherCondition, "month"),
            doids = HashSet(Json.stringList(priorOtherCondition, "doids")),
            category = Json.string(priorOtherCondition, "category"),
            isContraindicationForTherapy = Json.bool(priorOtherCondition, "isContraindicationForTherapy")
        )
    }

    private fun extractPriorIHCTests(clinical: JsonObject): List<PriorIHCTest> {
        return Json.array(clinical, "priorMolecularTests").mapNotNull(::toPriorIHCTest)
    }

    private fun toPriorIHCTest(priorMolecularTestElement: JsonElement): PriorIHCTest {
        val priorMolecularTest = priorMolecularTestElement.asJsonObject
        return PriorIHCTest(
            test = Json.string(priorMolecularTest, "test"),
            item = Json.nullableString(priorMolecularTest, "item"),
            measure = Json.nullableString(priorMolecularTest, "measure"),
            measureDate = null,
            scoreText = Json.nullableString(priorMolecularTest, "scoreText"),
            scoreValuePrefix = Json.nullableString(priorMolecularTest, "scoreValuePrefix"),
            scoreValue = Json.nullableDouble(priorMolecularTest, "scoreValue"),
            scoreValueUnit = Json.nullableString(priorMolecularTest, "scoreValueUnit"),
            impliesPotentialIndeterminateStatus = false
        )
    }

    private fun extractComplications(clinical: JsonObject): List<Complication>? {
        return Json.optionalArray(clinical, "complications")?.mapNotNull(::toComplication)
    }

    private fun toComplication(complicationElement: JsonElement): Complication {
        val complication = complicationElement.asJsonObject
        return Complication(
            name = Json.string(complication, "name"),
            categories = HashSet(Json.stringList(complication, "categories")),
            year = Json.nullableInteger(complication, "year"),
            month = Json.nullableInteger(complication, "month")
        )
    }

    private fun extractLabValues(clinical: JsonObject): List<LabValue> {
        return Json.array(clinical, "labValues").mapNotNull(::toLabValue)
    }

    private fun toLabValue(labValueElement: JsonElement): LabValue {
        val labValue = labValueElement.asJsonObject
        return LabValue(
            date = Json.date(labValue, "date"),
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
        return Json.array(clinical, "toxicities").mapNotNull { toToxicity(it) }
    }

    private fun toToxicity(toxicityElement: JsonElement): Toxicity {
        val toxicity = toxicityElement.asJsonObject
        return Toxicity(
            name = Json.string(toxicity, "name"),
            categories = emptySet(),
            evaluatedDate = Json.date(toxicity, "evaluatedDate"),
            source = ToxicitySource.valueOf(Json.string(toxicity, "source")),
            grade = Json.nullableInteger(toxicity, "grade")
        )
    }

    private fun extractIntolerances(clinical: JsonObject): List<Intolerance> {
        return Json.array(clinical, "intolerances").mapNotNull { toIntolerance(it) }
    }

    private fun toIntolerance(intoleranceElement: JsonElement): Intolerance {
        val intolerance = intoleranceElement.asJsonObject
        return Intolerance(
            name = Json.string(intolerance, "name"),
            doids = HashSet(Json.stringList(intolerance, "doids")),
            category = Json.nullableString(intolerance, "category"),
            subcategories = Json.nullableStringList(intolerance, "subcategories")?.toSet(),
            type = Json.nullableString(intolerance, "type"),
            clinicalStatus = Json.nullableString(intolerance, "clinicalStatus"),
            verificationStatus = Json.nullableString(intolerance, "verificationStatus"),
            criticality = Json.nullableString(intolerance, "criticality"),
            treatmentCategories = null
        )
    }

    private fun extractSurgeries(clinical: JsonObject): List<Surgery> {
        return Json.array(clinical, "surgeries").mapNotNull { toSurgery(it) }
    }

    private fun toSurgery(surgeryElement: JsonElement): Surgery {
        val surgery = surgeryElement.asJsonObject
        return Surgery(
            endDate = Json.date(surgery, "endDate"),
            status = SurgeryStatus.valueOf(Json.string(surgery, "status"))
        )
    }

    private fun extractBodyWeights(clinical: JsonObject): List<BodyWeight> {
        return Json.array(clinical, "bodyWeights").mapNotNull { toBodyWeight(it) }
    }

    private fun toBodyWeight(bodyWeightElement: JsonElement): BodyWeight {
        val bodyWeight = bodyWeightElement.asJsonObject
        return BodyWeight(
            date = toDateTime(Json.date(bodyWeight, "date")),
            value = Json.double(bodyWeight, "value"),
            unit = Json.string(bodyWeight, "unit"),
            valid = false
        )
    }

    private fun extractVitalFunctions(clinical: JsonObject): List<VitalFunction> {
        return Json.array(clinical, "vitalFunctions").mapNotNull { toVitalFunction(it) }
    }

    private fun toVitalFunction(vitalFunctionElement: JsonElement): VitalFunction {
        val vitalFunction = vitalFunctionElement.asJsonObject
        return VitalFunction(
            date = toDateTime(Json.date(vitalFunction, "date")),
            category = VitalFunctionCategory.valueOf(Json.string(vitalFunction, "category")),
            subcategory = Json.string(vitalFunction, "subcategory"),
            value = Json.double(vitalFunction, "value"),
            unit = Json.string(vitalFunction, "unit"),
            valid = false
        )
    }

    private fun extractBloodTransfusions(clinical: JsonObject): List<BloodTransfusion> {
        return Json.array(clinical, "bloodTransfusions").mapNotNull { toBloodTransfusion(it) }
    }

    private fun toBloodTransfusion(bloodTransfusionElement: JsonElement): BloodTransfusion {
        val bloodTransfusion = bloodTransfusionElement.asJsonObject
        return BloodTransfusion(
            date = Json.date(bloodTransfusion, "date"),
            product = Json.string(bloodTransfusion, "product")
        )
    }

    private fun extractMedications(clinical: JsonObject): List<Medication>? {
        return Json.optionalArray(clinical, "medications")?.mapNotNull { toMedication(it) }
    }

    private fun toMedication(medicationElement: JsonElement): Medication {
        val medication = medicationElement.asJsonObject
        return Medication(
            name = Json.string(medication, "name"),
            status = Json.nullableString(medication, "status")?.let { MedicationStatus.valueOf(it) },
            administrationRoute = null,
            dosage = extractDosage(medication),
            startDate = Json.nullableDate(medication, "startDate"),
            stopDate = Json.nullableDate(medication, "stopDate"),
            cypInteractions = emptyList(),
            qtProlongatingRisk = QTProlongatingRisk.UNKNOWN,
            atc = null,
            isSelfCare = false,
            isTrialMedication = false
        )
    }

    private fun extractDosage(medication: JsonObject): Dosage {
        val dosage = if (medication.has("dosage")) Json.`object`(medication, "dosage") else medication

        return Dosage(
            dosageMin = Json.nullableDouble(dosage, "dosageMin"),
            dosageMax = Json.nullableDouble(dosage, "dosageMax"),
            dosageUnit = Json.nullableString(dosage, "dosageUnit"),
            frequency = Json.nullableDouble(dosage, "frequency"),
            frequencyUnit = Json.nullableString(dosage, "frequencyUnit"),
            periodBetweenValue = Json.optionalDouble(dosage, "periodBetweenValue"),
            periodBetweenUnit = Json.optionalString(dosage, "periodBetweenUnit"),
            ifNeeded = Json.nullableBool(dosage, "ifNeeded")
        )
    }

    private fun toDateTime(date: LocalDate): LocalDateTime {
        return LocalDateTime.of(date.year, date.month, date.dayOfMonth, 0, 0)
    }
}