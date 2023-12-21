package com.hartwig.actin.molecular.serialization

import com.google.common.collect.Sets
import com.google.common.io.Resources
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.RefGenomeVersion
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.createExhaustiveTestMolecularRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.createMinimalTestMolecularRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory.createProperTestMolecularRecord
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.driver.CodingContext
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect
import com.hartwig.actin.molecular.datamodel.driver.CopyNumber
import com.hartwig.actin.molecular.datamodel.driver.Disruption
import com.hartwig.actin.molecular.datamodel.driver.DisruptionType
import com.hartwig.actin.molecular.datamodel.driver.Driver
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.Fusion
import com.hartwig.actin.molecular.datamodel.driver.FusionDriverType
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.datamodel.driver.RegionType
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.molecular.datamodel.driver.VariantEffect
import com.hartwig.actin.molecular.datamodel.driver.Virus
import com.hartwig.actin.molecular.datamodel.driver.VirusType
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.createEmpty
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.withApprovedTreatment
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.withPreClinicalTreatment
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry
import com.hartwig.actin.molecular.serialization.MolecularRecordJson.fromJson
import com.hartwig.actin.molecular.serialization.MolecularRecordJson.read
import com.hartwig.actin.molecular.serialization.MolecularRecordJson.toJson
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Offset
import org.junit.Test
import java.io.File
import java.time.LocalDate

class MolecularRecordJsonTest {

    @Test
    fun canConvertBackAndForthJson() {
        val minimal = createMinimalTestMolecularRecord()
        val convertedMinimal = fromJson(toJson(minimal))
        assertThat(convertedMinimal).isEqualTo(minimal)
        
        val proper = createProperTestMolecularRecord()
        val convertedProper = fromJson(toJson(proper))
        assertThat(convertedProper).isEqualTo(proper)
        
        val exhaustive = createExhaustiveTestMolecularRecord()
        val convertedExhaustive = fromJson(toJson(exhaustive))
        assertThat(convertedExhaustive).isEqualTo(exhaustive)
    }

    @Test
    fun canReadMinimalMolecularJson() {
        assertThat(read(MINIMAL_MOLECULAR_JSON)).isNotNull
    }

    @Test
    fun canReadSampleMolecularJson() {
        val molecular = read(SAMPLE_MOLECULAR_JSON)
        assertThat(molecular.patientId).isEqualTo("ACTN01029999")
        assertThat(molecular.sampleId).isEqualTo("ACTN01029999T")
        assertThat(molecular.type).isEqualTo(ExperimentType.WHOLE_GENOME)
        assertThat(molecular.refGenomeVersion).isEqualTo(RefGenomeVersion.V37)
        assertThat(molecular.date).isEqualTo(LocalDate.of(2021, 2, 23))
        assertThat(molecular.evidenceSource).isEqualTo("kb")
        assertThat(molecular.externalTrialSource).isEqualTo("trial kb")
        assertThat(molecular.containsTumorCells).isTrue
        assertThat(molecular.hasSufficientQualityAndPurity).isTrue
        assertThat(molecular.hasSufficientQuality).isTrue
        assertCharacteristics(molecular.characteristics)
        assertDrivers(molecular.drivers)
        assertImmunology(molecular.immunology)
        assertPharmaco(molecular.pharmaco)
    }

