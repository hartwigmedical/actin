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
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.builder
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.createEmpty
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.withApprovedTreatment
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory.withPreClinicalTreatment
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry
import com.hartwig.actin.molecular.serialization.MolecularRecordJson.fromJson
import com.hartwig.actin.molecular.serialization.MolecularRecordJson.read
import com.hartwig.actin.molecular.serialization.MolecularRecordJson.toJson
import org.junit.Assert
import org.junit.Test
import java.io.File
import java.io.IOException
import java.time.LocalDate

class MolecularRecordJsonTest {
    @Test
    fun canConvertBackAndForthJson() {
        val minimal = createMinimalTestMolecularRecord()
        val convertedMinimal = fromJson(toJson(minimal))
        Assert.assertEquals(minimal, convertedMinimal)
        val proper = createProperTestMolecularRecord()
        val convertedProper = fromJson(toJson(proper))
        Assert.assertEquals(proper, convertedProper)
        val exhaustive = createExhaustiveTestMolecularRecord()
        val convertedExhaustive = fromJson(toJson(exhaustive))
        Assert.assertEquals(exhaustive, convertedExhaustive)
    }

    @Test
    @Throws(IOException::class)
    fun canReadMinimalMolecularJson() {
        Assert.assertNotNull(read(MINIMAL_MOLECULAR_JSON))
    }

    @Test
    @Throws(IOException::class)
    fun canReadSampleMolecularJson() {
        val molecular = read(SAMPLE_MOLECULAR_JSON)
        Assert.assertEquals("ACTN01029999", molecular.patientId())
        Assert.assertEquals("ACTN01029999T", molecular.sampleId())
        Assert.assertEquals(ExperimentType.WHOLE_GENOME, molecular.type())
        Assert.assertEquals(RefGenomeVersion.V37, molecular.refGenomeVersion())
        Assert.assertEquals(LocalDate.of(2021, 2, 23), molecular.date())
        Assert.assertEquals("kb", molecular.evidenceSource())
        Assert.assertEquals("trial kb", molecular.externalTrialSource())
        Assert.assertTrue(molecular.containsTumorCells())
        Assert.assertTrue(molecular.hasSufficientQualityAndPurity())
        Assert.assertTrue(molecular.hasSufficientQuality())
        assertCharacteristics(molecular.characteristics())
        assertDrivers(molecular.drivers())
        assertImmunology(molecular.immunology())
        assertPharmaco(molecular.pharmaco())
    }

