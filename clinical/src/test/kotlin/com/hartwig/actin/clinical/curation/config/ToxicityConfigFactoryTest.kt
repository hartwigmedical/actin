package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.CurationIcdValidator
import com.hartwig.actin.clinical.curation.TestCurationFactory
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ToxicityConfigFactoryTest {
    private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.TOXICITY_TSV)
    private val curationIcdValidator = mockk<CurationIcdValidator>()
    private val icdTitle = "icdTitle"
    private val icdCode = "icdCode"

    @Test
    fun `Should return ToxicityConfig from valid inputs`() {
        every { curationIcdValidator.isValidIcdTitle(icdTitle) } returns true
        every { curationIcdValidator.getCodeFromTitle(icdTitle) } returns icdCode

        val config = ToxicityConfigFactory(curationIcdValidator).create(fields, arrayOf("input", "name", "categories", "3", icdTitle))

        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.name).isEqualTo("name")
        assertThat(config.config.categories).containsExactly("categories")
        assertThat(config.config.grade).isEqualTo(3)
        assertThat(config.config.icdCode).isEqualTo(icdCode)
    }

    @Test
    fun `Should return validation error when grade is not an integer`() {
        every { curationIcdValidator.isValidIcdTitle(icdTitle) } returns true
        every { curationIcdValidator.getCodeFromTitle(icdTitle) } returns icdCode

        val config = ToxicityConfigFactory(curationIcdValidator).create(fields, arrayOf("input", "name", "categories", "abc", icdTitle))
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.TOXICITY.categoryName,
                "input",
                "grade",
                "abc",
                "integer"
            )
        )
    }

    @Test
    fun `Should return validation error when icd title is not valid`() {
        every { curationIcdValidator.isValidIcdTitle(icdTitle) } returns false
        val config = ToxicityConfigFactory(curationIcdValidator).create(fields, arrayOf("input", "name", "categories", "3", icdTitle))
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.TOXICITY.categoryName,
                "input",
                "icd",
                "icdTitle",
                "string",
                "ICD title is not known - check for existence in resource"
            )
        )
    }
}