package com.hartwig.actin.molecular.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;
import com.hartwig.actin.molecular.datamodel.characteristics.ImmutableMolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.characteristics.MolecularCharacteristics;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory;
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class AggregatedEvidenceFactoryTest {

    @Test
    public void findsNoEvidenceOnMinimalRecord() {
        AggregatedEvidence evidence = AggregatedEvidenceFactory.create(TestMolecularFactory.createMinimalTestMolecularRecord());

        assertTrue(evidence.approvedTreatmentsPerEvent().isEmpty());
        assertTrue(evidence.externalEligibleTrialsPerEvent().isEmpty());
        assertTrue(evidence.onLabelExperimentalTreatmentsPerEvent().isEmpty());
        assertTrue(evidence.offLabelExperimentalTreatmentsPerEvent().isEmpty());
        assertTrue(evidence.preClinicalTreatmentsPerEvent().isEmpty());
        assertTrue(evidence.knownResistantTreatmentsPerEvent().isEmpty());
        assertTrue(evidence.suspectResistanceTreatmentsPerEvent().isEmpty());
    }

    @Test
    public void findsNoEvidenceOnNoEvidence() {
        MolecularCharacteristics characteristics = ImmutableMolecularCharacteristics.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord().characteristics())
                .isMicrosatelliteUnstable(true)
                .microsatelliteEvidence(TestActionableEvidenceFactory.createEmpty())
                .isHomologousRepairDeficient(true)
                .homologousRepairEvidence(TestActionableEvidenceFactory.createEmpty())
                .hasHighTumorMutationalBurden(true)
                .hasHighTumorMutationalBurden(null)
                .hasHighTumorMutationalLoad(true)
                .hasHighTumorMutationalLoad(null)
                .build();

        AggregatedEvidence evidence = AggregatedEvidenceFactory.create(withCharacteristics(characteristics));

        assertTrue(evidence.approvedTreatmentsPerEvent().isEmpty());
        assertTrue(evidence.externalEligibleTrialsPerEvent().isEmpty());
        assertTrue(evidence.onLabelExperimentalTreatmentsPerEvent().isEmpty());
        assertTrue(evidence.offLabelExperimentalTreatmentsPerEvent().isEmpty());
        assertTrue(evidence.preClinicalTreatmentsPerEvent().isEmpty());
        assertTrue(evidence.knownResistantTreatmentsPerEvent().isEmpty());
        assertTrue(evidence.suspectResistanceTreatmentsPerEvent().isEmpty());
    }

    @Test
    public void canAggregateCharacteristics() {
        MolecularCharacteristics characteristics = ImmutableMolecularCharacteristics.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord().characteristics())
                .isMicrosatelliteUnstable(true)
                .microsatelliteEvidence(TestActionableEvidenceFactory.createExhaustive())
                .isHomologousRepairDeficient(true)
                .homologousRepairEvidence(TestActionableEvidenceFactory.createExhaustive())
                .hasHighTumorMutationalBurden(true)
                .tumorMutationalBurdenEvidence(TestActionableEvidenceFactory.createExhaustive())
                .hasHighTumorMutationalLoad(true)
                .tumorMutationalLoadEvidence(TestActionableEvidenceFactory.createExhaustive())
                .build();

        AggregatedEvidence evidence = AggregatedEvidenceFactory.create(withCharacteristics(characteristics));
        assertEquals(4, evidence.approvedTreatmentsPerEvent().keySet().size());
        assertEquals(4, evidence.approvedTreatmentsPerEvent().values().size());
        assertEquals(4, evidence.externalEligibleTrialsPerEvent().keySet().size());
        assertEquals(4, evidence.externalEligibleTrialsPerEvent().values().size());
        assertEquals(4, evidence.onLabelExperimentalTreatmentsPerEvent().keySet().size());
        assertEquals(4, evidence.onLabelExperimentalTreatmentsPerEvent().values().size());
        assertEquals(4, evidence.offLabelExperimentalTreatmentsPerEvent().keySet().size());
        assertEquals(4, evidence.offLabelExperimentalTreatmentsPerEvent().values().size());
        assertEquals(4, evidence.preClinicalTreatmentsPerEvent().keySet().size());
        assertEquals(4, evidence.preClinicalTreatmentsPerEvent().values().size());
        assertEquals(4, evidence.knownResistantTreatmentsPerEvent().keySet().size());
        assertEquals(4, evidence.knownResistantTreatmentsPerEvent().values().size());
        assertEquals(4, evidence.suspectResistanceTreatmentsPerEvent().keySet().size());
        assertEquals(4, evidence.suspectResistanceTreatmentsPerEvent().values().size());
    }

    @Test
    public void skipEvidenceOnMissingCharacteristics() {
        MolecularCharacteristics characteristics = ImmutableMolecularCharacteristics.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord().characteristics())
                .isMicrosatelliteUnstable(null)
                .microsatelliteEvidence(TestActionableEvidenceFactory.createExhaustive())
                .isHomologousRepairDeficient(null)
                .homologousRepairEvidence(TestActionableEvidenceFactory.createExhaustive())
                .hasHighTumorMutationalBurden(null)
                .tumorMutationalBurdenEvidence(TestActionableEvidenceFactory.createExhaustive())
                .hasHighTumorMutationalLoad(null)
                .tumorMutationalLoadEvidence(TestActionableEvidenceFactory.createExhaustive())
                .build();

        AggregatedEvidence evidence = AggregatedEvidenceFactory.create(withCharacteristics(characteristics));
        assertTrue(evidence.approvedTreatmentsPerEvent().isEmpty());
        assertTrue(evidence.externalEligibleTrialsPerEvent().isEmpty());
        assertTrue(evidence.onLabelExperimentalTreatmentsPerEvent().isEmpty());
        assertTrue(evidence.offLabelExperimentalTreatmentsPerEvent().isEmpty());
        assertTrue(evidence.preClinicalTreatmentsPerEvent().isEmpty());
        assertTrue(evidence.knownResistantTreatmentsPerEvent().isEmpty());
        assertTrue(evidence.suspectResistanceTreatmentsPerEvent().isEmpty());
    }

    @Test
    public void canAggregateDrivers() {
        MolecularDrivers drivers = ImmutableMolecularDrivers.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord().drivers())
                .addVariants(TestVariantFactory.builder()
                        .event("variant")
                        .evidence(TestActionableEvidenceFactory.createExhaustive())
                        .build())
                .addCopyNumbers(TestCopyNumberFactory.builder()
                        .event("amplification")
                        .evidence(TestActionableEvidenceFactory.createExhaustive())
                        .build())
                .addHomozygousDisruptions(TestHomozygousDisruptionFactory.builder()
                        .event("hom disruption")
                        .evidence(TestActionableEvidenceFactory.createExhaustive())
                        .build())
                .addDisruptions(TestDisruptionFactory.builder()
                        .event("disruption")
                        .evidence(TestActionableEvidenceFactory.createExhaustive())
                        .build())
                .addFusions(TestFusionFactory.builder().event("fusion").evidence(TestActionableEvidenceFactory.createExhaustive()).build())
                .addViruses(TestVirusFactory.builder().event("virus").evidence(TestActionableEvidenceFactory.createExhaustive()).build())
                .build();

        AggregatedEvidence evidence = AggregatedEvidenceFactory.create(withDrivers(drivers));
        assertEquals(6, evidence.approvedTreatmentsPerEvent().keySet().size());
        assertEquals(6, evidence.approvedTreatmentsPerEvent().values().size());
        assertEquals(6, evidence.externalEligibleTrialsPerEvent().keySet().size());
        assertEquals(6, evidence.externalEligibleTrialsPerEvent().values().size());
        assertEquals(6, evidence.onLabelExperimentalTreatmentsPerEvent().keySet().size());
        assertEquals(6, evidence.onLabelExperimentalTreatmentsPerEvent().values().size());
        assertEquals(6, evidence.offLabelExperimentalTreatmentsPerEvent().keySet().size());
        assertEquals(6, evidence.offLabelExperimentalTreatmentsPerEvent().values().size());
        assertEquals(6, evidence.preClinicalTreatmentsPerEvent().keySet().size());
        assertEquals(6, evidence.preClinicalTreatmentsPerEvent().values().size());
        assertEquals(6, evidence.knownResistantTreatmentsPerEvent().keySet().size());
        assertEquals(6, evidence.knownResistantTreatmentsPerEvent().values().size());
        assertEquals(6, evidence.suspectResistanceTreatmentsPerEvent().keySet().size());
        assertEquals(6, evidence.suspectResistanceTreatmentsPerEvent().values().size());
    }

    @Test
    public void canFilterDuplicateEntries() {
        MolecularDrivers drivers = ImmutableMolecularDrivers.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord().drivers())
                .addVariants(TestVariantFactory.builder()
                        .driverLikelihood(DriverLikelihood.HIGH)
                        .event("variant")
                        .evidence(TestActionableEvidenceFactory.createExhaustive())
                        .build())
                .addVariants(TestVariantFactory.builder()
                        .driverLikelihood(DriverLikelihood.MEDIUM)
                        .event("variant")
                        .evidence(TestActionableEvidenceFactory.createExhaustive())
                        .build())
                .build();

        AggregatedEvidence evidence = AggregatedEvidenceFactory.create(withDrivers(drivers));
        assertEquals(1, evidence.approvedTreatmentsPerEvent().keySet().size());
        assertEquals(2, evidence.approvedTreatmentsPerEvent().values().size());
        assertEquals(1, evidence.externalEligibleTrialsPerEvent().keySet().size());
        assertEquals(2, evidence.externalEligibleTrialsPerEvent().values().size());
        assertEquals(1, evidence.onLabelExperimentalTreatmentsPerEvent().keySet().size());
        assertEquals(2, evidence.onLabelExperimentalTreatmentsPerEvent().values().size());
        assertEquals(1, evidence.offLabelExperimentalTreatmentsPerEvent().keySet().size());
        assertEquals(2, evidence.offLabelExperimentalTreatmentsPerEvent().values().size());
        assertEquals(1, evidence.preClinicalTreatmentsPerEvent().keySet().size());
        assertEquals(2, evidence.preClinicalTreatmentsPerEvent().values().size());
        assertEquals(1, evidence.knownResistantTreatmentsPerEvent().keySet().size());
        assertEquals(2, evidence.knownResistantTreatmentsPerEvent().values().size());
        assertEquals(1, evidence.suspectResistanceTreatmentsPerEvent().keySet().size());
        assertEquals(2, evidence.suspectResistanceTreatmentsPerEvent().values().size());
    }

    @NotNull
    private static MolecularRecord withCharacteristics(@NotNull MolecularCharacteristics characteristics) {
        return ImmutableMolecularRecord.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord())
                .characteristics(characteristics)
                .build();
    }

    @NotNull
    private static MolecularRecord withDrivers(@NotNull MolecularDrivers drivers) {
        return ImmutableMolecularRecord.builder().from(TestMolecularFactory.createMinimalTestMolecularRecord()).drivers(drivers).build();
    }
}