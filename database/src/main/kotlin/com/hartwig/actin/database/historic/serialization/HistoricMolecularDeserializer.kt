package com.hartwig.actin.database.historic.serialization

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.Fusion
import com.hartwig.actin.molecular.datamodel.GeneRole
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.PredictedTumorOrigin
import com.hartwig.actin.molecular.datamodel.ProteinEffect
import com.hartwig.actin.molecular.datamodel.RefGenomeVersion
import com.hartwig.actin.molecular.datamodel.TranscriptImpact
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.VariantType
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.orange.characteristics.CupPrediction
import com.hartwig.actin.molecular.datamodel.orange.driver.CodingContext
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumberType
import com.hartwig.actin.molecular.datamodel.orange.driver.Disruption
import com.hartwig.actin.molecular.datamodel.orange.driver.DisruptionType
import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedFusionDetails
import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedVariantDetails
import com.hartwig.actin.molecular.datamodel.orange.driver.FusionDriverType
import com.hartwig.actin.molecular.datamodel.orange.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.orange.driver.RegionType
import com.hartwig.actin.molecular.datamodel.orange.driver.Virus
import com.hartwig.actin.molecular.datamodel.orange.driver.VirusType
import com.hartwig.actin.molecular.datamodel.orange.immunology.MolecularImmunology
import com.hartwig.actin.molecular.datamodel.orange.pharmaco.Haplotype
import com.hartwig.actin.molecular.datamodel.orange.pharmaco.PharmacoEntry
import com.hartwig.actin.util.json.Json
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.io.FileReader

object HistoricMolecularDeserializer {

    private val logger: Logger = LogManager.getLogger(HistoricMolecularDeserializer::class.java)
    private val gson = GsonBuilder().create()

    fun deserialize(molecularJson: File): MolecularHistory {
        val reader = JsonReader(FileReader(molecularJson))
        val molecular = JsonParser.parseReader(reader).asJsonObject

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
            immunology = MolecularImmunology(isReliable = false, hlaAlleles = setOf()),
            pharmaco = extractPharmaco(molecular),
            experimentType = determineExperimentType(molecular),
            date = Json.date(molecular, "date"),
            drivers = extractDrivers(Json.`object`(molecular, "drivers")),
            characteristics = extractCharacteristics(Json.`object`(molecular, "characteristics")),
            evidenceSource = Json.optionalString(molecular, "evidenceSource") ?: "",
        )

        if (reader.peek() != JsonToken.END_DOCUMENT) {
            logger.warn("More data found in {} after reading main molecular JSON object!", molecularJson)
        }

