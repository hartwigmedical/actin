package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.panel.PanelTargetSpecification
import com.hartwig.actin.report.pdf.getCellContents
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class OrangeMolecularRecordGeneratorTest {

    @Test
    fun `Should show warning in case the date of the molecular test is before the oldest version date of this test`() {
        val date = LocalDate.of(2023, 9, 19)
        val table = OrangeMolecularRecordGenerator(
            emptySet(),
            emptyList(),
            1.0f,
            TestMolecularFactory.createProperWholeGenomeTest()
                .copy(
                    date = date,
                    targetSpecification = PanelTargetSpecification(emptyMap(), date.plusYears(1), testDateIsBeforeOldestTestVersion = true)
                ),
            null
        )
        assertThat(
            getCellContents(
                table.contents(),
                0,
                0
            )
        ).isEqualTo("The date of this test (2023-09-19) is older than the date of the oldest version of the test for which we could derive which genes were tested (2024-09-19). This version is still used to determine which genes were tested. This determination is potentially not correct.")
    }
}