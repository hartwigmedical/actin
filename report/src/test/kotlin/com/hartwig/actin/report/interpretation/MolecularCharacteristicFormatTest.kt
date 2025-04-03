package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombination
import com.hartwig.actin.datamodel.molecular.characteristics.HomologousRecombinationType
import com.hartwig.actin.datamodel.molecular.characteristics.MicrosatelliteStability
import com.hartwig.actin.datamodel.molecular.characteristics.MolecularCharacteristics
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalLoad
import com.hartwig.actin.datamodel.molecular.evidence.TestClinicalEvidenceFactory
import com.hartwig.actin.report.pdf.util.Formats
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularCharacteristicFormatTest {

    @Test
    fun `Should format TMB high and low`() {
        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalBurden(withTumorMutationalBurden(score = 61.0, isHigh = true))
        ).isEqualTo("TMB High (61)")

        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalBurden(withTumorMutationalBurden(score = 6.0, isHigh = false))
        ).isEqualTo("TMB Low (6)")

        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalBurden(TestMolecularFactory.createMinimalTestCharacteristics())
        ).isEqualTo("TMB ${Formats.VALUE_UNKNOWN}")
    }

    @Test
    fun `Should format TML high and low`() {
        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalLoad(withTumorMutationalLoad(score = 10, isHigh = true))
        ).isEqualTo("TML High (10)")

        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalLoad(withTumorMutationalLoad(score = 10, isHigh = false))
        ).isEqualTo("TML Low (10)")

        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalLoad(TestMolecularFactory.createMinimalTestCharacteristics())
        ).isEqualTo("TML ${Formats.VALUE_UNKNOWN}")
    }

    @Test
    fun `Should format microsatellite stable and unstable`() {
        assertThat(
            MolecularCharacteristicFormat.formatMicrosatelliteStability(withMicrosatelliteUnstable(isUnstable = true))
        ).isEqualTo("Unstable")

        assertThat(
            MolecularCharacteristicFormat.formatMicrosatelliteStability(withMicrosatelliteUnstable(isUnstable = false))
        ).isEqualTo("Stable")

        assertThat(
            MolecularCharacteristicFormat.formatMicrosatelliteStability(TestMolecularFactory.createMinimalTestCharacteristics())
        ).isEqualTo(Formats.VALUE_UNKNOWN)
    }

    @Test
    fun `Should format HR deficient and proficient optionally with HRD type`() {
        assertThat(
            MolecularCharacteristicFormat.formatHomologousRecombination(
                withHomologousRecombination(
                    score = 1.0,
                    isDeficient = true,
                    type = HomologousRecombinationType.BRCA1_TYPE,
                    brca1Value = 0.8
                )
            )
        ).isEqualTo("Deficient (1) - BRCA1-type (BRCA1 value: 0.8)")

        assertThat(
            MolecularCharacteristicFormat.formatHomologousRecombination(
                withHomologousRecombination(score = 0.2, isDeficient = false)
            )
        ).isEqualTo("Proficient (0.2)")

        assertThat(
            MolecularCharacteristicFormat.formatHomologousRecombination(
                withHomologousRecombination(
                    score = 1.0,
                    isDeficient = true,
                    type = HomologousRecombinationType.BRCA2_TYPE,
                    brca2Value = 0.9
                )
            )
        ).isEqualTo("Deficient (1) - BRCA2-type (BRCA2 value: 0.9)")
    }

    private fun withTumorMutationalBurden(score: Double, isHigh: Boolean): MolecularCharacteristics {
        return TestMolecularFactory.createMinimalTestCharacteristics().copy(
            tumorMutationalBurden = TumorMutationalBurden(
                score = score,
                isHigh = isHigh,
                evidence = TestClinicalEvidenceFactory.createEmpty()
            )
        )
    }

    private fun withTumorMutationalLoad(score: Int, isHigh: Boolean): MolecularCharacteristics {
        return TestMolecularFactory.createMinimalTestCharacteristics().copy(
            tumorMutationalLoad = TumorMutationalLoad(
                score = score,
                isHigh = isHigh,
                evidence = TestClinicalEvidenceFactory.createEmpty()
            )
        )
    }

    private fun withMicrosatelliteUnstable(isUnstable: Boolean): MolecularCharacteristics {
        return TestMolecularFactory.createMinimalTestCharacteristics().copy(
            microsatelliteStability = MicrosatelliteStability(
                microsatelliteIndelsPerMb = null,
                isUnstable = isUnstable,
                evidence = TestClinicalEvidenceFactory.createEmpty()
            )
        )
    }

    private fun withHomologousRecombination(
        score: Double,
        isDeficient: Boolean?,
        type: HomologousRecombinationType = HomologousRecombinationType.CANNOT_BE_DETERMINED,
        brca1Value: Double = 0.0,
        brca2Value: Double = 0.0
    ): MolecularCharacteristics {
        return TestMolecularFactory.createMinimalTestCharacteristics().copy(
            homologousRecombination = HomologousRecombination(
                score = score,
                isDeficient = isDeficient,
                type = type,
                brca1Value = brca1Value,
                brca2Value = brca2Value,
                evidence = TestClinicalEvidenceFactory.createEmpty()
            )
        )
    }
}