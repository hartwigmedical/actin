package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.report.interpretation.IhcTestInterpreter
import com.hartwig.actin.report.pdf.tables.clinical.CellTestUtil.extractTextFromCell
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class IhcResultGeneratorTest {

    private val testDate = LocalDate.of(2024, 5, 10)
    private val ihcTest1 = IhcTest("PD-L1", "TPS", testDate, null, null, 2.0, "%", false)
    private val ihcTest2 = IhcTest("ALK", null, testDate, "Negative", null, null, null, false)
    private val ihcTest3 = IhcTest("NTRK1", null, testDate, "Negative", null, null, null, false)
    private val ihcTest4 = IhcTest("NTRK2", null, testDate, "Negative", null, null, null, false)
    private val ihcTest5 = IhcTest("NTRK3", null, testDate, "Negative", null, null, null, false)

    private val ihcResultGenerator = IhcResultGenerator(emptyList(), 10.0f, 10.0f, IhcTestInterpreter())

    @Test
    fun `Should return IHC results titles`() {
        assertThat(ihcResultGenerator.title()).isEqualTo("IHC results")
    }

    @Test
    fun `Should return content grouped per date when all IHC tests have the same date`() {

        val generator = IhcResultGenerator(
            ihcTests = listOf(ihcTest1, ihcTest2, ihcTest3, ihcTest4, ihcTest5),
            keyWidth = 10.0f,
            valueWidth = 10.0f,
            interpreter = IhcTestInterpreter()
        )
        val table = generator.contents()
        assertThat(table.numberOfRows).isEqualTo(2)
        assertThat(extractTextFromCell(table.getCell(0, 0))).isEqualTo("Negative")
        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("ALK, NTRK1, NTRK2, NTRK3 (2024-05-10)")
        assertThat(extractTextFromCell(table.getCell(1, 0))).isEqualTo("PD-L1")
        assertThat(extractTextFromCell(table.getCell(1, 1))).isEqualTo("Score TPS 2% (2024-05-10)")
    }

    @Test
    fun `Should return content grouped per date when all IHC tests have different dates`() {

        val generator = IhcResultGenerator(
            ihcTests = listOf(
                ihcTest1,
                ihcTest2,
                ihcTest3,
                ihcTest4.copy(measureDate = testDate.minusDays(1)),
                ihcTest5.copy(measureDate = null)
            ),
            keyWidth = 10.0f,
            valueWidth = 10.0f,
            interpreter = IhcTestInterpreter()
        )
        val table = generator.contents()
        assertThat(table.numberOfRows).isEqualTo(2)
        assertThat(extractTextFromCell(table.getCell(0, 0))).isEqualTo("Negative")
        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("ALK, NTRK1 (2024-05-10)\nNTRK2 (2024-05-09)\nNTRK3")
        assertThat(extractTextFromCell(table.getCell(1, 0))).isEqualTo("PD-L1")
        assertThat(extractTextFromCell(table.getCell(1, 1))).isEqualTo("Score TPS 2% (2024-05-10)")
    }

}