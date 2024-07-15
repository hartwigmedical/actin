package com.hartwig.actin.database.historic

import com.google.common.collect.Sets
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
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.PatientDetails
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.Surgery
import com.hartwig.actin.clinical.datamodel.Toxicity
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.clinical.datamodel.VitalFunction
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.util.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.FileReader
import java.time.LocalDate

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
            registrationDate = date(Json.`object`(patient, "registrationDate")),
            questionnaireDate = optionalDate(Json.`object`(patient, "questionnaireDate"))
        )
    }

    private fun extractTumorDetails(clinical: JsonObject): TumorDetails {
        val tumor: JsonObject = Json.`object`(clinical, "tumor")
        return TumorDetails(
            primaryTumorLocation = Json.optionalString(tumor, "primaryTumorLocation"),
            primaryTumorSubLocation = Json.optionalString(tumor, "primaryTumorSubLocation"),
            primaryTumorType = Json.optionalString(tumor, "primaryTumorType"),
            primaryTumorSubType = Json.optionalString(tumor, "primaryTumorSubType"),
            primaryTumorExtraDetails = Json.optionalString(tumor, "primaryTumorExtraDetails"),
            doids = Json.array(tumor, "doids").map { it.asString }.toCollection(Sets.newHashSet()),
            stage = Json.optionalString(tumor, "stage")?.let { TumorStage.valueOf(it) },
            derivedStages = null,
            hasMeasurableDisease = null,
            hasBrainLesions = null,
            brainLesionsCount = null,
            hasActiveBrainLesions = null,
            hasCnsLesions = null,
            cnsLesionsCount = null,
            hasActiveCnsLesions = null,
            hasBoneLesions = null,
            boneLesionsCount = null,
            hasLiverLesions = null,
            liverLesionsCount = null,
            hasLungLesions = null,
            lungLesionsCount = null,
            hasLymphNodeLesions = null,
            lymphNodeLesionsCount = null,
            otherLesions = null,
            biopsyLocation = null
        )
    }

    private fun tumorStage(optionalTumorStageString: String?): TumorStage? {
        return optionalTumorStageString?.let { TumorStage.valueOf(optionalTumorStageString) }
    }

    private fun extractClinicalStatus(clinical: JsonObject): ClinicalStatus {
        return ClinicalStatus()
    }

    private fun extractOncologicalHistory(clinical: JsonObject): List<TreatmentHistoryEntry> {
        return listOf()
    }

    private fun extractPriorSecondPrimaries(clinical: JsonObject): List<PriorSecondPrimary> {
        return listOf()
    }

    private fun extractPriorOtherConditions(clinical: JsonObject): List<PriorOtherCondition> {
        return listOf()
    }

    private fun extractPriorMolecularTest(clinical: JsonObject): List<PriorMolecularTest> {
        return listOf()
    }

    private fun extractComplications(clinical: JsonObject): List<Complication>? {
        return listOf()
    }

    private fun extractLabValues(clinical: JsonObject): List<LabValue> {
        return listOf()
    }

    private fun extractToxicities(clinical: JsonObject): List<Toxicity> {
        return listOf()
    }

    private fun extractIntolerances(clinical: JsonObject): List<Intolerance> {
        return listOf()
    }

    private fun extractSurgeries(clinical: JsonObject): List<Surgery> {
        return listOf()
    }

    private fun extractBodyWeights(clinical: JsonObject): List<BodyWeight> {
        return listOf()
    }

    private fun extractBodyHeights(clinical: JsonObject): List<BodyHeight> {
        return listOf()
    }

    private fun extractVitalFunctions(clinical: JsonObject): List<VitalFunction> {
        return listOf()
    }

    private fun extractBloodTransfusions(clinical: JsonObject): List<BloodTransfusion> {
        return listOf()
    }

    private fun extractMedications(clinical: JsonObject): List<Medication>? {
        return null
    }

    private fun optionalDate(jsonDate: JsonObject): LocalDate? {
        if (jsonDate.isJsonNull) {
            return null;
        }
        return date(jsonDate)
    }

    private fun date(jsonDate: JsonObject): LocalDate {
        return LocalDate.of(Json.integer(jsonDate, "year"), Json.integer(jsonDate, "month"), Json.integer(jsonDate, "day"))
    }
}