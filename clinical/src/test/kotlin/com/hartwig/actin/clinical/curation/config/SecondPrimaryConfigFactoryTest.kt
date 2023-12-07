package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SecondPrimaryConfigFactoryTest {
    private val fields: Map<String, Int> =
        CurationConfigFile.readTsv(CURATION_DIRECTORY + CurationDatabaseReader.SECOND_PRIMARY_TSV).second

    @Test
    fun `Should return SecondPrimaryConfig from valid inputs`() {
        val curationDoidValidator = mockk<CurationDoidValidator>()
        every { curationDoidValidator.isValidCancerDoidSet(setOf("123")) } returns true
        val config = SecondPrimaryConfigFactory(curationDoidValidator).create(
            fields,
            arrayOf(
                "input",
                "name",
                "tumorLocation",
                "tumorSubLocation",
                "tumorType",
                "tumorSubType",
                "123",
                "2023",
                "12",
                "treatmentHistory",
                "2023",
                "12",
                "active"
            )
        )
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.ignore).isFalse()
        val secondPrimary = config.config.curated!!
        assertThat(secondPrimary.tumorLocation()).isEqualTo("tumorLocation")
        assertThat(secondPrimary.tumorSubLocation()).isEqualTo("tumorSubLocation")
        assertThat(secondPrimary.tumorType()).isEqualTo("tumorType")
        assertThat(secondPrimary.tumorSubType()).isEqualTo("tumorSubType")
        assertThat(secondPrimary.doids()).containsExactly("123")
        assertThat(secondPrimary.diagnosedYear()).isEqualTo(2023)
        assertThat(secondPrimary.diagnosedMonth()).isEqualTo(12)
        assertThat(secondPrimary.treatmentHistory()).isEqualTo("treatmentHistory")
        assertThat(secondPrimary.lastTreatmentYear()).isEqualTo(2023)
        assertThat(secondPrimary.lastTreatmentMonth()).isEqualTo(12)
    }

    @Test
    fun `Should return validation errors when doids are invalid`() {
        val curationDoidValidator = mockk<CurationDoidValidator>()
        every { curationDoidValidator.isValidCancerDoidSet(setOf("123")) } returns false
        val config = SecondPrimaryConfigFactory(curationDoidValidator).create(
            fields,
            arrayOf(
                "input",
                "name",
                "tumorLocation",
                "tumorSubLocation",
                "tumorType",
                "tumorSubType",
                "123",
                "2023",
                "12",
                "treatmentHistory",
                "2023",
                "12",
                "active"
            )
        )
        assertThat(config.errors)
            .containsExactly(
                CurationConfigValidationError(
                    "Second primary config with input 'input' contains at least one invalid doid: '[123]'"
                )
            )
    }

    @Test
    fun `Should return validation errors when tumor status is invalid`() {
        val curationDoidValidator = mockk<CurationDoidValidator>()
        every { curationDoidValidator.isValidCancerDoidSet(setOf("123")) } returns true
        val config = SecondPrimaryConfigFactory(curationDoidValidator).create(
            fields,
            arrayOf(
                "input",
                "name",
                "tumorLocation",
                "tumorSubLocation",
                "tumorType",
                "tumorSubType",
                "123",
                "2023",
                "12",
                "treatmentHistory",
                "2023",
                "12",
                "not a status"
            )
        )
        assertThat(config.errors)
            .containsExactly(
                CurationConfigValidationError(
                    "Invalid enum value 'not a status' for enum 'TumorStatus'. Accepted values are [ACTIVE, INACTIVE, EXPECTATIVE]"
                )
            )
    }
}