        return MolecularHistory(listOf(molecularTest))
    }

    private fun Json.optionalInteger(obj: JsonObject, field: String): Int? {
        return if (obj.has(field)) nullableInteger(obj, field) else null
    }

    private fun determineExperimentType(molecular: JsonObject): ExperimentType {
        return when (val type = Json.optionalString(molecular, "type")) {
            "WGS", "WHOLE_GENOME", null -> ExperimentType.HARTWIG_WHOLE_GENOME
            else -> ExperimentType.valueOf(type)
        }
    }

    private fun determineSufficientQuality(molecular: JsonObject): Boolean {
        return Json.optionalBool(molecular, "hasReliableQuality") ?: Json.bool(molecular, "hasSufficientQuality")
    }

    private fun determinePatientId(molecular: JsonObject): String {
        return Json.optionalString(molecular, "patientId") ?: Json.string(molecular, "sampleId").substring(0, 12)
    }

    private fun determineRefGenomeVersion(molecular: JsonObject): RefGenomeVersion {
        return Json.optionalString(molecular, "refGenomeVersion")?.let(RefGenomeVersion::valueOf) ?: RefGenomeVersion.V37
    }

    private fun extractPharmaco(molecular: JsonObject): Set<PharmacoEntry> {
        return Json.array(molecular, "pharmaco").map { element ->
            val obj = element.asJsonObject
            PharmacoEntry(
                gene = Json.string(obj, "gene"),
                haplotypes = Json.array(obj, "haplotypes").map { haploJson ->
                    val haplo = haploJson.asJsonObject
                    Haplotype(
                        function = Json.string(haplo, "function"),
                        name = Json.string(haplo, "name")
                    )
                }.toSet()
            )
        }.toSet()
    }

    private fun extractDrivers(drivers: JsonObject): Drivers {
        return Drivers(
            variants = extractDriversFromField(drivers, "variants", ::extractVariant),
            copyNumbers = extractCopyNumbers(drivers),
            homozygousDisruptions = extractDriversFromField(drivers, "homozygousDisruptions", ::extractHomozygousDisruption),
            disruptions = extractDriversFromField(drivers, "disruptions", ::extractDisruption),
            fusions = extractDriversFromField(drivers, "fusions", ::extractFusion),
            viruses = extractDriversFromField(drivers, "viruses", ::extractVirus)
        )
    }

    private fun <T> extractDriversFromField(drivers: JsonObject, field: String, convertJson: (JsonElement) -> T): Set<T> {
        return Json.array(drivers, field).map(convertJson).toSet()
    }

    private fun extractVariant(variantElement: JsonElement): Variant {
        val obj = variantElement.asJsonObject
        return Variant(
            chromosome = "",
            position = 0,
            ref = "",
            alt = "",
            type = VariantType.UNDEFINED,
            canonicalImpact = extractCanonicalImpact(obj),
            otherImpacts = emptySet(),
            extendedVariantDetails = Json.optionalBool(obj, "isBiallelic")?.let { extractExtendedVariantDetails(obj, it) },
            isHotspot = Json.optionalBool(obj, "isHotspot")
                ?: Json.optionalString(obj, "driverType")?.let { it.uppercase() == "HOTSPOT" }
                ?: false,
            isReportable = determineIsReportable(obj),
            event = Json.string(obj, "event"),
            driverLikelihood = determineDriverLikelihood(obj),
            evidence = ActionableEvidence(),
            gene = Json.string(obj, "gene"),
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = null
        )
    }

    private fun extractCanonicalImpact(variant: JsonObject): TranscriptImpact {
        return Json.optionalObject(variant, "canonicalImpact")?.let { impactJson ->
            try {
                val impact = gson.fromJson(impactJson, TranscriptImpact::class.java)
                impact.hashCode()
                impact
            } catch (_: Exception) {
                logger.info("Failure deserializing: {}", impactJson)
                null
            }
        } ?: TranscriptImpact(
            transcriptId = "",
            hgvsCodingImpact = "",
            hgvsProteinImpact = "",
            affectedCodon = null,
            affectedExon = null,
            isSpliceRegion = null,
            effects = emptySet(),
            codingEffect = null
        )
    }

    private fun extractExtendedVariantDetails(variant: JsonObject, isBiallelic: Boolean) = ExtendedVariantDetails(
        variantCopyNumber = Json.double(variant, "variantCopyNumber"),
        totalCopyNumber = Json.double(variant, "totalCopyNumber"),
        isBiallelic = isBiallelic,
        phaseGroups = null,
        clonalLikelihood = Json.double(variant, "clonalLikelihood")
    )

    private fun extractCopyNumbers(drivers: JsonObject): Set<CopyNumber> {
        return sequenceOf("copyNumbers" to null, "amplifications" to CopyNumberType.FULL_GAIN, "losses" to CopyNumberType.LOSS)
            .flatMap { (field, type) -> Json.optionalArray(drivers, field)?.map { extractCopyNumber(it, type) } ?: emptyList() }
            .toSet()
    }

    private fun extractCopyNumber(copyNumberElement: JsonElement, typeGroup: CopyNumberType?): CopyNumber {
        val obj = copyNumberElement.asJsonObject
        val type = when {
            typeGroup == CopyNumberType.FULL_GAIN && Json.bool(obj, "isPartial") -> CopyNumberType.PARTIAL_GAIN
            typeGroup == null -> CopyNumberType.valueOf(Json.string(obj, "type"))
            else -> typeGroup
        }
        return CopyNumber(
            type = type,
            isReportable = determineIsReportable(obj),
            event = Json.string(obj, "event"),
            driverLikelihood = determineDriverLikelihood(obj),
            evidence = ActionableEvidence(),
            gene = Json.string(obj, "gene"),
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = null,
            minCopies = determineCopies(obj, "minCopies", type),
            maxCopies = determineCopies(obj, "maxCopies", type)
        )
    }

    private fun determineIsReportable(obj: JsonObject): Boolean = Json.optionalBool(obj, "isReportable") ?: true

    private fun determineDriverLikelihood(driver: JsonObject): DriverLikelihood? =
        Json.nullableString(driver, "driverLikelihood")?.let(DriverLikelihood::valueOf)

    private fun determineCopies(copyNumber: JsonObject, field: String, type: CopyNumberType): Int =
        Json.optionalInteger(copyNumber, field) ?: Json.optionalInteger(copyNumber, "copies") ?: if (type == CopyNumberType.LOSS) 0 else 2

    private fun extractHomozygousDisruption(homozygousDisruptionElement: JsonElement): HomozygousDisruption {
        val obj = homozygousDisruptionElement.asJsonObject
        return HomozygousDisruption(
            isReportable = determineIsReportable(obj),
            event = Json.string(obj, "event"),
            driverLikelihood = determineDriverLikelihood(obj),
            evidence = ActionableEvidence(),
            gene = Json.string(obj, "gene"),
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = null
        )
    }

    private fun extractDisruption(disruptionElement: JsonElement): Disruption {
        val obj = disruptionElement.asJsonObject
        val isReportable = determineIsReportable(obj)
        return Disruption(
            type = DisruptionType.SGL,
            junctionCopyNumber = 0.0,
            undisruptedCopyNumber = 0.0,
            regionType = RegionType.UPSTREAM,
            codingContext = CodingContext.NON_CODING,
            clusterGroup = 0,
            isReportable = isReportable,
            event = Json.string(obj, "event"),
            driverLikelihood = if (isReportable) determineDriverLikelihood(obj) else null,
            evidence = ActionableEvidence(),
            gene = Json.string(obj, "gene"),
            geneRole = GeneRole.UNKNOWN,
            proteinEffect = ProteinEffect.UNKNOWN,
            isAssociatedWithDrugResistance = null
        )
    }

    private fun extractFusion(fusionElement: JsonElement): Fusion {
        val obj = fusionElement.asJsonObject
        return Fusion(
            geneStart = Json.optionalString(obj, "geneStart") ?: Json.string(obj, "fiveGene"),
            geneEnd = Json.optionalString(obj, "geneEnd") ?: Json.string(obj, "threeGene"),
            geneTranscriptStart = "",
            geneTranscriptEnd = "",
            driverType = determineFusionDriverType(Json.string(obj, "driverType")),
            proteinEffect = ProteinEffect.UNKNOWN,
            extendedFusionDetails = extractExtendedFusionDetails(obj),
            isReportable = determineIsReportable(obj),
            event = Json.string(obj, "event"),
            driverLikelihood = determineDriverLikelihood(obj),
            evidence = ActionableEvidence()
        )
    }

    private fun extractExtendedFusionDetails(fusion: JsonObject): ExtendedFusionDetails {
        return ExtendedFusionDetails(
            fusedExonUp = Json.optionalInteger(fusion, "fusedExonUp") ?: 0,
            fusedExonDown = Json.optionalInteger(fusion, "fusedExonDown") ?: 0,
            isAssociatedWithDrugResistance = null
        )
    }

    private fun determineFusionDriverType(str: String): FusionDriverType {
        return when (str) {
            "KNOWN", "KNOWN_PAIR" -> FusionDriverType.KNOWN_PAIR
            "PROMISCUOUS" -> FusionDriverType.PROMISCUOUS_BOTH
            "PROMISCUOUS_3" -> FusionDriverType.PROMISCUOUS_3
            "PROMISCUOUS_5" -> FusionDriverType.PROMISCUOUS_5
            "PROMISCUOUS_ENHANCER_TARGET" -> FusionDriverType.PROMISCUOUS_ENHANCER_TARGET
            else -> FusionDriverType.NONE
        }
    }

    private fun extractVirus(virusElement: JsonElement): Virus {
        val obj = virusElement.asJsonObject
        return Virus(
            name = Json.string(obj, "name"),
            type = VirusType.OTHER,
            isReliable = true,
            integrations = Json.integer(obj, "integrations"),
            isReportable = determineIsReportable(obj),
            event = Json.string(obj, "event"),
            driverLikelihood = determineDriverLikelihood(obj),
            evidence = ActionableEvidence()
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
            hasHighTumorMutationalBurden = Json.optionalBool(characteristics, "hasHighTumorMutationalBurden"),
            tumorMutationalBurdenEvidence = extractEvidence(Json.optionalObject(characteristics, "tumorMutationalBurdenEvidence")),
            tumorMutationalLoad = Json.nullableInteger(characteristics, "tumorMutationalLoad"),
            hasHighTumorMutationalLoad = Json.optionalBool(characteristics, "hasHighTumorMutationalLoad"),
            tumorMutationalLoadEvidence = extractEvidence(Json.optionalObject(characteristics, "tumorMutationalLoadEvidence"))
        )
    }

    private fun extractPredictedTumorOrigin(predictedTumorOrigin: JsonObject?): PredictedTumorOrigin? {
        return predictedTumorOrigin?.let {
            PredictedTumorOrigin(predictions = extractPredictions(it))
        }
    }

    private fun extractPredictions(predictedTumorOrigin: JsonObject): List<CupPrediction> {
        return Json.optionalArray(predictedTumorOrigin, "predictions")?.map { toCupPrediction(it.asJsonObject) }
            ?: listOf(toCupPrediction(predictedTumorOrigin))
    }

    private fun toCupPrediction(cupPrediction: JsonObject): CupPrediction {
        return CupPrediction(
            cancerType = extractCancerType(cupPrediction),
            likelihood = Json.double(cupPrediction, "likelihood"),
            snvPairwiseClassifier = Json.optionalDouble(cupPrediction, "snvPairwiseClassifier") ?: 0.0,
            genomicPositionClassifier = Json.optionalDouble(cupPrediction, "genomicPositionClassifier") ?: 0.0,
            featureClassifier = Json.optionalDouble(cupPrediction, "featureClassifier") ?: 0.0
        )
    }

    private fun extractCancerType(prediction: JsonObject): String {
        return Json.optionalString(prediction, "tumorType") ?: Json.string(prediction, "cancerType")
    }

    private fun extractEvidence(evidence: JsonObject?): ActionableEvidence? {
        // (KD): This is explicitly not read since it contains confidential data (from CKB).
        return evidence?.let {
            ActionableEvidence(
                approvedTreatments = emptySet(),
                externalEligibleTrials = emptySet(),
                onLabelExperimentalTreatments = emptySet(),
                offLabelExperimentalTreatments = emptySet(),
                preClinicalTreatments = emptySet(),
                knownResistantTreatments = emptySet(),
                suspectResistantTreatments = emptySet()
            )
        }
    }
}