package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.molecular.datamodel.driver.TestCopyNumberFactory
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AggregatedEvidenceFactoryTest {

    private val evidenceFields = listOf(
        AggregatedEvidence::approvedTreatmentsPerEvent,
        AggregatedEvidence::externalEligibleTrialsPerEvent,
        AggregatedEvidence::onLabelExperimentalTreatmentsPerEvent,
        AggregatedEvidence::offLabelExperimentalTreatmentsPerEvent,
        AggregatedEvidence::preClinicalTreatmentsPerEvent,
        AggregatedEvidence::knownResistantTreatmentsPerEvent,
        AggregatedEvidence::suspectResistantTreatmentsPerEvent
    )
    
    @Test
    fun `Should find no evidence on minimal record`() {
        assertEvidenceIsEmpty(AggregatedEvidenceFactory.create(TestMolecularFactory.createMinimalTestMolecularRecord()))
    }

    @Test
    fun `Should find no evidence on no evidence`() {
        val characteristics = TestMolecularFactory.createMinimalTestMolecularRecord().characteristics.copy(
            isMicrosatelliteUnstable = true,
            microsatelliteEvidence = TestActionableEvidenceFactory.createEmpty(),
            isHomologousRepairDeficient = true,
            homologousRepairEvidence = TestActionableEvidenceFactory.createEmpty(),
            hasHighTumorMutationalBurden = null,
            hasHighTumorMutationalLoad = null
        )
        assertEvidenceIsEmpty(AggregatedEvidenceFactory.create(withCharacteristics(characteristics)))
    }

    @Test
    fun `Should aggregate characteristics`() {
        val characteristics = TestMolecularFactory.createMinimalTestMolecularRecord().characteristics.copy(
            isMicrosatelliteUnstable = true,
            microsatelliteEvidence = TestActionableEvidenceFactory.createExhaustive(),
            isHomologousRepairDeficient = true,
            homologousRepairEvidence = TestActionableEvidenceFactory.createExhaustive(),
            hasHighTumorMutationalBurden = true,
            tumorMutationalBurdenEvidence = TestActionableEvidenceFactory.createExhaustive(),
            hasHighTumorMutationalLoad = true,
            tumorMutationalLoadEvidence = TestActionableEvidenceFactory.createExhaustive()
        )
        val evidence = AggregatedEvidenceFactory.create(withCharacteristics(characteristics))

        assertEvidenceCountForAllFields(evidence, 4)
    }

    @Test
    fun `Should skip evidence on missing characteristics`() {
        val characteristics = TestMolecularFactory.createMinimalTestMolecularRecord().characteristics.copy(
            isMicrosatelliteUnstable = null,
            microsatelliteEvidence = TestActionableEvidenceFactory.createExhaustive(),
            isHomologousRepairDeficient = null,
            homologousRepairEvidence = TestActionableEvidenceFactory.createExhaustive(),
            hasHighTumorMutationalBurden = null,
            tumorMutationalBurdenEvidence = TestActionableEvidenceFactory.createExhaustive(),
            hasHighTumorMutationalLoad = null,
            tumorMutationalLoadEvidence = TestActionableEvidenceFactory.createExhaustive()
        )
        assertEvidenceIsEmpty(AggregatedEvidenceFactory.create(withCharacteristics(characteristics)))
    }

    @Test
    fun `Should aggregate drivers`() {
        val drivers = TestMolecularFactory.createMinimalTestMolecularRecord().drivers.copy(
            variants = setOf(
                TestVariantFactory.createMinimal().copy(
                    event = "variant", evidence = TestActionableEvidenceFactory.createExhaustive()
                )
            ),
            copyNumbers = setOf(
                TestCopyNumberFactory.createMinimal().copy(
                    event = "amplification", evidence = TestActionableEvidenceFactory.createExhaustive()
                )
            ),
            homozygousDisruptions = setOf(
                TestHomozygousDisruptionFactory.createMinimal().copy(
                    event = "hom disruption", evidence = TestActionableEvidenceFactory.createExhaustive()
                )
            ),
            disruptions = setOf(
                TestDisruptionFactory.createMinimal().copy(
                    event = "disruption", evidence = TestActionableEvidenceFactory.createExhaustive()
                )
            ),
            fusions = setOf(
                TestFusionFactory.createMinimal().copy(
                    event = "fusion", evidence = TestActionableEvidenceFactory.createExhaustive()
                )
            ),
            viruses = setOf(
                TestVirusFactory.createMinimal().copy(
                    event = "virus", evidence = TestActionableEvidenceFactory.createExhaustive()
                )
            ),
        )
        val evidence = AggregatedEvidenceFactory.create(withDrivers(drivers))
        assertEvidenceCountForAllFields(evidence, 6)
    }

    @Test
    fun `Should filter duplicate entries`() {
        val variant = TestVariantFactory.createMinimal().copy(
            driverLikelihood = DriverLikelihood.HIGH,
            event = "variant",
            evidence = TestActionableEvidenceFactory.createExhaustive(),
        )
        val drivers = TestMolecularFactory.createMinimalTestMolecularRecord().drivers.copy(
            variants = setOf(variant, variant.copy(driverLikelihood = DriverLikelihood.MEDIUM))
        )
        val evidence = AggregatedEvidenceFactory.create(withDrivers(drivers))
        assertEvidenceCountForAllFields(evidence, 1, 1)
    }

    private fun withCharacteristics(characteristics: MolecularCharacteristics): MolecularRecord {
        return TestMolecularFactory.createMinimalTestMolecularRecord().copy(characteristics = characteristics)
    }

    private fun withDrivers(drivers: Drivers): MolecularRecord {
        return TestMolecularFactory.createMinimalTestMolecularRecord().copy(drivers = drivers)
    }

    private fun assertEvidenceIsEmpty(evidence: AggregatedEvidence) {
        evidenceFields.forEach {
            assertThat(it.invoke(evidence)).isEmpty()
        }
    }

    private fun assertEvidenceCountForAllFields(
        evidence: AggregatedEvidence, expectedEvidenceKeyCount: Int, expectedEvidenceValueCount: Int? = null
    ) {
        evidenceFields.forEach {
            val evidenceMap = it.invoke(evidence)
            assertThat(evidenceMap.keys).hasSize(expectedEvidenceKeyCount)
            assertThat(evidenceMap.values.flatten()).hasSize(expectedEvidenceValueCount ?: expectedEvidenceKeyCount)
        }
    }
}