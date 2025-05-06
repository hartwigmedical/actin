package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PriorPrimaryConfigFactoryTest {
    private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.PRIOR_PRIMARY_TSV)

    @Test
    fun `Should return PriorPrimaryConfig from valid inputs`() {
        val curationDoidValidator = mockk<CurationDoidValidator>()
        every { curationDoidValidator.isValidCancerDoidSet(setOf("123")) } returns true
        val config = PriorPrimaryConfigFactory(curationDoidValidator).create(
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
        val priorPrimary = config.config.curated!!
        assertThat(priorPrimary.tumorLocation).isEqualTo("tumorLocation")
        assertThat(priorPrimary.tumorSubLocation).isEqualTo("tumorSubLocation")
        assertThat(priorPrimary.tumorType).isEqualTo("tumorType")
        assertThat(priorPrimary.tumorSubType).isEqualTo("tumorSubType")
        assertThat(priorPrimary.doids).containsExactly("123")
        assertThat(priorPrimary.diagnosedYear).isEqualTo(2023)
        assertThat(priorPrimary.diagnosedMonth).isEqualTo(12)
        assertThat(priorPrimary.treatmentHistory).isEqualTo("treatmentHistory")
        assertThat(priorPrimary.lastTreatmentYear).isEqualTo(2023)
        assertThat(priorPrimary.lastTreatmentMonth).isEqualTo(12)
    }

    @Test
    fun `Should return validation errors when doids are invalid`() {
        val curationDoidValidator = mockk<CurationDoidValidator>()
        every { curationDoidValidator.isValidCancerDoidSet(setOf("123")) } returns false
        val config = PriorPrimaryConfigFactory(curationDoidValidator).create(
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
                    CurationCategory.PRIOR_PRIMARY,
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
        val config = PriorPrimaryConfigFactory(curationDoidValidator).create(
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
                    CurationCategory.PRIOR_PRIMARY,
                    "input",
                    "status",
                    "not a status",
                    "TumorStatus",
                    "Accepted values are [ACTIVE, INACTIVE, EXPECTATIVE, UNKNOWN]"
                )
            )
    }

    @Test
    fun `Should return no validation errors and null prior primary when ignore string as input`() {
        val curationDoidValidator = mockk<CurationDoidValidator>()
        val config = PriorPrimaryConfigFactory(curationDoidValidator).create(
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