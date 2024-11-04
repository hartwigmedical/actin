package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.HrdType
import com.hartwig.actin.datamodel.molecular.MolecularCharacteristics
import com.hartwig.actin.report.pdf.util.Formats
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularCharacteristicFormatTest {

    @Test
    fun `Should format TMB high and low`() {
        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalBurden(
                MolecularCharacteristics(
                    tumorMutationalBurden = 61.0,
                    hasHighTumorMutationalBurden = true
                )
            )
        ).isEqualTo("TMB high (61)")
        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalBurden(
                MolecularCharacteristics(
                    tumorMutationalBurden = 61.0,
                    hasHighTumorMutationalBurden = false
                )
            )
        ).isEqualTo("TMB low (61)")
        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalBurden(
                MolecularCharacteristics(
                    hasHighTumorMutationalBurden = null,
                    tumorMutationalBurden = null
                )
            )
        ).isEqualTo("TMB ${Formats.VALUE_UNKNOWN}")
    }

    @Test
    fun `Should format TML high and low`() {
        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalLoad(
                MolecularCharacteristics(
                    tumorMutationalLoad = 10,
                    hasHighTumorMutationalLoad = true
                )
            )
        ).isEqualTo("TML high (10)")
        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalLoad(
                MolecularCharacteristics(
                    tumorMutationalLoad = 10,
                    hasHighTumorMutationalLoad = false
                )
            )
        ).isEqualTo("TML low (10)")
        assertThat(
            MolecularCharacteristicFormat.formatTumorMutationalLoad(
                MolecularCharacteristics(
                    hasHighTumorMutationalLoad = null,
                    tumorMutationalLoad = null
                )
            )
        ).isEqualTo("TML ${Formats.VALUE_UNKNOWN}")
    }

    @Test
    fun `Should format microsatellite stable and unstable`() {
        assertThat(
            MolecularCharacteristicFormat.formatMicrosatelliteStability(
                MolecularCharacteristics(
                    isMicrosatelliteUnstable = true
                )
            )
        ).isEqualTo("Unstable")
        assertThat(
            MolecularCharacteristicFormat.formatMicrosatelliteStability(
                MolecularCharacteristics(
                    isMicrosatelliteUnstable = false
                )
            )
        ).isEqualTo("Stable")
        assertThat(
            MolecularCharacteristicFormat.formatMicrosatelliteStability(
                MolecularCharacteristics(
                    isMicrosatelliteUnstable = null
                )
            )
        ).isEqualTo(Formats.VALUE_UNKNOWN)
    }

    @Test
    fun `Should format HR deficient and proficient optionally with HRD type`() {
        assertThat(
            MolecularCharacteristicFormat.formatHomologuousRepair(
                MolecularCharacteristics(
                    isHomologousRepairDeficient = true,
                    homologousRepairScore = 1.0,
                    hrdType = HrdType.BRCA1_TYPE,
                    brca1Value = 2.0
                )
            )
        ).isEqualTo("Deficient (1) - BRCA1-type (BRCA1 value: 2)")
        assertThat(
            MolecularCharacteristicFormat.formatHomologuousRepair(
                MolecularCharacteristics(
                    isHomologousRepairDeficient = false,
                    homologousRepairScore = 1.0,
                )
            )
        ).isEqualTo("Proficient (1)")
        assertThat(
            MolecularCharacteristicFormat.formatHomologuousRepair(
                MolecularCharacteristics(
                    isHomologousRepairDeficient = true,
                    homologousRepairScore = 1.0,
                    hrdType = HrdType.BRCA2_TYPE,
                    brca2Value = 2.0
                )
            )
        ).isEqualTo("Deficient (1) - BRCA2-type (BRCA2 value: 2)")
    }
}