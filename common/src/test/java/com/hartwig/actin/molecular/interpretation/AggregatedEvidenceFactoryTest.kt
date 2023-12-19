package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import org.junit.Assert
import org.junit.Test

class AggregatedEvidenceFactoryTest {
    @Test
    fun findsNoEvidenceOnMinimalRecord() {
        val evidence = AggregatedEvidenceFactory.create(TestMolecularFactory.createMinimalTestMolecularRecord())
        Assert.assertTrue(evidence.approvedTreatmentsPerEvent().isEmpty)
        Assert.assertTrue(evidence.externalEligibleTrialsPerEvent().isEmpty)
        Assert.assertTrue(evidence.onLabelExperimentalTreatmentsPerEvent().isEmpty)
        Assert.assertTrue(evidence.offLabelExperimentalTreatmentsPerEvent().isEmpty)
        Assert.assertTrue(evidence.preClinicalTreatmentsPerEvent().isEmpty)
        Assert.assertTrue(evidence.knownResistantTreatmentsPerEvent().isEmpty)
        Assert.assertTrue(evidence.suspectResistanceTreatmentsPerEvent().isEmpty)
    }

    @Test
    fun findsNoEvidenceOnNoEvidence() {
        val characteristics: MolecularCharacteristics = ImmutableMolecularCharacteristics.builder()
            .from(TestMolecularFactory.createMinimalTestMolecularRecord().characteristics())
            .isMicrosatelliteUnstable(true)
            .microsatelliteEvidence(TestActionableEvidenceFactory.createEmpty())
            .isHomologousRepairDeficient(true)
            .homologousRepairEvidence(TestActionableEvidenceFactory.createEmpty())
            .hasHighTumorMutationalBurden(true)
            .hasHighTumorMutationalBurden(null)
            .hasHighTumorMutationalLoad(true)
            .hasHighTumorMutationalLoad(null)
            .build()
        val evidence = AggregatedEvidenceFactory.create(withCharacteristics(characteristics))
        Assert.assertTrue(evidence.approvedTreatmentsPerEvent().isEmpty)
        Assert.assertTrue(evidence.externalEligibleTrialsPerEvent().isEmpty)
        Assert.assertTrue(evidence.onLabelExperimentalTreatmentsPerEvent().isEmpty)
        Assert.assertTrue(evidence.offLabelExperimentalTreatmentsPerEvent().isEmpty)
        Assert.assertTrue(evidence.preClinicalTreatmentsPerEvent().isEmpty)
        Assert.assertTrue(evidence.knownResistantTreatmentsPerEvent().isEmpty)
        Assert.assertTrue(evidence.suspectResistanceTreatmentsPerEvent().isEmpty)
    }

    @Test
    fun canAggregateCharacteristics() {
        val characteristics: MolecularCharacteristics = ImmutableMolecularCharacteristics.builder()
            .from(TestMolecularFactory.createMinimalTestMolecularRecord().characteristics())
            .isMicrosatelliteUnstable(true)
            .microsatelliteEvidence(TestActionableEvidenceFactory.createExhaustive())
            .isHomologousRepairDeficient(true)
            .homologousRepairEvidence(TestActionableEvidenceFactory.createExhaustive())
            .hasHighTumorMutationalBurden(true)
            .tumorMutationalBurdenEvidence(TestActionableEvidenceFactory.createExhaustive())
            .hasHighTumorMutationalLoad(true)
            .tumorMutationalLoadEvidence(TestActionableEvidenceFactory.createExhaustive())
            .build()
        val evidence = AggregatedEvidenceFactory.create(withCharacteristics(characteristics))
        Assert.assertEquals(4, evidence.approvedTreatmentsPerEvent().keySet().size.toLong())
        Assert.assertEquals(4, evidence.approvedTreatmentsPerEvent().values().size.toLong())
        Assert.assertEquals(4, evidence.externalEligibleTrialsPerEvent().keySet().size.toLong())
        Assert.assertEquals(4, evidence.externalEligibleTrialsPerEvent().values().size.toLong())
        Assert.assertEquals(4, evidence.onLabelExperimentalTreatmentsPerEvent().keySet().size.toLong())
        Assert.assertEquals(4, evidence.onLabelExperimentalTreatmentsPerEvent().values().size.toLong())
        Assert.assertEquals(4, evidence.offLabelExperimentalTreatmentsPerEvent().keySet().size.toLong())
        Assert.assertEquals(4, evidence.offLabelExperimentalTreatmentsPerEvent().values().size.toLong())
        Assert.assertEquals(4, evidence.preClinicalTreatmentsPerEvent().keySet().size.toLong())
        Assert.assertEquals(4, evidence.preClinicalTreatmentsPerEvent().values().size.toLong())
        Assert.assertEquals(4, evidence.knownResistantTreatmentsPerEvent().keySet().size.toLong())
        Assert.assertEquals(4, evidence.knownResistantTreatmentsPerEvent().values().size.toLong())
        Assert.assertEquals(4, evidence.suspectResistanceTreatmentsPerEvent().keySet().size.toLong())
        Assert.assertEquals(4, evidence.suspectResistanceTreatmentsPerEvent().values().size.toLong())
    }

