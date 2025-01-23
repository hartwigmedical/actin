package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.HrdType
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

const val WIDTH = 1f

class MolecularCharacteristicsGeneratorTest {

    @Test
    fun `Should not display hrd type or brca value when sample is HR proficient`() {
        val test = withHomologousRepairStatusAndType(false, hrScore = 0.0, HrdType.NONE, brca1Value = 0.0, brca2Value = 0.0)
        assertThat(MolecularCharacteristicsGenerator(test, WIDTH).createHRStatusString()).isEqualTo("Proficient (0)")
    }

    @Test
    fun `Should display hrd type when type cannot be determined`() {
        val cannotBeDetermined =
            withHomologousRepairStatusAndType(true, hrScore = 0.8123, HrdType.CANNOT_BE_DETERMINED, brca1Value = 0.75, brca2Value = 0.06123)
        assertThat(MolecularCharacteristicsGenerator(cannotBeDetermined, WIDTH).createHRStatusString())
            .isEqualTo("Deficient (0.81)")
    }

    @Test
    fun `Should display hrd type and brca value when sample is HR deficient`() {
        val brca1Type =
            withHomologousRepairStatusAndType(true, hrScore = 0.8123, HrdType.BRCA1_TYPE, brca1Value = 0.75, brca2Value = 0.06123)
        val brca2Type =
            withHomologousRepairStatusAndType(true, hrScore = 0.8123, HrdType.BRCA2_TYPE, brca1Value = 0.03123, brca2Value = 0.78)
        assertThat(MolecularCharacteristicsGenerator(brca1Type, WIDTH).createHRStatusString())
            .isEqualTo("Deficient (0.81) - BRCA1-type (BRCA1 value: 0.75)")
        assertThat(MolecularCharacteristicsGenerator(brca2Type, WIDTH).createHRStatusString())
            .isEqualTo("Deficient (0.81) - BRCA2-type (BRCA2 value: 0.78)")
    }

    private fun withHomologousRepairStatusAndType(
        isHrd: Boolean,
        hrScore: Double = 0.0,
        hrdType: HrdType,
        brca1Value: Double = 0.0,
        brca2Value: Double = 0.0
    ): MolecularTest {
        val base = TestMolecularFactory.createMinimalTestOrangeRecord()
        return base.copy(
            characteristics = base.characteristics.copy(
                isHomologousRepairDeficient = isHrd,
                homologousRepairScore = hrScore,
                hrdType = hrdType,
                brca1Value = brca1Value,
                brca2Value = brca2Value
            )
        )
    }
}