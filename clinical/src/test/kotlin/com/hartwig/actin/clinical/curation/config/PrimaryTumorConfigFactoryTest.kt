package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PrimaryTumorConfigFactoryTest {
    private val fields: Map<String, Int> = CurationConfigFile.readTsv(CURATION_DIRECTORY + CurationDatabaseReader.PRIMARY_TUMOR_TSV).second

    @Test
    fun `Should return PrimaryTumorConfig from valid inputs`() {
        val doidValidator = mockk<CurationDoidValidator>()
        every { doidValidator.isValidCancerDoidSet(setOf("123")) } returns true
        val config = PrimaryTumorConfigFactory(doidValidator).create(
            fields,
            arrayOf(
                "input",
                "primaryTumorLocation",
                "primaryTumorSubLocation",
                "primaryTumorType",
                "primaryTumorSubType",
                "primaryTumorExtraDetails",
                "123"
            )
        )
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.primaryTumorLocation).isEqualTo("primaryTumorLocation")
        assertThat(config.config.primaryTumorSubLocation).isEqualTo("primaryTumorSubLocation")
        assertThat(config.config.primaryTumorType).isEqualTo("primaryTumorType")
        assertThat(config.config.primaryTumorSubType).isEqualTo("primaryTumorSubType")
        assertThat(config.config.primaryTumorExtraDetails).isEqualTo("primaryTumorExtraDetails")
        assertThat(config.config.doids).containsExactly("123")
    }

    @Test
    fun `Should return validation error when doid is invalid from valid`() {
        val doidValidator = mockk<CurationDoidValidator>()
        every { doidValidator.isValidCancerDoidSet(setOf("123")) } returns false
        val config = PrimaryTumorConfigFactory(doidValidator).create(
            fields,
            arrayOf(
                "input",
                "primaryTumorLocation",
                "primaryTumorSubLocation",
                "primaryTumorType",
                "primaryTumorSubType",
                "primaryTumorExtraDetails",
                "123"
            )
        )
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError("Primary tumor config with input 'input' contains at least one invalid doid: 'input'")
        )
    }
}