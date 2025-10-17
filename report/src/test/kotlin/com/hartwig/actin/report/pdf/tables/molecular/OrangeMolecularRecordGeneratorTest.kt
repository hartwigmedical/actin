package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.panel.PanelTargetSpecification
import com.hartwig.actin.report.pdf.getCellContents
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OrangeMolecularRecordGeneratorTest {

    @Test
    fun `Should show warning in case correct version of test is not present in database`() {
        val table = OrangeMolecularRecordGenerator(
            emptySet(),
            emptyList(),
            1.0f,
            TestMolecularFactory.createProperWholeGenomeTest()
                .copy(targetSpecification = PanelTargetSpecification(emptyMap(), isNewerTest = true)),
            null
        )
        assertThat(
            getCellContents(
                table.contents(),
                0,
                0
            )
        ).isEqualTo("The date of this test is before the oldest version date of this test, the oldest version of the test is used")
    }
}