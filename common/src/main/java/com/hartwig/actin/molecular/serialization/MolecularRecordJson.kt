package com.hartwig.actin.molecular.serialization

import com.google.gson.GsonBuilder
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.util.Paths
import com.hartwig.actin.util.json.GsonSerializer
import org.apache.logging.log4j.LogManager
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.nio.file.Files

object MolecularRecordJson {
    private val LOGGER = LogManager.getLogger(MolecularRecordJson::class.java)
    private const val MOLECULAR_JSON_EXTENSION = ".molecular.json"

    fun write(record: MolecularRecord, directory: String) {
        val path = Paths.forceTrailingFileSeparator(directory)
        val jsonFile = path + record.sampleId + MOLECULAR_JSON_EXTENSION
        LOGGER.info("Writing molecular record to {}", jsonFile)
        val writer = BufferedWriter(FileWriter(jsonFile))
        writer.write(toJson(record))
        writer.close()
    }

    fun read(molecularJson: String): MolecularRecord {
        return fromJson(Files.readString(File(molecularJson).toPath()))
    }

    fun toJson(record: MolecularRecord): String {
        return GsonSerializer.create().toJson(record)
    }

    fun fromJson(json: String): MolecularRecord {
//        val gson: Gson = GsonBuilder().registerTypeAdapter(MolecularRecord::class.java, MolecularRecordCreator()).create()
        return GsonBuilder().create().fromJson(json, MolecularRecord::class.java)
    }
    /*
        private class MolecularRecordCreator : JsonDeserializer<MolecularRecord?> {
            
            override fun deserialize(
                jsonElement: JsonElement, type: Type,
                jsonDeserializationContext: JsonDeserializationContext
            ): MolecularRecord {
                val record: JsonObject = jsonElement.getAsJsonObject()
                return ImmutableMolecularRecord.builder()
                    .patientId(string(record, "patientId"))
                    .sampleId(string(record, "sampleId"))
                    .type(ExperimentType.valueOf(string(record, "type")))
                    .refGenomeVersion(RefGenomeVersion.valueOf(string(record, "refGenomeVersion")))
                    .date(nullableDate(record, "date"))
                    .evidenceSource(string(record, "evidenceSource"))
                    .externalTrialSource(string(record, "externalTrialSource"))
                    .containsTumorCells(bool(record, "containsTumorCells"))
                    .hasSufficientQualityAndPurity(bool(record, "hasSufficientQualityAndPurity"))
                    .hasSufficientQuality(bool(record, "hasSufficientQuality"))
                    .characteristics(toMolecularCharacteristics(`object`(record, "characteristics")))
                    .drivers(toMolecularDrivers(`object`(record, "drivers")))
                    .immunology(toMolecularImmunology(`object`(record, "immunology")))
                    .pharmaco(toPharmacoEntries(array(record, "pharmaco")))
                    .build()
            }
    
            companion object {
                private fun toMolecularCharacteristics(characteristics: JsonObject): MolecularCharacteristics {
                    return ImmutableMolecularCharacteristics.builder()
                        .purity(nullableNumber(characteristics, "purity"))
                        .ploidy(nullableNumber(characteristics, "ploidy"))
                        .predictedTumorOrigin(toPredictedTumorOrigin(nullableObject(characteristics, "predictedTumorOrigin")))
                        .isMicrosatelliteUnstable(nullableBool(characteristics, "isMicrosatelliteUnstable"))
                        .microsatelliteEvidence(toNullableActionableEvidence(nullableObject(characteristics, "microsatelliteEvidence")))
                        .homologousRepairScore(nullableNumber(characteristics, "homologousRepairScore"))
                        .isHomologousRepairDeficient(nullableBool(characteristics, "isHomologousRepairDeficient"))
                        .homologousRepairEvidence(toNullableActionableEvidence(nullableObject(characteristics, "homologousRepairEvidence")))
                        .tumorMutationalBurden(nullableNumber(characteristics, "tumorMutationalBurden"))
                        .hasHighTumorMutationalBurden(nullableBool(characteristics, "hasHighTumorMutationalBurden"))
                        .tumorMutationalBurdenEvidence(
                            toNullableActionableEvidence(
                                nullableObject(
                                    characteristics,
                                    "tumorMutationalBurdenEvidence"
                                )
                            )
                        )
                        .tumorMutationalLoad(nullableInteger(characteristics, "tumorMutationalLoad"))
                        .hasHighTumorMutationalLoad(nullableBool(characteristics, "hasHighTumorMutationalLoad"))
                        .tumorMutationalLoadEvidence(
                            toNullableActionableEvidence(
                                nullableObject(
                                    characteristics,
                                    "tumorMutationalLoadEvidence"
                                )
                            )
                        )
                        .build()
                }
    
                private fun toPredictedTumorOrigin(predictedTumorOrigin: JsonObject?): PredictedTumorOrigin? {
                    return if (predictedTumorOrigin == null) {
                        null
                    } else ImmutablePredictedTumorOrigin.builder()
                        .predictions(
                            extractListFromJson(array(predictedTumorOrigin, "predictions")) { prediction: JsonObject ->
                                    ImmutableCupPrediction.builder()
                                        .cancerType(string(prediction, "cancerType"))
                                        .likelihood(number(prediction, "likelihood"))
                                        .snvPairwiseClassifier(number(prediction, "snvPairwiseClassifier"))
                                        .genomicPositionClassifier(number(prediction, "genomicPositionClassifier"))
                                        .featureClassifier(number(prediction, "featureClassifier"))
                                        .build()
                                }
                        )
                        .build()
                }
    
                private fun toNullableActionableEvidence(evidence: JsonObject?): ActionableEvidence? {
                    return if (evidence == null) {
                        null
                    } else toActionableEvidence(evidence)
                }
    
                private fun toMolecularDrivers(drivers: JsonObject): MolecularDrivers {
                    return ImmutableMolecularDrivers.builder()
                        .variants(toVariants(array(drivers, "variants")))
                        .copyNumbers(toCopyNumbers(array(drivers, "copyNumbers")))
                        .homozygousDisruptions(toHomozygousDisruptions(array(drivers, "homozygousDisruptions")))
                        .disruptions(toDisruptions(array(drivers, "disruptions")))
                        .fusions(toFusions(array(drivers, "fusions")))
                        .viruses(toViruses(array(drivers, "viruses")))
                        .build()
                }
    
                private fun toVariants(variantArray: JsonArray): Set<Variant> {
                    val variants: MutableSet<Variant> = Sets.newTreeSet<Variant>(VariantComparator())
                    for (element in variantArray) {
                        val variant: JsonObject = element.asJsonObject
                        variants.add(
                            ImmutableVariant.builder()
                                .from(toDriver(variant))
                                .gene(string(variant, "gene"))
                                .geneRole(GeneRole.valueOf(string(variant, "geneRole")))
                                .proteinEffect(ProteinEffect.valueOf(string(variant, "proteinEffect")))
                                .isAssociatedWithDrugResistance(nullableBool(variant, "isAssociatedWithDrugResistance"))
                                .type(VariantType.valueOf(string(variant, "type")))
                                .variantCopyNumber(number(variant, "variantCopyNumber"))
                                .totalCopyNumber(number(variant, "totalCopyNumber"))
                                .isBiallelic(bool(variant, "isBiallelic"))
                                .isHotspot(bool(variant, "isHotspot"))
                                .clonalLikelihood(number(variant, "clonalLikelihood"))
                                .phaseGroups(nullableIntegerList(variant, "phaseGroups"))
                                .canonicalImpact(toTranscriptImpact(`object`(variant, "canonicalImpact")))
                                .otherImpacts(extractSetFromJson(array(variant, "otherImpacts")) { impact: JsonObject -> toTranscriptImpact(impact) })
                                .build()
                        )
                    }
                    return variants
                }
    
                private fun toTranscriptImpact(impact: JsonObject): TranscriptImpact {
                    return ImmutableTranscriptImpact.builder()
                        .transcriptId(string(impact, "transcriptId"))
                        .hgvsCodingImpact(string(impact, "hgvsCodingImpact"))
                        .hgvsProteinImpact(string(impact, "hgvsProteinImpact"))
                        .affectedCodon(nullableInteger(impact, "affectedCodon"))
                        .affectedExon(nullableInteger(impact, "affectedExon"))
                        .isSpliceRegion(bool(impact, "isSpliceRegion"))
                        .effects(toTranscriptEffects(stringList(impact, "effects")))
                        .codingEffect(toCodingEffect(nullableString(impact, "codingEffect")))
                        .build()
                }
    
                private fun toTranscriptEffects(effectStrings: List<String>): Set<VariantEffect> {
                    val effects: MutableSet<VariantEffect> = Sets.newHashSet<VariantEffect>()
                    for (effect in effectStrings) {
                        effects.add(VariantEffect.valueOf(effect))
                    }
                    return effects
                }
    
                private fun toCodingEffect(codingEffectString: String?): CodingEffect? {
                    return if (codingEffectString != null) CodingEffect.valueOf(codingEffectString) else null
                }
    
                private fun toCopyNumbers(copyNumberArray: JsonArray): Set<CopyNumber> {
                    val copyNumbers: MutableSet<CopyNumber> = Sets.newTreeSet<CopyNumber>(CopyNumberComparator())
                    for (element in copyNumberArray) {
                        val copyNumber: JsonObject = element.asJsonObject
                        copyNumbers.add(
                            ImmutableCopyNumber.builder()
                                .from(toDriver(copyNumber))
                                .gene(string(copyNumber, "gene"))
                                .geneRole(GeneRole.valueOf(string(copyNumber, "geneRole")))
                                .proteinEffect(ProteinEffect.valueOf(string(copyNumber, "proteinEffect")))
                                .isAssociatedWithDrugResistance(nullableBool(copyNumber, "isAssociatedWithDrugResistance"))
                                .type(CopyNumberType.valueOf(string(copyNumber, "type")))
                                .minCopies(integer(copyNumber, "minCopies"))
                                .maxCopies(integer(copyNumber, "maxCopies"))
                                .build()
                        )
                    }
                    return copyNumbers
                }
    
                private fun toHomozygousDisruptions(homozygousDisruptionArray: JsonArray): Set<HomozygousDisruption> {
                    val homozygousDisruptions: MutableSet<HomozygousDisruption> =
                        Sets.newTreeSet<HomozygousDisruption>(HomozygousDisruptionComparator())
                    for (element in homozygousDisruptionArray) {
                        val homozygousDisruption: JsonObject = element.asJsonObject
                        homozygousDisruptions.add(
                            ImmutableHomozygousDisruption.builder()
                                .from(toDriver(homozygousDisruption))
                                .gene(string(homozygousDisruption, "gene"))
                                .geneRole(GeneRole.valueOf(string(homozygousDisruption, "geneRole")))
                                .proteinEffect(ProteinEffect.valueOf(string(homozygousDisruption, "proteinEffect")))
                                .isAssociatedWithDrugResistance(nullableBool(homozygousDisruption, "isAssociatedWithDrugResistance"))
                                .build()
                        )
                    }
                    return homozygousDisruptions
                }
    
                private fun toDisruptions(disruptionArray: JsonArray): Set<Disruption> {
                    val disruptions: MutableSet<Disruption> = Sets.newTreeSet<Disruption>(DisruptionComparator())
                    for (element in disruptionArray) {
                        val disruption: JsonObject = element.asJsonObject
                        disruptions.add(
                            ImmutableDisruption.builder()
                                .from(toDriver(disruption))
                                .gene(string(disruption, "gene"))
                                .geneRole(GeneRole.valueOf(string(disruption, "geneRole")))
                                .proteinEffect(ProteinEffect.valueOf(string(disruption, "proteinEffect")))
                                .isAssociatedWithDrugResistance(nullableBool(disruption, "isAssociatedWithDrugResistance"))
                                .type(DisruptionType.valueOf(string(disruption, "type")))
                                .junctionCopyNumber(number(disruption, "junctionCopyNumber"))
                                .undisruptedCopyNumber(number(disruption, "undisruptedCopyNumber"))
                                .regionType(RegionType.valueOf(string(disruption, "regionType")))
                                .codingContext(CodingContext.valueOf(string(disruption, "codingContext")))
                                .clusterGroup(integer(disruption, "clusterGroup"))
                                .build()
                        )
                    }
                    return disruptions
                }
    
                private fun toFusions(fusionArray: JsonArray): Set<Fusion> {
                    val fusions: MutableSet<Fusion> = Sets.newTreeSet<Fusion>(FusionComparator())
                    for (element in fusionArray) {
                        val fusion: JsonObject = element.asJsonObject
                        fusions.add(
                            ImmutableFusion.builder()
                                .from(toDriver(fusion))
                                .geneStart(string(fusion, "geneStart"))
                                .geneTranscriptStart(string(fusion, "geneTranscriptStart"))
                                .fusedExonUp(integer(fusion, "fusedExonUp"))
                                .geneEnd(string(fusion, "geneEnd"))
                                .geneTranscriptEnd(string(fusion, "geneTranscriptEnd"))
                                .fusedExonDown(integer(fusion, "fusedExonDown"))
                                .proteinEffect(ProteinEffect.valueOf(string(fusion, "proteinEffect")))
                                .isAssociatedWithDrugResistance(nullableBool(fusion, "isAssociatedWithDrugResistance"))
                                .driverType(FusionDriverType.valueOf(string(fusion, "driverType")))
                                .build()
                        )
                    }
                    return fusions
                }
    
                private fun toViruses(virusArray: JsonArray): Set<Virus> {
                    val viruses: MutableSet<Virus> = Sets.newTreeSet<Virus>(VirusComparator())
                    for (element in virusArray) {
                        val virus: JsonObject = element.asJsonObject
                        viruses.add(
                            ImmutableVirus.builder()
                                .from(toDriver(virus))
                                .name(string(virus, "name"))
                                .type(VirusType.valueOf(string(virus, "type")))
                                .isReliable(bool(virus, "isReliable"))
                                .integrations(integer(virus, "integrations"))
                                .build()
                        )
                    }
                    return viruses
                }
    
                private fun toDriver(`object`: JsonObject): Driver {
                    return object : Driver {
                        override val isReportable: Boolean
                            get() = bool(`object`, "isReportable")
    
                        override val event: String
                            get() = string(`object`, "event")
    
                        override val driverLikelihood: DriverLikelihood?
                            get() = toDriverLikelihood(nullableString(`object`, "driverLikelihood"))
    
                        override val evidence: ActionableEvidence
                            get() = toActionableEvidence(`object`(`object`, "evidence"))
                    }
                }
    
                private fun toDriverLikelihood(driverLikelihoodString: String?): DriverLikelihood? {
                    return if (driverLikelihoodString != null) DriverLikelihood.valueOf(driverLikelihoodString) else null
                }
    
                private fun toActionableEvidence(evidence: JsonObject): ActionableEvidence {
                    return ImmutableActionableEvidence.builder()
                        .approvedTreatments(stringList(evidence, "approvedTreatments"))
                        .externalEligibleTrials(stringList(evidence, "externalEligibleTrials"))
                        .onLabelExperimentalTreatments(stringList(evidence, "onLabelExperimentalTreatments"))
                        .offLabelExperimentalTreatments(stringList(evidence, "offLabelExperimentalTreatments"))
                        .preClinicalTreatments(stringList(evidence, "preClinicalTreatments"))
                        .knownResistantTreatments(stringList(evidence, "knownResistantTreatments"))
                        .suspectResistantTreatments(stringList(evidence, "suspectResistantTreatments"))
                        .build()
                }
    
                private fun toMolecularImmunology(immunology: JsonObject): MolecularImmunology {
                    return ImmutableMolecularImmunology.builder()
                        .isReliable(bool(immunology, "isReliable"))
                        .hlaAlleles(toHlaAlleles(array(immunology, "hlaAlleles")))
                        .build()
                }
    
                private fun toHlaAlleles(hlaAlleleArray: JsonArray): Set<HlaAllele> {
                    return extractSetFromJson<HlaAllele>(hlaAlleleArray,
                        Function<JsonObject, HlaAllele> { hlaAllele: JsonObject ->
                            ImmutableHlaAllele.builder()
                                .name(string(hlaAllele, "name"))
                                .tumorCopyNumber(number(hlaAllele, "tumorCopyNumber"))
                                .hasSomaticMutations(bool(hlaAllele, "hasSomaticMutations"))
                                .build()
                        })
                }
    
                private fun toPharmacoEntries(pharmacoArray: JsonArray): Set<PharmacoEntry> {
                    return extractSetFromJson<PharmacoEntry>(pharmacoArray,
                        Function<JsonObject, PharmacoEntry> { pharmaco: JsonObject ->
                            ImmutablePharmacoEntry.builder()
                                .gene(string(pharmaco, "gene"))
                                .haplotypes(toHaplotypes(array(pharmaco, "haplotypes")))
                                .build()
                        })
                }
    
                private fun toHaplotypes(haplotypeArray: JsonArray): Set<Haplotype> {
                    return extractSetFromJson<Haplotype>(haplotypeArray,
                        Function<JsonObject, Haplotype> { haplotype: JsonObject ->
                            ImmutableHaplotype.builder()
                                .name(string(haplotype, "name"))
                                .function(string(haplotype, "function"))
                                .build()
                        })
                }
            }
        }
        
     */
}
