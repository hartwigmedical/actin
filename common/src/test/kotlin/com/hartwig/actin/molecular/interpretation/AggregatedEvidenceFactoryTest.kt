package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.Drivers
import com.hartwig.actin.datamodel.molecular.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.TestCopyNumberFactory
import com.hartwig.actin.datamodel.molecular.driver.TestDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestFusionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVariantFactory
import com.hartwig.actin.datamodel.molecular.driver.TestVirusFactory
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AggregatedEvidenceFactoryTest {

    @Test
    fun `Should find no evidence on minimal record`() {
        assertThat(AggregatedEvidenceFactory.create(TestMolecularFactory.createMinimalTestMolecularRecord()).treatmentEvidence).isEmpty()
    }

    @Test
    fun `Should find no evidence on no evidence`() {
        val characteristics = TestMolecularFactory.createMinimalTestMolecularRecord().characteristics.copy(
            isMicrosatelliteUnstable = true,
            microsatelliteEvidence = TestClinicalEvidenceFactory.createEmptyClinicalEvidence(),
            isHomologousRepairDeficient = true,
            homologousRepairEvidence = TestClinicalEvidenceFactory.createEmptyClinicalEvidence(),
            hasHighTumorMutationalBurden = null,
            hasHighTumorMutationalLoad = null
        )
        assertThat(AggregatedEvidenceFactory.create(withCharacteristics(characteristics)).treatmentEvidence).isEmpty()
    }

    @Test
    fun `Should find no external eligible trials when hasSufficientQuality is false`() {
        assertThat(
            AggregatedEvidenceFactory.create(
                TestMolecularFactory.createMinimalTestMolecularRecord().copy(hasSufficientQuality = false)
            ).externalEligibleTrialsPerEvent
        ).isEmpty()
    }

    @Test
    fun `Should aggregate characteristics`() {
        val characteristics = TestMolecularFactory.createMinimalTestMolecularRecord().characteristics.copy(
            isMicrosatelliteUnstable = true,
            microsatelliteEvidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence(),
            isHomologousRepairDeficient = true,
            homologousRepairEvidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence(),
            hasHighTumorMutationalBurden = true,
            tumorMutationalBurdenEvidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence(),
            hasHighTumorMutationalLoad = true,
            tumorMutationalLoadEvidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence()
        )
        val evidence = AggregatedEvidenceFactory.create(withCharacteristics(characteristics))

        assertThat(evidence.treatmentEvidence).hasSize(4)
    }

    @Test
    fun `Should skip evidence on missing characteristics`() {
        val characteristics = TestMolecularFactory.createMinimalTestMolecularRecord().characteristics.copy(
            isMicrosatelliteUnstable = null,
            microsatelliteEvidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence(),
            isHomologousRepairDeficient = null,
            homologousRepairEvidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence(),
            hasHighTumorMutationalBurden = null,
            tumorMutationalBurdenEvidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence(),
            hasHighTumorMutationalLoad = null,
            tumorMutationalLoadEvidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence()
        )
        assertThat(AggregatedEvidenceFactory.create(withCharacteristics(characteristics)).treatmentEvidence).isEmpty()
    }

    @Test
    fun `Should aggregate drivers`() {
        val drivers = TestMolecularFactory.createMinimalTestMolecularRecord().drivers.copy(
            variants = listOf(
                TestVariantFactory.createMinimal().copy(
                    event = "variant", evidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence()
                )
            ),
            copyNumbers = listOf(
                TestCopyNumberFactory.createMinimal().copy(
                    event = "amplification", evidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence()
                )
            ),
            homozygousDisruptions = listOf(
                TestHomozygousDisruptionFactory.createMinimal().copy(
                    event = "hom disruption", evidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence()
                )
            ),
            disruptions = listOf(
                TestDisruptionFactory.createMinimal().copy(
                    event = "disruption", evidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence()
                )
            ),
            fusions = listOf(
                TestFusionFactory.createMinimal().copy(
                    event = "fusion", evidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence()
                )
            ),
            viruses = listOf(
                TestVirusFactory.createMinimal().copy(
                    event = "virus", evidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence()
                )
            ),
        )
        val evidence = AggregatedEvidenceFactory.create(withDrivers(drivers))
        assertThat(evidence.treatmentEvidence).hasSize(6)
    }

    @Test
    fun `Should filter duplicate entries`() {
        val variant = TestVariantFactory.createMinimal().copy(
            driverLikelihood = DriverLikelihood.HIGH,
            event = "variant",
            evidence = TestClinicalEvidenceFactory.createExhaustiveClinicalEvidence(),
        )
        val drivers = TestMolecularFactory.createMinimalTestMolecularRecord().drivers.copy(
            variants = listOf(variant, variant.copy(driverLikelihood = DriverLikelihood.MEDIUM))
        )
        val evidence = AggregatedEvidenceFactory.create(withDrivers(drivers))
        assertThat(evidence.treatmentEvidence).hasSize(1)
    }

    private fun withCharacteristics(characteristics: MolecularCharacteristics): MolecularRecord {
        return TestMolecularFactory.createMinimalTestMolecularRecord().copy(characteristics = characteristics)
    }

    private fun withDrivers(drivers: Drivers): MolecularRecord {
        return TestMolecularFactory.createMinimalTestMolecularRecord().copy(drivers = drivers)
    }
}