    companion object {
        private val MOLECULAR_DIRECTORY = Resources.getResource("molecular").path
        private val SAMPLE_MOLECULAR_JSON = MOLECULAR_DIRECTORY + File.separator + "sample.molecular.json"
        private val MINIMAL_MOLECULAR_JSON = MOLECULAR_DIRECTORY + File.separator + "minimal.molecular.json"
        private const val EPSILON = 1.0E-10

        private fun assertCharacteristics(characteristics: MolecularCharacteristics) {
            assertThat(characteristics.purity!!).isEqualTo(0.98, Offset.offset(EPSILON))
            assertThat(characteristics.ploidy!!).isEqualTo(3.1, Offset.offset(EPSILON))

            val predictedTumorOrigin = characteristics.predictedTumorOrigin
            assertThat(predictedTumorOrigin).isNotNull
            assertThat(predictedTumorOrigin!!.cancerType()).isEqualTo("Melanoma")
            assertThat(predictedTumorOrigin.likelihood()).isEqualTo(0.996, Offset.offset(EPSILON))

            assertThat(characteristics.isMicrosatelliteUnstable).isFalse
            assertThat(characteristics.microsatelliteEvidence).isNull()
            assertThat(characteristics.homologousRepairScore!!).isEqualTo(0.85, Offset.offset(EPSILON))
            assertThat(characteristics.isHomologousRepairDeficient!!).isTrue
            assertThat(characteristics.homologousRepairEvidence).isEqualTo(
                ActionableEvidence(
                    externalEligibleTrials = setOf("PARP trial"),
                    onLabelExperimentalTreatments = setOf("PARP on label"),
                    offLabelExperimentalTreatments = setOf("PARP off label")
                )
            )
            assertThat(characteristics.tumorMutationalBurden!!).isEqualTo(4.32, Offset.offset(EPSILON))
            assertThat(characteristics.hasHighTumorMutationalBurden!!).isTrue
            assertThat(characteristics.tumorMutationalBurdenEvidence).isEqualTo(withApprovedTreatment("Pembro"))
            assertThat((characteristics.tumorMutationalLoad)).isEqualTo(243)
            assertThat(characteristics.hasHighTumorMutationalLoad!!).isTrue
            assertThat(characteristics.tumorMutationalLoadEvidence).isNull()
        }

        private fun assertDrivers(drivers: MolecularDrivers) {
            assertVariants(drivers.variants)
            assertCopyNumbers(drivers.copyNumbers)
            assertHomozygousDisruptions(drivers.homozygousDisruptions)
            assertDisruptions(drivers.disruptions)
            assertFusions(drivers.fusions)
            assertViruses(drivers.viruses)
        }

        private fun assertVariants(variants: Set<Variant>) {
            assertThat(variants).hasSize(1)
            val variant = variants.first()
            assertThat(variant.isReportable).isTrue
            assertThat(variant.event).isEqualTo("BRAF V600E")
            assertThat(variant.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
            assertThat(variant.evidence).isEqualTo(
                ActionableEvidence(
                    knownResistantTreatments = setOf("Anti-BRAF known"),
                    suspectResistantTreatments = setOf("Anti-BRAF suspect")
                )
            )
            assertThat(variant.gene).isEqualTo("BRAF")
            assertThat(variant.geneRole).isEqualTo(GeneRole.ONCO)
            assertThat(variant.proteinEffect).isEqualTo(ProteinEffect.GAIN_OF_FUNCTION)
            assertThat(variant.isAssociatedWithDrugResistance!!).isTrue
            assertThat(variant.variantCopyNumber).isEqualTo(4.1, Offset.offset(EPSILON))
            assertThat(variant.totalCopyNumber).isEqualTo(6.0, Offset.offset(EPSILON))
            assertThat(variant.isBiallelic).isFalse
            assertThat(variant.isHotspot).isTrue
            assertThat(variant.clonalLikelihood).isEqualTo(1.0, Offset.offset(EPSILON))
            assertThat(variant.phaseGroups!!).hasSize(1)
            assertThat(variant.phaseGroups.contains(2)).isTrue

            val canonicalImpact = variant.canonicalImpact
            assertThat(canonicalImpact.transcriptId).isEqualTo("ENST00000288602")
            assertThat(canonicalImpact.hgvsCodingImpact).isEqualTo("c.1799T>A")
            assertThat(canonicalImpact.hgvsProteinImpact).isEqualTo("p.V600E")
            assertThat((canonicalImpact.affectedCodon as Int).toLong()).isEqualTo(600)
            assertThat(canonicalImpact.affectedExon).isNull()
            assertThat(canonicalImpact.isSpliceRegion).isFalse
            assertThat(canonicalImpact.effects).isEqualTo(Sets.newHashSet(VariantEffect.MISSENSE))
            assertThat(canonicalImpact.codingEffect).isEqualTo(CodingEffect.MISSENSE)

            assertThat(variant.otherImpacts).hasSize(1)
            val otherImpact = variant.otherImpacts.first()
            assertThat(otherImpact.transcriptId).isEqualTo("other trans")
            assertThat(otherImpact.hgvsCodingImpact).isEqualTo("c.other")
            assertThat(otherImpact.hgvsProteinImpact).isEqualTo("p.V601K")
            assertThat(otherImpact.affectedCodon).isNull()
            assertThat((otherImpact.affectedExon as Int).toLong()).isEqualTo(8)
            assertThat(otherImpact.isSpliceRegion).isFalse
            assertThat(otherImpact.effects).isEqualTo(Sets.newHashSet(VariantEffect.MISSENSE, VariantEffect.SPLICE_ACCEPTOR))
            assertThat(otherImpact.codingEffect).isNull()
        }

        private fun assertCopyNumbers(copyNumbers: Set<CopyNumber>) {
            assertThat(copyNumbers).hasSize(2)
            val copyNumber1 = findByEvent(copyNumbers, "MYC amp")
            assertThat(copyNumber1.isReportable).isTrue
            assertThat(copyNumber1.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
            assertThat(copyNumber1.evidence).isEqualTo(withPreClinicalTreatment("MYC pre-clinical"))
            assertThat(copyNumber1.gene).isEqualTo("MYC")
            assertThat(copyNumber1.geneRole).isEqualTo(GeneRole.UNKNOWN)
            assertThat(copyNumber1.proteinEffect).isEqualTo(ProteinEffect.UNKNOWN)
            assertThat(copyNumber1.isAssociatedWithDrugResistance).isNull()
            assertThat(copyNumber1.minCopies).isEqualTo(38)
            assertThat(copyNumber1.maxCopies).isEqualTo(39)
            
            val copyNumber2 = findByEvent(copyNumbers, "PTEN del")
            assertThat(copyNumber2.isReportable).isFalse
            assertThat(copyNumber2.driverLikelihood).isNull()
            assertThat(copyNumber2.evidence).isEqualTo(createEmpty())
            assertThat(copyNumber2.gene).isEqualTo("PTEN")
            assertThat(copyNumber2.geneRole).isEqualTo(GeneRole.TSG)
            assertThat(copyNumber2.proteinEffect).isEqualTo(ProteinEffect.LOSS_OF_FUNCTION)
            assertThat(copyNumber2.isAssociatedWithDrugResistance!!).isFalse
            assertThat(copyNumber2.minCopies).isEqualTo(0)
            assertThat(copyNumber2.maxCopies).isEqualTo(2)
        }

        private fun assertHomozygousDisruptions(homozygousDisruptions: Set<HomozygousDisruption>) {
            assertThat(homozygousDisruptions).hasSize(1)
            val homozygousDisruption = homozygousDisruptions.first()
            assertThat(homozygousDisruption.isReportable).isTrue
            assertThat(homozygousDisruption.event).isEqualTo("PTEN hom disruption")
            assertThat(homozygousDisruption.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
            assertThat(homozygousDisruption.evidence).isEqualTo(createEmpty())
            assertThat(homozygousDisruption.gene).isEqualTo("PTEN")
            assertThat(homozygousDisruption.geneRole).isEqualTo(GeneRole.TSG)
            assertThat(homozygousDisruption.proteinEffect).isEqualTo(ProteinEffect.LOSS_OF_FUNCTION)
            assertThat(homozygousDisruption.isAssociatedWithDrugResistance!!).isFalse
        }

        private fun assertDisruptions(disruptions: Set<Disruption>) {
            assertThat(disruptions).hasSize(2)
            val disruption1 = findByEvent(disruptions, "NF1 disruption 1")
            assertThat(disruption1.isReportable).isTrue
            assertThat(disruption1.driverLikelihood).isEqualTo(DriverLikelihood.LOW)
            assertThat(disruption1.evidence).isEqualTo(createEmpty())
            assertThat(disruption1.gene).isEqualTo("NF1")
            assertThat(disruption1.geneRole).isEqualTo(GeneRole.UNKNOWN)
            assertThat(disruption1.proteinEffect).isEqualTo(ProteinEffect.UNKNOWN)
            assertThat(disruption1.isAssociatedWithDrugResistance).isNull()
            assertThat(disruption1.type).isEqualTo(DisruptionType.DEL)
            assertThat(disruption1.junctionCopyNumber).isEqualTo(1.1, Offset.offset(EPSILON))
            assertThat(disruption1.undisruptedCopyNumber).isEqualTo(2.0, Offset.offset(EPSILON))
            assertThat(disruption1.regionType).isEqualTo(RegionType.INTRONIC)
            assertThat(disruption1.codingContext).isEqualTo(CodingContext.NON_CODING)
            assertThat(disruption1.clusterGroup.toLong()).isEqualTo(1)
            
            val disruption2 = findByEvent(disruptions, "NF1 disruption 2")
            assertThat(disruption2.isReportable).isFalse
            assertThat(disruption2.driverLikelihood).isEqualTo(DriverLikelihood.LOW)
            assertThat(disruption2.evidence).isEqualTo(createEmpty())
            assertThat(disruption2.gene).isEqualTo("NF1")
            assertThat(disruption2.geneRole).isEqualTo(GeneRole.UNKNOWN)
            assertThat(disruption2.proteinEffect).isEqualTo(ProteinEffect.NO_EFFECT)
            assertThat(disruption2.junctionCopyNumber).isEqualTo(0.3, Offset.offset(EPSILON))
            assertThat(disruption2.undisruptedCopyNumber).isEqualTo(2.8, Offset.offset(EPSILON))
            assertThat(disruption2.regionType).isEqualTo(RegionType.EXONIC)
            assertThat(disruption2.codingContext).isEqualTo(CodingContext.CODING)
            assertThat(disruption2.clusterGroup).isEqualTo(2)
        }

        private fun assertFusions(fusions: Set<Fusion>) {
            assertThat(fusions).hasSize(1)
            val fusion = fusions.first()
            assertThat(fusion.isReportable).isTrue
            assertThat(fusion.event).isEqualTo("EML4 - ALK fusion")
            assertThat(fusion.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
            assertThat(fusion.evidence).isEqualTo(createEmpty())
            assertThat(fusion.geneStart).isEqualTo("EML4")
            assertThat(fusion.geneTranscriptStart).isEqualTo("ENST00000318522")
            assertThat(fusion.fusedExonUp).isEqualTo(12)
            assertThat(fusion.geneEnd).isEqualTo("ALK")
            assertThat(fusion.geneTranscriptEnd).isEqualTo("ENST00000389048")
            assertThat(fusion.fusedExonDown).isEqualTo(20)
            assertThat(fusion.driverType).isEqualTo(FusionDriverType.KNOWN_PAIR)
            assertThat(fusion.proteinEffect).isEqualTo(ProteinEffect.UNKNOWN)
            assertThat(fusion.isAssociatedWithDrugResistance!!).isFalse
        }

        private fun assertViruses(viruses: Set<Virus>) {
            assertThat(viruses).hasSize(1)
            val virus = viruses.first()
            assertThat(virus.isReportable).isTrue
            assertThat(virus.event).isEqualTo("HPV positive")
            assertThat(virus.driverLikelihood).isEqualTo(DriverLikelihood.HIGH)
            assertThat(virus.evidence).isEqualTo(createEmpty())
            assertThat(virus.name).isEqualTo("Human papillomavirus type 16")
            assertThat(virus.type).isEqualTo(VirusType.HUMAN_PAPILLOMA_VIRUS)
            assertThat(virus.isReliable).isTrue
            assertThat(virus.integrations.toLong()).isEqualTo(3)
        }

        private fun <T : Driver> findByEvent(drivers: Iterable<T>, eventToFind: String): T {
            return drivers.find { it.event == eventToFind }
                ?: throw IllegalStateException("Could not find driver with event: $eventToFind")
        }

        private fun assertImmunology(immunology: MolecularImmunology) {
            assertThat(immunology.hlaAlleles).hasSize(1)
            val hlaAllele = immunology.hlaAlleles.first()
            assertThat(hlaAllele.name).isEqualTo("A*02:01")
            assertThat(hlaAllele.tumorCopyNumber).isEqualTo(1.2, Offset.offset(EPSILON))
            assertThat(hlaAllele.hasSomaticMutations).isFalse
        }

        private fun assertPharmaco(pharmaco: Set<PharmacoEntry?>) {
            assertThat(pharmaco).hasSize(1)
            val entry = pharmaco.first()
            assertThat(entry!!.gene).isEqualTo("DPYD")
            assertThat(entry.haplotypes).hasSize(1)

            val haplotype = entry.haplotypes.first()
            assertThat(haplotype.name).isEqualTo("1* HOM")
            assertThat(haplotype.function).isEqualTo("Normal function")
        }
    }
}