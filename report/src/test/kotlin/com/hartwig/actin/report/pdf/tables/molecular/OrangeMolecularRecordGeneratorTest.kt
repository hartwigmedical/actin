package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.TestMolecularFactory
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OrangeMolecularRecordGeneratorTest {
    private val highDriverVariant = TestMolecularFactory.createProperVariant().copy(driverLikelihood = DriverLikelihood.HIGH)
    private val mediumDriverVariant = TestMolecularFactory.createProperVariant().copy(driverLikelihood = DriverLikelihood.MEDIUM)
    private val lowDriverVariant = TestMolecularFactory.createProperVariant().copy(driverLikelihood = DriverLikelihood.LOW)

    private val highDriverCopyNumber = TestMolecularFactory.createProperCopyNumber().copy(driverLikelihood = DriverLikelihood.HIGH)
    private val mediumDriverCopyNumber = TestMolecularFactory.createProperCopyNumber().copy(driverLikelihood = DriverLikelihood.MEDIUM)
    private val lowDriverCopyNumber = TestMolecularFactory.createProperCopyNumber().copy(driverLikelihood = DriverLikelihood.LOW)

    private val molecularRecord = TestMolecularFactory.createMinimalTestMolecularRecord().copy(
        drivers = TestMolecularFactory.createProperTestDrivers().copy(
            variants = listOf(highDriverVariant, mediumDriverVariant, lowDriverVariant),
            copyNumbers = listOf(highDriverCopyNumber, mediumDriverCopyNumber, lowDriverCopyNumber)
        )
    )
    private val generator = OrangeMolecularRecordGenerator(emptySet(), emptyList(), 0.0F, molecularRecord, null)

    @Test
    fun `Should filter high drivers`() {
        val result = generator.filterDriversByDriverLikelihood(molecularRecord.drivers, true)
        assertThat(result.variants).isEqualTo(listOf(highDriverVariant))
        assertThat(result.copyNumbers).isEqualTo(listOf(highDriverCopyNumber))
    }

    @Test
    fun `Should filter non high drivers`() {
        val result = generator.filterDriversByDriverLikelihood(molecularRecord.drivers, false)
        assertThat(result.variants).isEqualTo(listOf(mediumDriverVariant, lowDriverVariant))
        assertThat(result.copyNumbers).isEqualTo(listOf(mediumDriverCopyNumber, lowDriverCopyNumber))
    }
}