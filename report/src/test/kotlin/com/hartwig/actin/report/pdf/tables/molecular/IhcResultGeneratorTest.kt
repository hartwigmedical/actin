package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.report.interpretation.IhcTestInterpreter
import com.hartwig.actin.report.pdf.tables.CellTestUtil.extractTextFromCell
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class IhcResultGeneratorTest {

    private val testDate = LocalDate.of(2024, 5, 10)
    private val ihcTest1 = IhcTest(item = "PD-L1", measure = "TPS", measureDate = testDate, scoreLowerBound = 2.0, scoreUpperBound = 2.0, scoreValueUnit = "%")
    private val ihcTest2 = IhcTest(item = "ALK", measureDate = testDate, scoreText = "Negative")
    private val ihcTest3 = IhcTest(item = "NTRK1", measureDate = testDate, scoreText = "Negative")
    private val ihcTest4 = IhcTest(item = "NTRK2", measureDate = testDate, scoreText = "Negative")
    private val ihcTest5 = IhcTest(item = "NTRK3", measureDate = testDate, scoreText = "Negative")

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
        assertThat(extractTextFromCell(table.getCell(0, 0))).isEqualTo("ALK, NTRK1, NTRK2, NTRK3 (2024-05-10)")
        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("Negative")
        assertThat(extractTextFromCell(table.getCell(1, 0))).isEqualTo("PD-L1 (2024-05-10)")
        assertThat(extractTextFromCell(table.getCell(1, 1))).isEqualTo("Score TPS 2%")
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
        assertThat(extractTextFromCell(table.getCell(0, 0))).isEqualTo("ALK, NTRK1 (2024-05-10)\nNTRK2 (2024-05-09)\nNTRK3")
        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("Negative")
        assertThat(extractTextFromCell(table.getCell(1, 0))).isEqualTo("PD-L1 (2024-05-10)")
        assertThat(extractTextFromCell(table.getCell(1, 1))).isEqualTo("Score TPS 2%")
    }

    @Test
    fun `Should display exclusive lower bound with greater-than operator`() {
        val test = IhcTest(item = "PD-L1", measureDate = testDate, scoreLowerBound = 50.0, isLowerBoundInclusive = false, scoreValueUnit = "%")
        val generator = IhcResultGenerator(listOf(test), 10.0f, 10.0f, IhcTestInterpreter())
        val table = generator.contents()
        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("Score > 50%")
    }

    @Test
    fun `Should display exclusive upper bound with less-than operator`() {
        val test = IhcTest(item = "PD-L1", measureDate = testDate, scoreUpperBound = 1.0, isUpperBoundInclusive = false, scoreValueUnit = "%")
        val generator = IhcResultGenerator(listOf(test), 10.0f, 10.0f, IhcTestInterpreter())
        val table = generator.contents()
        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("Score < 1%")
    }

    @Test
    fun `Should display inclusive lower bound with greater-than-or-equal operator`() {
        val test = IhcTest(item = "PD-L1", measureDate = testDate, scoreLowerBound = 50.0, isLowerBoundInclusive = true, scoreValueUnit = "%")
        val generator = IhcResultGenerator(listOf(test), 10.0f, 10.0f, IhcTestInterpreter())
        val table = generator.contents()
        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("Score >= 50%")
    }

    @Test
    fun `Should display inclusive upper bound with less-than-or-equal operator`() {
        val test = IhcTest(item = "PD-L1", measureDate = testDate, scoreUpperBound = 1.0, isUpperBoundInclusive = true, scoreValueUnit = "%")
        val generator = IhcResultGenerator(listOf(test), 10.0f, 10.0f, IhcTestInterpreter())
        val table = generator.contents()
        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("Score <= 1%")
    }

    @Test
    fun `Should default to inclusive when inclusivity flag is null`() {
        val test = IhcTest(item = "PD-L1", measureDate = testDate, scoreUpperBound = 5.0, scoreValueUnit = "%")
        val generator = IhcResultGenerator(listOf(test), 10.0f, 10.0f, IhcTestInterpreter())
        val table = generator.contents()
        assertThat(extractTextFromCell(table.getCell(0, 1))).isEqualTo("Score <= 5%")
    }
}