    @Test
    fun skipEvidenceOnMissingCharacteristics() {
        val characteristics: MolecularCharacteristics = ImmutableMolecularCharacteristics.builder()
            .from(TestMolecularFactory.createMinimalTestMolecularRecord().characteristics())
            .isMicrosatelliteUnstable(null)
            .microsatelliteEvidence(TestActionableEvidenceFactory.createExhaustive())
            .isHomologousRepairDeficient(null)
            .homologousRepairEvidence(TestActionableEvidenceFactory.createExhaustive())
            .hasHighTumorMutationalBurden(null)
            .tumorMutationalBurdenEvidence(TestActionableEvidenceFactory.createExhaustive())
            .hasHighTumorMutationalLoad(null)
            .tumorMutationalLoadEvidence(TestActionableEvidenceFactory.createExhaustive())
            .build()
        val evidence = AggregatedEvidenceFactory.create(withCharacteristics(characteristics))
        Assert.assertTrue(evidence.approvedTreatmentsPerEvent().isEmpty)
        Assert.assertTrue(evidence.externalEligibleTrialsPerEvent().isEmpty)
        Assert.assertTrue(evidence.onLabelExperimentalTreatmentsPerEvent().isEmpty)
        Assert.assertTrue(evidence.offLabelExperimentalTreatmentsPerEvent().isEmpty)
        Assert.assertTrue(evidence.preClinicalTreatmentsPerEvent().isEmpty)
        Assert.assertTrue(evidence.knownResistantTreatmentsPerEvent().isEmpty)
        Assert.assertTrue(evidence.suspectResistanceTreatmentsPerEvent().isEmpty)
    }

    @Test
    fun canAggregateDrivers() {
        val drivers: MolecularDrivers = ImmutableMolecularDrivers.builder()
            .from(TestMolecularFactory.createMinimalTestMolecularRecord().drivers())
            .addVariants(
                TestVariantFactory.builder()
                    .event("variant")
                    .evidence(TestActionableEvidenceFactory.createExhaustive())
                    .build()
            )
            .addCopyNumbers(
                TestCopyNumberFactory.builder()
                    .event("amplification")
                    .evidence(TestActionableEvidenceFactory.createExhaustive())
                    .build()
            )
            .addHomozygousDisruptions(
                TestHomozygousDisruptionFactory.builder()
                    .event("hom disruption")
                    .evidence(TestActionableEvidenceFactory.createExhaustive())
                    .build()
            )
            .addDisruptions(
                TestDisruptionFactory.builder()
                    .event("disruption")
                    .evidence(TestActionableEvidenceFactory.createExhaustive())
                    .build()
            )
            .addFusions(TestFusionFactory.builder().event("fusion").evidence(TestActionableEvidenceFactory.createExhaustive()).build())
            .addViruses(TestVirusFactory.builder().event("virus").evidence(TestActionableEvidenceFactory.createExhaustive()).build())
            .build()
        val evidence = AggregatedEvidenceFactory.create(withDrivers(drivers))
        Assert.assertEquals(6, evidence.approvedTreatmentsPerEvent().keySet().size.toLong())
        Assert.assertEquals(6, evidence.approvedTreatmentsPerEvent().values().size.toLong())
        Assert.assertEquals(6, evidence.externalEligibleTrialsPerEvent().keySet().size.toLong())
        Assert.assertEquals(6, evidence.externalEligibleTrialsPerEvent().values().size.toLong())
        Assert.assertEquals(6, evidence.onLabelExperimentalTreatmentsPerEvent().keySet().size.toLong())
        Assert.assertEquals(6, evidence.onLabelExperimentalTreatmentsPerEvent().values().size.toLong())
        Assert.assertEquals(6, evidence.offLabelExperimentalTreatmentsPerEvent().keySet().size.toLong())
        Assert.assertEquals(6, evidence.offLabelExperimentalTreatmentsPerEvent().values().size.toLong())
        Assert.assertEquals(6, evidence.preClinicalTreatmentsPerEvent().keySet().size.toLong())
        Assert.assertEquals(6, evidence.preClinicalTreatmentsPerEvent().values().size.toLong())
        Assert.assertEquals(6, evidence.knownResistantTreatmentsPerEvent().keySet().size.toLong())
        Assert.assertEquals(6, evidence.knownResistantTreatmentsPerEvent().values().size.toLong())
        Assert.assertEquals(6, evidence.suspectResistanceTreatmentsPerEvent().keySet().size.toLong())
        Assert.assertEquals(6, evidence.suspectResistanceTreatmentsPerEvent().values().size.toLong())
    }

