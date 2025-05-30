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
            MolecularCharacteristicFormat.formatTumorMutationalBurden(
                withTumorMutationalBurden(score = 200.5, isHigh = true),
                displayValue = true
            )
        ).isEqualTo("TMB 200.5 mut/Mb")

        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalBurden(
                withTumorMutationalBurden(score = 200.5, isHigh = true),
                displayValue = false
            )
        ).isEqualTo("TMB High")

        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalBurden(
                withTumorMutationalBurden(score = 2.5, isHigh = false),
                displayValue = true
            )
        ).isEqualTo("TMB 2.5 mut/Mb")

        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalBurden(
                withTumorMutationalBurden(score = 2.5, isHigh = false),
                displayValue = false
            )
        ).isEqualTo("TMB Low")

        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalBurden(
                TestMolecularFactory.createMinimalTestCharacteristics(),
                displayValue = true
            )
        ).isEqualTo("TMB ${Formats.VALUE_UNKNOWN}")
    }

    @Test
    fun `Should format TML high and low`() {
        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalLoad(
                withTumorMutationalLoad(score = 200, isHigh = true),
                displayValue = true
            )
        ).isEqualTo("TML 200")

        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalLoad(
                withTumorMutationalLoad(score = 200, isHigh = true),
                displayValue = false
            )
        ).isEqualTo("TML High")

        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalLoad(
                withTumorMutationalLoad(score = 2, isHigh = false),
                displayValue = true
            )
        ).isEqualTo("TML 2")

        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalLoad(
                withTumorMutationalLoad(score = 2, isHigh = false),
                displayValue = false
            )
        ).isEqualTo("TML Low")

        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalLoad(
                TestMolecularFactory.createMinimalTestCharacteristics(),
                displayValue = true
            )
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
                    isDeficient = true,
                    score = 1.0,
                    type = HomologousRecombinationType.BRCA1_TYPE,
                    brca1Value = 0.8
                )
            )
        ).isEqualTo("Deficient (1) - BRCA1-type (BRCA1 value: 0.8)")

        assertThat(
            MolecularCharacteristicFormat.formatHomologousRecombination(
                withHomologousRecombination(isDeficient = false, score = 0.2)
            )
        ).isEqualTo("Proficient (0.2)")

        assertThat(
            MolecularCharacteristicFormat.formatHomologousRecombination(
                withHomologousRecombination(
                    isDeficient = true,
                    score = 1.0,
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
        isDeficient: Boolean,
        score: Double,
        type: HomologousRecombinationType = HomologousRecombinationType.CANNOT_BE_DETERMINED,
        brca1Value: Double = 0.0,
        brca2Value: Double = 0.0
    ): MolecularCharacteristics {
        return TestMolecularFactory.createMinimalTestCharacteristics().copy(
            homologousRecombination = HomologousRecombination(
                isDeficient = isDeficient,
                score = score,
                type = type,
                brca1Value = brca1Value,
                brca2Value = brca2Value,
                evidence = TestClinicalEvidenceFactory.createEmpty()
            )
        )
    }
}