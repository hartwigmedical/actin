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
import com.hartwig.actin.molecular.datamodel.RefGenomeVersion
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
        val molecularObject: JsonObject = JsonParser.parseReader(reader).asJsonObject

        val molecularTest = MolecularRecord(
            patientId = extractPatientId(molecularObject),
            sampleId = Json.string(molecularObject, "sampleId"),
            refGenomeVersion = RefGenomeVersion.V37,
            externalTrialSource = "",
            containsTumorCells = false,
            isContaminated = false,
            hasSufficientPurity = false,
            hasSufficientQuality = false,
            immunology = extractImmunology(molecularObject),
            pharmaco = extractPharmaco(molecularObject),
            experimentType = ExperimentType.HARTWIG_WHOLE_GENOME,
            date = Json.date(molecularObject, "date"),
            drivers = extractDrivers(molecularObject),
            characteristics = extractCharacteristics(molecularObject),
            evidenceSource = ""
        )

        if (reader.peek() != JsonToken.END_DOCUMENT) {
            LOGGER.warn("More data found in {} after reading main molecular JSON object!", molecularJson)
        }

        return MolecularHistory(listOf(molecularTest))
    }

    private fun extractPatientId(molecular: JsonObject): String {
        val sample: String = Json.string(molecular, "sampleId")
        return sample.substring(0, 12)
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

    private fun extractDrivers(molecularObject: JsonObject): Drivers {
        return Drivers(
            variants = setOf(),
            copyNumbers = emptySet(),
            homozygousDisruptions = emptySet(),
            disruptions = emptySet(),
            fusions = emptySet(),
            viruses = emptySet()
        )
    }

    private fun extractCharacteristics(molecularObject: JsonObject): MolecularCharacteristics {
        return MolecularCharacteristics(
            purity = null,
            ploidy = null,
            predictedTumorOrigin = null,
            isMicrosatelliteUnstable = null,
            microsatelliteEvidence = null,
            homologousRepairScore = null,
            isHomologousRepairDeficient = null,
            homologousRepairEvidence = null,
            tumorMutationalBurden = null,
            hasHighTumorMutationalBurden = null,
            tumorMutationalBurdenEvidence = null,
            tumorMutationalLoad = null,
            hasHighTumorMutationalLoad = null,
            tumorMutationalLoadEvidence = null
        )
    }
}