    @Test
    fun canFilterDuplicateEntries() {
        val drivers: MolecularDrivers = ImmutableMolecularDrivers.builder()
            .from(TestMolecularFactory.createMinimalTestMolecularRecord().drivers())
            .addVariants(
                TestVariantFactory.builder()
                    .driverLikelihood(DriverLikelihood.HIGH)
                    .event("variant")
                    .evidence(TestActionableEvidenceFactory.createExhaustive())
                    .build()
            )
            .addVariants(
                TestVariantFactory.builder()
                    .driverLikelihood(DriverLikelihood.MEDIUM)
                    .event("variant")
                    .evidence(TestActionableEvidenceFactory.createExhaustive())
                    .build()
            )
            .build()
        val evidence = AggregatedEvidenceFactory.create(withDrivers(drivers))
        Assert.assertEquals(1, evidence.approvedTreatmentsPerEvent().keySet().size.toLong())
        Assert.assertEquals(2, evidence.approvedTreatmentsPerEvent().values().size.toLong())
        Assert.assertEquals(1, evidence.externalEligibleTrialsPerEvent().keySet().size.toLong())
        Assert.assertEquals(2, evidence.externalEligibleTrialsPerEvent().values().size.toLong())
        Assert.assertEquals(1, evidence.onLabelExperimentalTreatmentsPerEvent().keySet().size.toLong())
        Assert.assertEquals(2, evidence.onLabelExperimentalTreatmentsPerEvent().values().size.toLong())
        Assert.assertEquals(1, evidence.offLabelExperimentalTreatmentsPerEvent().keySet().size.toLong())
        Assert.assertEquals(2, evidence.offLabelExperimentalTreatmentsPerEvent().values().size.toLong())
        Assert.assertEquals(1, evidence.preClinicalTreatmentsPerEvent().keySet().size.toLong())
        Assert.assertEquals(2, evidence.preClinicalTreatmentsPerEvent().values().size.toLong())
        Assert.assertEquals(1, evidence.knownResistantTreatmentsPerEvent().keySet().size.toLong())
        Assert.assertEquals(2, evidence.knownResistantTreatmentsPerEvent().values().size.toLong())
        Assert.assertEquals(1, evidence.suspectResistanceTreatmentsPerEvent().keySet().size.toLong())
        Assert.assertEquals(2, evidence.suspectResistanceTreatmentsPerEvent().values().size.toLong())
    }

    companion object {
        private fun withCharacteristics(characteristics: MolecularCharacteristics): MolecularRecord {
            return ImmutableMolecularRecord.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                .characteristics(characteristics)
                .build()
        }

        private fun withDrivers(drivers: MolecularDrivers): MolecularRecord {
            return ImmutableMolecularRecord.builder().from(TestMolecularFactory.createMinimalTestMolecularRecord()).drivers(drivers).build()
        }
    }
}