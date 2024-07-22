package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

val FIRST_TEST = TestMolecularFactory.createMinimalTestMolecularRecord().copy(date = LocalDate.of(2024, 7, 21))
val SECOND_TEST = FIRST_TEST.copy(date = FIRST_TEST.date?.plusDays(1))
val VARIANT = TestMolecularFactory.createProperVariant()

class LongitudinalMolecularHistoryGeneratorTest {

    @Test
    fun `Should create table with header with column for each test`() {
        val result =
            LongitudinalMolecularHistoryGenerator(MolecularHistory(listOf(FIRST_TEST, SECOND_TEST)), 1f)
        assertThat(result.title()).isEqualTo("Molecular history")
        val contentTable = getWrappedTable(result)
        assertThat(getCellContents(contentTable.header, 0, 0)).isEqualTo("Event")
        assertThat(getCellContents(contentTable.header, 0, 1)).isEqualTo("Description")
        assertThat(getCellContents(contentTable.header, 0, 2)).isEqualTo("Driver likelihood")
        assertThat(getCellContents(contentTable.header, 0, 3)).isEqualTo("2024-07-21\nHartwig WGS")
        assertThat(getCellContents(contentTable.header, 0, 4)).isEqualTo("2024-07-22\nHartwig WGS")
    }

    @Test
    fun `Should create row for each variant and mark as detected in correct tests`() {
        val result =
            LongitudinalMolecularHistoryGenerator(
                MolecularHistory(
                    listOf(
                        FIRST_TEST.copy(drivers = Drivers(variants = setOf(VARIANT))),
                        SECOND_TEST
                    )
                ), 1f
            )
        val contentTable = getWrappedTable(result)
        assertThat(getCellContents(contentTable, 0, 0)).isEqualTo("BRAF V600E")
        assertThat(getCellContents(contentTable, 0, 1)).isEqualTo("Missense\nGain of function\nHotspot")
        assertThat(getCellContents(contentTable, 0, 2)).isEqualTo("High")
        assertThat(getCellContents(contentTable, 0, 3)).isEqualTo("Detected")
        assertThat(getCellContents(contentTable, 0, 4)).isEqualTo("Not detected")
    }

    @Test
    fun `Should create row for TMB and assign value to the correct test`() {
        val result =
            LongitudinalMolecularHistoryGenerator(
                MolecularHistory(
                    listOf(
                        FIRST_TEST.copy(characteristics = MolecularCharacteristics(tumorMutationalBurden = 1.0)),
                        SECOND_TEST.copy(characteristics = MolecularCharacteristics(tumorMutationalBurden = 2.0))
                    )
                ), 1f
            )
        val contentTable = getWrappedTable(result)
        assertThat(getCellContents(contentTable, 0, 0)).isEqualTo("TMB")
        assertThat(getCellContents(contentTable, 0, 1)).isEqualTo("")
        assertThat(getCellContents(contentTable, 0, 2)).isEqualTo("")
        assertThat(getCellContents(contentTable, 0, 3)).isEqualTo("1.0")
        assertThat(getCellContents(contentTable, 0, 4)).isEqualTo("2.0")
    }

    @Test
    fun `Should create row for MSI and assign value to the correct test`() {
        val result =
            LongitudinalMolecularHistoryGenerator(
                MolecularHistory(
                    listOf(
                        FIRST_TEST.copy(characteristics = MolecularCharacteristics(isMicrosatelliteUnstable = false)),
                        SECOND_TEST.copy(characteristics = MolecularCharacteristics(isMicrosatelliteUnstable = true))
                    )
                ), 1f
            )
        val contentTable = getWrappedTable(result)
        assertThat(getCellContents(contentTable, 1, 0)).isEqualTo("MSI")
        assertThat(getCellContents(contentTable, 1, 1)).isEqualTo("")
        assertThat(getCellContents(contentTable, 1, 2)).isEqualTo("")
        assertThat(getCellContents(contentTable, 1, 3)).isEqualTo("Stable")
        assertThat(getCellContents(contentTable, 1, 4)).isEqualTo("Unstable")
    }
}

private fun getWrappedTable(result: LongitudinalMolecularHistoryGenerator) =
    (result.contents().getCell(0, 0).children[0] as Table)

private fun getCellContents(table: Table, row: Int, column: Int) =
    ((table.getCell(row, column).children[0] as Paragraph).children[0] as Text).text