    companion object {
        private val MOLECULAR_DIRECTORY = Resources.getResource("molecular").path
        private val SAMPLE_MOLECULAR_JSON = MOLECULAR_DIRECTORY + File.separator + "sample.molecular.json"
        private val MINIMAL_MOLECULAR_JSON = MOLECULAR_DIRECTORY + File.separator + "minimal.molecular.json"
        private const val EPSILON = 1.0E-10
        private fun assertCharacteristics(characteristics: MolecularCharacteristics) {
            Assert.assertEquals(0.98, characteristics.purity()!!, EPSILON)
            Assert.assertEquals(3.1, characteristics.ploidy()!!, EPSILON)
            val predictedTumorOrigin = characteristics.predictedTumorOrigin()
            Assert.assertNotNull(predictedTumorOrigin)
            Assert.assertEquals("Melanoma", predictedTumorOrigin!!.cancerType())
            Assert.assertEquals(0.996, predictedTumorOrigin.likelihood(), EPSILON)
            Assert.assertFalse(characteristics.isMicrosatelliteUnstable())
            Assert.assertNull(characteristics.microsatelliteEvidence())
            Assert.assertEquals(0.85, characteristics.homologousRepairScore()!!, EPSILON)
            Assert.assertTrue(characteristics.isHomologousRepairDeficient!!)
            assertEquals(
                builder()
                    .addExternalEligibleTrials("PARP trial")
                    .addOnLabelExperimentalTreatments("PARP on label")
                    .addOffLabelExperimentalTreatments("PARP off label")
                    .build(), characteristics.homologousRepairEvidence()
            )
            Assert.assertEquals(4.32, characteristics.tumorMutationalBurden()!!, EPSILON)
            Assert.assertTrue(characteristics.hasHighTumorMutationalBurden()!!)
            Assert.assertEquals(withApprovedTreatment("Pembro"), characteristics.tumorMutationalBurdenEvidence())
            Assert.assertEquals(243, (characteristics.tumorMutationalLoad() as Int).toLong())
            Assert.assertTrue(characteristics.hasHighTumorMutationalLoad()!!)
            Assert.assertNull(characteristics.tumorMutationalLoadEvidence())
        }

        private fun assertDrivers(drivers: MolecularDrivers) {
            assertVariants(drivers.variants())
            assertCopyNumbers(drivers.copyNumbers())
            assertHomozygousDisruptions(drivers.homozygousDisruptions())
            assertDisruptions(drivers.disruptions())
            assertFusions(drivers.fusions())
            assertViruses(drivers.viruses())
        }

        private fun assertVariants(variants: Set<Variant?>) {
            Assert.assertEquals(1, variants.size.toLong())
            val variant = variants.iterator().next()
            Assert.assertTrue(variant!!.isReportable)
            Assert.assertEquals("BRAF V600E", variant.event())
            Assert.assertEquals(DriverLikelihood.HIGH, variant.driverLikelihood())
            assertEquals(
                builder()
                    .addKnownResistantTreatments("Anti-BRAF known")
                    .addSuspectResistantTreatments("Anti-BRAF suspect")
                    .build(), variant.evidence()
            )
            Assert.assertEquals("BRAF", variant.gene())
            Assert.assertEquals(GeneRole.ONCO, variant.geneRole())
            Assert.assertEquals(ProteinEffect.GAIN_OF_FUNCTION, variant.proteinEffect())
            Assert.assertTrue(variant.isAssociatedWithDrugResistance!!)
            Assert.assertEquals(4.1, variant.variantCopyNumber(), EPSILON)
            Assert.assertEquals(6.0, variant.totalCopyNumber(), EPSILON)
            Assert.assertFalse(variant.isBiallelic)
            Assert.assertTrue(variant.isHotspot)
            Assert.assertEquals(1.0, variant.clonalLikelihood(), EPSILON)
            Assert.assertEquals(1, variant.phaseGroups()!!.size.toLong())
            Assert.assertTrue(variant.phaseGroups()!!.contains(2))
            val canonicalImpact = variant.canonicalImpact()
            Assert.assertEquals("ENST00000288602", canonicalImpact.transcriptId())
            Assert.assertEquals("c.1799T>A", canonicalImpact.hgvsCodingImpact())
            Assert.assertEquals("p.V600E", canonicalImpact.hgvsProteinImpact())
            Assert.assertEquals(600, (canonicalImpact.affectedCodon() as Int).toLong())
            Assert.assertNull(canonicalImpact.affectedExon())
            Assert.assertFalse(canonicalImpact.isSpliceRegion)
            Assert.assertEquals(Sets.newHashSet(VariantEffect.MISSENSE), canonicalImpact.effects())
            Assert.assertEquals(CodingEffect.MISSENSE, canonicalImpact.codingEffect())
            Assert.assertEquals(1, variant.otherImpacts().size.toLong())
            val otherImpact = variant.otherImpacts().iterator().next()!!
            Assert.assertEquals("other trans", otherImpact.transcriptId())
            Assert.assertEquals("c.other", otherImpact.hgvsCodingImpact())
            Assert.assertEquals("p.V601K", otherImpact.hgvsProteinImpact())
            Assert.assertNull(otherImpact.affectedCodon())
            Assert.assertEquals(8, (otherImpact.affectedExon() as Int).toLong())
            Assert.assertFalse(otherImpact.isSpliceRegion)
            Assert.assertEquals(Sets.newHashSet(VariantEffect.MISSENSE, VariantEffect.SPLICE_ACCEPTOR), otherImpact.effects())
            Assert.assertNull(otherImpact.codingEffect())
        }

        private fun assertCopyNumbers(copyNumbers: Set<CopyNumber?>) {
            Assert.assertEquals(2, copyNumbers.size.toLong())
            val copyNumber1 = findByEvent(copyNumbers, "MYC amp")
            Assert.assertTrue(copyNumber1!!.isReportable)
            Assert.assertEquals(DriverLikelihood.HIGH, copyNumber1.driverLikelihood())
            Assert.assertEquals(withPreClinicalTreatment("MYC pre-clinical"), copyNumber1.evidence())
            Assert.assertEquals("MYC", copyNumber1.gene())
            Assert.assertEquals(GeneRole.UNKNOWN, copyNumber1.geneRole())
            Assert.assertEquals(ProteinEffect.UNKNOWN, copyNumber1.proteinEffect())
            Assert.assertNull(copyNumber1.isAssociatedWithDrugResistance)
            Assert.assertEquals(38, copyNumber1.minCopies().toLong())
            Assert.assertEquals(39, copyNumber1.maxCopies().toLong())
            val copyNumber2 = findByEvent(copyNumbers, "PTEN del")
            Assert.assertFalse(copyNumber2!!.isReportable)
            Assert.assertNull(copyNumber2.driverLikelihood())
            Assert.assertEquals(createEmpty(), copyNumber2.evidence())
            Assert.assertEquals("PTEN", copyNumber2.gene())
            Assert.assertEquals(GeneRole.TSG, copyNumber2.geneRole())
            Assert.assertEquals(ProteinEffect.LOSS_OF_FUNCTION, copyNumber2.proteinEffect())
            Assert.assertFalse(copyNumber2.isAssociatedWithDrugResistance!!)
            Assert.assertEquals(0, copyNumber2.minCopies().toLong())
            Assert.assertEquals(2, copyNumber2.maxCopies().toLong())
        }

        private fun assertHomozygousDisruptions(homozygousDisruptions: Set<HomozygousDisruption?>) {
            Assert.assertEquals(1, homozygousDisruptions.size.toLong())
            val homozygousDisruption = homozygousDisruptions.iterator().next()
            Assert.assertTrue(homozygousDisruption!!.isReportable)
            Assert.assertEquals("PTEN hom disruption", homozygousDisruption.event())
            Assert.assertEquals(DriverLikelihood.HIGH, homozygousDisruption.driverLikelihood())
            Assert.assertEquals(createEmpty(), homozygousDisruption.evidence())
            Assert.assertEquals("PTEN", homozygousDisruption.gene())
            Assert.assertEquals(GeneRole.TSG, homozygousDisruption.geneRole())
            Assert.assertEquals(ProteinEffect.LOSS_OF_FUNCTION, homozygousDisruption.proteinEffect())
            Assert.assertFalse(homozygousDisruption.isAssociatedWithDrugResistance!!)
        }

        private fun assertDisruptions(disruptions: Set<Disruption?>) {
            Assert.assertEquals(2, disruptions.size.toLong())
            val disruption1 = findByEvent(disruptions, "NF1 disruption 1")
            Assert.assertTrue(disruption1!!.isReportable)
            Assert.assertEquals(DriverLikelihood.LOW, disruption1.driverLikelihood())
            Assert.assertEquals(createEmpty(), disruption1.evidence())
            Assert.assertEquals("NF1", disruption1.gene())
            Assert.assertEquals(GeneRole.UNKNOWN, disruption1.geneRole())
            Assert.assertEquals(ProteinEffect.UNKNOWN, disruption1.proteinEffect())
            Assert.assertNull(disruption1.isAssociatedWithDrugResistance)
            Assert.assertEquals(DisruptionType.DEL, disruption1.type())
            Assert.assertEquals(1.1, disruption1.junctionCopyNumber(), EPSILON)
            Assert.assertEquals(2.0, disruption1.undisruptedCopyNumber(), EPSILON)
            Assert.assertEquals(RegionType.INTRONIC, disruption1.regionType())
            Assert.assertEquals(CodingContext.NON_CODING, disruption1.codingContext())
            Assert.assertEquals(1, disruption1.clusterGroup().toLong())
            val disruption2 = findByEvent(disruptions, "NF1 disruption 2")
            Assert.assertFalse(disruption2!!.isReportable)
            Assert.assertEquals(DriverLikelihood.LOW, disruption2.driverLikelihood())
            Assert.assertEquals(createEmpty(), disruption2.evidence())
            Assert.assertEquals("NF1", disruption2.gene())
            Assert.assertEquals(GeneRole.UNKNOWN, disruption2.geneRole())
            Assert.assertEquals(ProteinEffect.NO_EFFECT, disruption2.proteinEffect())
            Assert.assertEquals(0.3, disruption2.junctionCopyNumber(), EPSILON)
            Assert.assertEquals(2.8, disruption2.undisruptedCopyNumber(), EPSILON)
            Assert.assertEquals(RegionType.EXONIC, disruption2.regionType())
            Assert.assertEquals(CodingContext.CODING, disruption2.codingContext())
            Assert.assertEquals(2, disruption2.clusterGroup().toLong())
        }

        private fun assertFusions(fusions: Set<Fusion?>) {
            Assert.assertEquals(1, fusions.size.toLong())
            val fusion = fusions.iterator().next()
            Assert.assertTrue(fusion!!.isReportable)
            Assert.assertEquals("EML4 - ALK fusion", fusion.event())
            Assert.assertEquals(DriverLikelihood.HIGH, fusion.driverLikelihood())
            Assert.assertEquals(createEmpty(), fusion.evidence())
            Assert.assertEquals("EML4", fusion.geneStart())
            Assert.assertEquals("ENST00000318522", fusion.geneTranscriptStart())
            Assert.assertEquals(12, fusion.fusedExonUp().toLong())
            Assert.assertEquals("ALK", fusion.geneEnd())
            Assert.assertEquals("ENST00000389048", fusion.geneTranscriptEnd())
            Assert.assertEquals(20, fusion.fusedExonDown().toLong())
            Assert.assertEquals(FusionDriverType.KNOWN_PAIR, fusion.driverType())
            Assert.assertEquals(ProteinEffect.UNKNOWN, fusion.proteinEffect())
            Assert.assertFalse(fusion.isAssociatedWithDrugResistance!!)
        }

        private fun assertViruses(viruses: Set<Virus?>) {
            Assert.assertEquals(1, viruses.size.toLong())
            val virus = viruses.iterator().next()
            Assert.assertTrue(virus!!.isReportable)
            Assert.assertEquals("HPV positive", virus.event())
            Assert.assertEquals(DriverLikelihood.HIGH, virus.driverLikelihood())
            Assert.assertEquals(createEmpty(), virus.evidence())
            Assert.assertEquals("Human papillomavirus type 16", virus.name())
            Assert.assertEquals(VirusType.HUMAN_PAPILLOMA_VIRUS, virus.type())
            Assert.assertTrue(virus.isReliable)
            Assert.assertEquals(3, virus.integrations().toLong())
        }

        private fun <T : Driver?> findByEvent(drivers: Iterable<T>, eventToFind: String): T {
            for (driver in drivers) {
                if (driver!!.event() == eventToFind) {
                    return driver
                }
            }
            throw IllegalStateException("Could not find driver with event: $eventToFind")
        }

        private fun assertImmunology(immunology: MolecularImmunology) {
            Assert.assertEquals(1, immunology.hlaAlleles().size.toLong())
            val hlaAllele = immunology.hlaAlleles().iterator().next()!!
            Assert.assertEquals("A*02:01", hlaAllele.name())
            Assert.assertEquals(1.2, hlaAllele.tumorCopyNumber(), EPSILON)
            Assert.assertFalse(hlaAllele.hasSomaticMutations())
        }

        private fun assertPharmaco(pharmaco: Set<PharmacoEntry?>) {
            Assert.assertEquals(1, pharmaco.size.toLong())
            val entry = pharmaco.iterator().next()
            Assert.assertEquals("DPYD", entry!!.gene())
            Assert.assertEquals(1, entry.haplotypes().size.toLong())
            val haplotype = entry.haplotypes().iterator().next()!!
            Assert.assertEquals("1* HOM", haplotype.name())
            Assert.assertEquals("Normal function", haplotype.function())
        }
    }
}