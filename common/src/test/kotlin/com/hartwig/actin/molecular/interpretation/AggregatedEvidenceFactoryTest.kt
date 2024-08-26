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
import com.hartwig.actin.molecular.datamodel.evidence.TestClinicalEvidenceFactory
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
            microsatelliteEvidence = TestClinicalEvidenceFactory.createEmpty(),
            isHomologousRepairDeficient = true,
            homologousRepairEvidence = TestClinicalEvidenceFactory.createEmpty(),
            hasHighTumorMutationalBurden = null,
            hasHighTumorMutationalLoad = null
        )
        assertThat(AggregatedEvidenceFactory.create(withCharacteristics(characteristics)).treatmentEvidence).isEmpty()
    }

    @Test
    fun `Should aggregate characteristics`() {
        val characteristics = TestMolecularFactory.createMinimalTestMolecularRecord().characteristics.copy(
            isMicrosatelliteUnstable = true,
            microsatelliteEvidence = TestClinicalEvidenceFactory.createExhaustive(),
            isHomologousRepairDeficient = true,
            homologousRepairEvidence = TestClinicalEvidenceFactory.createExhaustive(),
            hasHighTumorMutationalBurden = true,
            tumorMutationalBurdenEvidence = TestClinicalEvidenceFactory.createExhaustive(),
            hasHighTumorMutationalLoad = true,
            tumorMutationalLoadEvidence = TestClinicalEvidenceFactory.createExhaustive()
        )
        val evidence = AggregatedEvidenceFactory.create(withCharacteristics(characteristics))

        assertThat(evidence.treatmentEvidence).hasSize(4)
    }

    @Test
    fun `Should skip evidence on missing characteristics`() {
        val characteristics = TestMolecularFactory.createMinimalTestMolecularRecord().characteristics.copy(
            isMicrosatelliteUnstable = null,
            microsatelliteEvidence = TestClinicalEvidenceFactory.createExhaustive(),
            isHomologousRepairDeficient = null,
            homologousRepairEvidence = TestClinicalEvidenceFactory.createExhaustive(),
            hasHighTumorMutationalBurden = null,
            tumorMutationalBurdenEvidence = TestClinicalEvidenceFactory.createExhaustive(),
            hasHighTumorMutationalLoad = null,
            tumorMutationalLoadEvidence = TestClinicalEvidenceFactory.createExhaustive()
        )
        assertThat(AggregatedEvidenceFactory.create(withCharacteristics(characteristics)).treatmentEvidence).isEmpty()
    }

    @Test
    fun `Should aggregate drivers`() {
        val drivers = TestMolecularFactory.createMinimalTestMolecularRecord().drivers.copy(
            variants = setOf(
                TestVariantFactory.createMinimal().copy(
                    event = "variant", evidence = TestClinicalEvidenceFactory.createExhaustive()
                )
            ),
            copyNumbers = setOf(
                TestCopyNumberFactory.createMinimal().copy(
                    event = "amplification", evidence = TestClinicalEvidenceFactory.createExhaustive()
                )
            ),
            homozygousDisruptions = setOf(
                TestHomozygousDisruptionFactory.createMinimal().copy(
                    event = "hom disruption", evidence = TestClinicalEvidenceFactory.createExhaustive()
                )
            ),
            disruptions = setOf(
                TestDisruptionFactory.createMinimal().copy(
                    event = "disruption", evidence = TestClinicalEvidenceFactory.createExhaustive()
                )
            ),
            fusions = setOf(
                TestFusionFactory.createMinimal().copy(
                    event = "fusion", evidence = TestClinicalEvidenceFactory.createExhaustive()
                )
            ),
            viruses = setOf(
                TestVirusFactory.createMinimal().copy(
                    event = "virus", evidence = TestClinicalEvidenceFactory.createExhaustive()
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
            evidence = TestClinicalEvidenceFactory.createExhaustive(),
        )
        val drivers = TestMolecularFactory.createMinimalTestMolecularRecord().drivers.copy(
            variants = setOf(variant, variant.copy(driverLikelihood = DriverLikelihood.MEDIUM))
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