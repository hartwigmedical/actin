package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SecondPrimaryConfigFactoryTest {
    private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.SECOND_PRIMARY_TSV)

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
        assertThat(secondPrimary.tumorLocation).isEqualTo("tumorLocation")
        assertThat(secondPrimary.tumorSubLocation).isEqualTo("tumorSubLocation")
        assertThat(secondPrimary.tumorType).isEqualTo("tumorType")
        assertThat(secondPrimary.tumorSubType).isEqualTo("tumorSubType")
        assertThat(secondPrimary.doids).containsExactly("123")
        assertThat(secondPrimary.diagnosedYear).isEqualTo(2023)
        assertThat(secondPrimary.diagnosedMonth).isEqualTo(12)
        assertThat(secondPrimary.treatmentHistory).isEqualTo("treatmentHistory")
        assertThat(secondPrimary.lastTreatmentYear).isEqualTo(2023)
        assertThat(secondPrimary.lastTreatmentMonth).isEqualTo(12)
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
                    CurationCategory.SECOND_PRIMARY,
                    "input",
                    "doids",
                    "[123]",
                    "doids"
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
                    CurationCategory.SECOND_PRIMARY,
                    "input",
                    "status",
                    "not a status",
                    "TumorStatus",
                    "Accepted values are [ACTIVE, INACTIVE, EXPECTATIVE, UNKNOWN]"
                )
            )
    }

    @Test
    fun `Should return no validation errors and null second primary when ignore string as input`() {
        val curationDoidValidator = mockk<CurationDoidValidator>()
        val config = SecondPrimaryConfigFactory(curationDoidValidator).create(
            fields,
            arrayOf(
                "input",
                "<ignore>",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                ""
            )
        )
        assertThat(config.config.curated).isNull()
        assertThat(config.errors).isEmpty()
    }
}