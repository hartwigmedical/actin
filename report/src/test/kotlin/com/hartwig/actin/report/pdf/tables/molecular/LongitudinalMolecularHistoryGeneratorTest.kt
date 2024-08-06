package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.Drivers
import com.hartwig.actin.molecular.datamodel.MolecularCharacteristics
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.report.pdf.assertRow
import com.hartwig.actin.report.pdf.getWrappedTable
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

val FIRST_TEST = TestMolecularFactory.createMinimalTestMolecularRecord().copy(date = LocalDate.of(2024, 7, 21))
val SECOND_TEST = FIRST_TEST.copy(date = FIRST_TEST.date?.plusDays(1))
val VARIANT = TestMolecularFactory.createProperVariant()

class LongitudinalMolecularHistoryGeneratorTest {

    @Test
    fun `Should create table with header with column for each test`() {
        val result = LongitudinalMolecularHistoryGenerator(MolecularHistory(listOf(FIRST_TEST, SECOND_TEST)), 1f)
        assertThat(result.title()).isEqualTo("Molecular history")
        assertRow(
            getWrappedTable(result).header,
            0,
            "Event",
            "Description",
            "Driver likelihood",
            "2024-07-21\nHartwig WGS",
            "2024-07-22\nHartwig WGS"
        )
    }

    @Test
    fun `Should create row for each variant and mark as detected in correct tests`() {
        val result = LongitudinalMolecularHistoryGenerator(
            MolecularHistory(
                listOf(
                    FIRST_TEST.copy(drivers = Drivers(variants = setOf(VARIANT))), SECOND_TEST
                )
            ), 1f
        )
        assertRow(getWrappedTable(result), 0, "BRAF V600E", "Missense\nGain of function\nHotspot", "High", "Detected", "Not detected")
    }

    @Test
    fun `Should create row for TMB and assign value to the correct test`() {
        val result = LongitudinalMolecularHistoryGenerator(
            MolecularHistory(
                listOf(
                    FIRST_TEST.copy(characteristics = MolecularCharacteristics(tumorMutationalBurden = 1.0)),
                    SECOND_TEST.copy(characteristics = MolecularCharacteristics(tumorMutationalBurden = 2.0))
                )
            ), 1f
        )
        assertRow(getWrappedTable(result), 0, "TMB", "", "", "1.0", "2.0")
    }

    @Test
    fun `Should create row for MSI and assign value to the correct test`() {
        val result = LongitudinalMolecularHistoryGenerator(
            MolecularHistory(
                listOf(
                    FIRST_TEST.copy(characteristics = MolecularCharacteristics(isMicrosatelliteUnstable = false)),
                    SECOND_TEST.copy(characteristics = MolecularCharacteristics(isMicrosatelliteUnstable = true)),
                    SECOND_TEST.copy(
                        date = SECOND_TEST.date?.plusDays(1), characteristics = MolecularCharacteristics()
                    )
                )
            ), 1f
        )
        assertRow(getWrappedTable(result), 1, "MSI", "", "", "Stable", "Unstable", "")
    }
}


