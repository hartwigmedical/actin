package com.hartwig.actin.database.historic.serialization

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.PredictedTumorOrigin
import com.hartwig.actin.molecular.datamodel.RefGenomeVersion
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.molecular.datamodel.orange.characteristics.CupPrediction
import com.hartwig.actin.molecular.datamodel.orange.immunology.MolecularImmunology
import com.hartwig.actin.molecular.datamodel.orange.pharmaco.PharmacoEntry
import com.hartwig.actin.util.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.FileReader

object HistoricMolecularDeserializer {

    private val LOGGER: Logger = LogManager.getLogger(HistoricClinicalDeserializer::class.java)

    fun deserialize(molecularJson: File): MolecularHistory {
        val reader = JsonReader(FileReader(molecularJson))
        val molecular: JsonObject = JsonParser.parseReader(reader).asJsonObject

        val hasSufficientQuality = determineSufficientQuality(molecular)

        val molecularTest = MolecularRecord(
            patientId = determinePatientId(molecular),
            sampleId = Json.string(molecular, "sampleId"),
            refGenomeVersion = determineRefGenomeVersion(molecular),
            externalTrialSource = Json.optionalString(molecular, "externalTrialSource") ?: "",
            containsTumorCells = Json.optionalBool(molecular, "containsTumorCells") ?: hasSufficientQuality,
            isContaminated = !hasSufficientQuality,
            hasSufficientPurity = Json.optionalBool(molecular, "hasReliablePurity") ?: hasSufficientQuality,
            hasSufficientQuality = hasSufficientQuality,
            immunology = extractImmunology(molecular),
            pharmaco = extractPharmaco(molecular),
            experimentType = determineExperimentType(molecular),
            date = Json.date(molecular, "date"),
            drivers = extractDrivers(Json.`object`(molecular, "drivers")),
            characteristics = extractCharacteristics(Json.`object`(molecular, "characteristics")),
            evidenceSource = Json.optionalString(molecular, "evidenceSource") ?: "",
        )

        if (reader.peek() != JsonToken.END_DOCUMENT) {
            LOGGER.warn("More data found in {} after reading main molecular JSON object!", molecularJson)
        }

        return MolecularHistory(listOf(molecularTest))
    }

    private fun determineExperimentType(molecular: JsonObject): ExperimentType {
        return if (molecular.has("type")) {
            when (val type = Json.string(molecular, "type")) {
                "WGS" -> ExperimentType.HARTWIG_WHOLE_GENOME
                else -> ExperimentType.valueOf(type)
            }
        } else {
            ExperimentType.HARTWIG_WHOLE_GENOME
        }
    }

    private fun determineSufficientQuality(molecular: JsonObject): Boolean {
        return if (molecular.has("hasReliableQuality")) {
            Json.bool(molecular, "hasReliableQuality")
        } else {
            Json.bool(molecular, "hasSufficientQuality")
        }
    }

    private fun determinePatientId(molecular: JsonObject): String {
        return if (molecular.has("patientId")) {
            Json.string(molecular, "patientId")
        } else {
            val sample: String = Json.string(molecular, "sampleId")
            return sample.substring(0, 12)
        }
    }

    private fun determineRefGenomeVersion(molecular: JsonObject): RefGenomeVersion {
        return if (molecular.has("refGenomeVersion")) {
            RefGenomeVersion.valueOf(Json.string(molecular, "refGenomeVersion"))
        } else {
            RefGenomeVersion.V37
        }
    }

    private fun extractImmunology(molecularObject: JsonObject): MolecularImmunology {
        return MolecularImmunology(
            isReliable = false,
            hlaAlleles = setOf()
        )
    }

    private fun extractPharmaco(molecularObject: JsonObject): Set<PharmacoEntry> {
        return setOf()
    }

    private fun extractDrivers(drivers: JsonObject): Drivers {
        return Drivers(
            variants = setOf(),
            copyNumbers = emptySet(),
            homozygousDisruptions = emptySet(),
            disruptions = emptySet(),
            fusions = emptySet(),
            viruses = emptySet()
        )
    }

    private fun extractCharacteristics(characteristics: JsonObject): MolecularCharacteristics {
        return MolecularCharacteristics(
            purity = Json.nullableDouble(characteristics, "purity"),
            ploidy = Json.optionalDouble(characteristics, "ploidy"),
            predictedTumorOrigin = extractPredictedTumorOrigin(Json.nullableObject(characteristics, "predictedTumorOrigin")),
            isMicrosatelliteUnstable = Json.nullableBool(characteristics, "isMicrosatelliteUnstable"),
            microsatelliteEvidence = extractEvidence(Json.optionalObject(characteristics, "microsatelliteEvidence")),
            homologousRepairScore = null,
            isHomologousRepairDeficient = Json.nullableBool(characteristics, "isHomologousRepairDeficient"),
            homologousRepairEvidence = extractEvidence(Json.optionalObject(characteristics, "homologousRepairEvidence")),
            tumorMutationalBurden = Json.nullableDouble(characteristics, "tumorMutationalBurden"),
            hasHighTumorMutationalBurden = Json.optionalBool(characteristics, "hasHighTumorMutationalBurde"),
            tumorMutationalBurdenEvidence = extractEvidence(Json.optionalObject(characteristics, "tumorMutationalBurdenEvidence")),
            tumorMutationalLoad = Json.nullableInteger(characteristics, "tumorMutationalLoad"),
            hasHighTumorMutationalLoad = Json.optionalBool(characteristics, "hasHighTumorMutationalLoad"),
            tumorMutationalLoadEvidence = extractEvidence(Json.optionalObject(characteristics, "tumorMutationalLoadEvidence"))
        )
    }

    private fun extractPredictedTumorOrigin(predictedTumorOrigin: JsonObject?): PredictedTumorOrigin? {
        return predictedTumorOrigin?.let {
            PredictedTumorOrigin(
                predictions = listOf(
                    CupPrediction(
                        cancerType = Json.string(it, "tumorType"),
                        likelihood = Json.double(it, "likelihood"),
                        snvPairwiseClassifier = 0.0,
                        genomicPositionClassifier = 0.0,
                        featureClassifier = 0.0
                    )
                )
            )
        }
    }

    private fun extractEvidence(evidence: JsonObject?): ActionableEvidence? {
        return evidence?.let {
            ActionableEvidence(
                approvedTreatments = HashSet(Json.stringList(it, "approvedTreatments")),
                externalEligibleTrials = toExternalTrials(Json.stringList(it, "externalEligibleTrials")),
                onLabelExperimentalTreatments = HashSet(Json.stringList(it, "onLabelExperimentalTreatments")),
                offLabelExperimentalTreatments = HashSet(Json.stringList(it, "offLabelExperimentalTreatments")),
                preClinicalTreatments = HashSet(Json.stringList(it, "preClinicalTreatments")),
                knownResistantTreatments = HashSet(Json.stringList(it, "knownResistantTreatments")),
                suspectResistantTreatments = HashSet(Json.stringList(it, "suspectResistantTreatments"))
            )
        }
    }

    private fun toExternalTrials(externalTrials: List<String>): Set<ExternalTrial> {
        return HashSet(externalTrials.map {
            ExternalTrial(
                title = it,
                countries = setOf(),
                url = "",
                nctId = ""
            )
        })
    }
}