package com.hartwig.actin.molecular.interpretation

import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombination
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombinationType
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Drivers
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
        val minimalRecord = TestMolecularFactory.createMinimalTestMolecularRecord()
        assertThat(AggregatedEvidenceFactory.create(minimalRecord).treatmentEvidencePerEvent).isEmpty()
    }

    @Test
    fun `Should find no evidence on no evidence`() {
        val characteristics = TestMolecularFactory.createMinimalTestMolecularRecord().characteristics.copy(
            microsatelliteStability = MicrosatelliteStability(
                microsatelliteIndelsPerMb = 10.2,
                isUnstable = true,
                evidence = TestClinicalEvidenceFactory.createEmpty()
            ),
            homologousRecombination = HomologousRecombination(
                score = 1.0,
                isDeficient = true,
                type = HomologousRecombinationType.BRCA1_TYPE,
                brca1Value = 1.0,
                brca2Value = 0.0,
                evidence = TestClinicalEvidenceFactory.createEmpty()
            )
        )
        assertThat(AggregatedEvidenceFactory.create(withCharacteristics(characteristics)).treatmentEvidencePerEvent).isEmpty()
    }

    @Test
    fun `Should find no external eligible trials when hasSufficientQuality is false`() {
        assertThat(
            AggregatedEvidenceFactory.create(
                TestMolecularFactory.createMinimalTestMolecularRecord().copy(hasSufficientQuality = false)
            ).eligibleTrialsPerEvent
        ).isEmpty()
    }

    @Test
    fun `Should aggregate characteristics`() {
        val characteristics = TestMolecularFactory.createExhaustiveTestMolecularRecord().characteristics
        val evidence = AggregatedEvidenceFactory.create(withCharacteristics(characteristics))

        assertThat(evidence.treatmentEvidencePerEvent).hasSize(2)
    }

    @Test
    fun `Should aggregate drivers`() {
        val drivers = TestMolecularFactory.createMinimalTestMolecularRecord().drivers.copy(
            variants = listOf(
                TestVariantFactory.createMinimal().copy(
                    event = "variant", evidence = TestClinicalEvidenceFactory.createExhaustive()
                )
            ),
            copyNumbers = listOf(
                TestCopyNumberFactory.createMinimal().copy(
                    event = "amplification", evidence = TestClinicalEvidenceFactory.createExhaustive()
                )
            ),
            homozygousDisruptions = listOf(
                TestHomozygousDisruptionFactory.createMinimal().copy(
                    event = "hom disruption", evidence = TestClinicalEvidenceFactory.createExhaustive()
                )
            ),
            disruptions = listOf(
                TestDisruptionFactory.createMinimal().copy(
                    event = "disruption", evidence = TestClinicalEvidenceFactory.createExhaustive()
                )
            ),
            fusions = listOf(
                TestFusionFactory.createMinimal().copy(
                    event = "fusion", evidence = TestClinicalEvidenceFactory.createExhaustive()
                )
            ),
            viruses = listOf(
                TestVirusFactory.createMinimal().copy(
                    event = "virus", evidence = TestClinicalEvidenceFactory.createExhaustive()
                )
            ),
        )
        val evidence = AggregatedEvidenceFactory.create(withDrivers(drivers))
        assertThat(evidence.treatmentEvidencePerEvent).hasSize(6)
    }

    @Test
    fun `Should filter duplicate entries`() {
        val variant = TestVariantFactory.createMinimal().copy(
            driverLikelihood = DriverLikelihood.HIGH,
            event = "variant",
            evidence = TestClinicalEvidenceFactory.createExhaustive(),
        )
        val drivers = TestMolecularFactory.createMinimalTestMolecularRecord().drivers.copy(
            variants = listOf(variant, variant.copy(driverLikelihood = DriverLikelihood.MEDIUM))
        )
        val evidence = AggregatedEvidenceFactory.create(withDrivers(drivers))
        assertThat(evidence.treatmentEvidencePerEvent).hasSize(1)
    }

    private fun withCharacteristics(characteristics: MolecularCharacteristics): MolecularRecord {
        return TestMolecularFactory.createMinimalTestMolecularRecord().copy(characteristics = characteristics)
    }

    private fun withDrivers(drivers: Drivers): MolecularRecord {
        return TestMolecularFactory.createMinimalTestMolecularRecord().copy(drivers = drivers)
    }
}