package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ToxicityConfigFactoryTest {
    private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.TOXICITY_TSV)
    private val icdModel = mockk<IcdModel>()
    private val icdTitle = "icdTitle"
    private val icdCodes = IcdCode("main", null)

    @Test
    fun `Should return ToxicityConfig from valid inputs`() {
        every { icdModel.isValidIcdTitle(icdTitle) } returns true
        every { icdModel.resolveCodeForTitle(icdTitle) } returns icdCodes

        val config = ToxicityConfigFactory(icdModel).create(fields, arrayOf("input", "name", "3", icdTitle))

        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.name).isEqualTo("name")
        assertThat(config.config.grade).isEqualTo(3)
        assertThat(config.config.icdCode.mainCode).isEqualTo(icdCodes.mainCode)
        assertThat(config.config.icdCode.extensionCode).isEqualTo(icdCodes.extensionCode)
    }

    @Test
    fun `Should return validation error when grade is not an integer`() {
        every { icdModel.isValidIcdTitle(icdTitle) } returns true
        every { icdModel.resolveCodeForTitle(icdTitle) } returns icdCodes

        val config = ToxicityConfigFactory(icdModel).create(fields, arrayOf("input", "name", "abc", icdTitle))
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
    fun `Should return validation error when icd title cannot be resolved to any code`() {
        every { icdModel.resolveCodeForTitle(icdTitle) } returns null
        val config = ToxicityConfigFactory(icdModel).create(fields, arrayOf("input", "name", "3", icdTitle))
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.TOXICITY.categoryName,
                "input",
                "icd",
                "icdTitle",
                "string",
                "ICD title \"icdTitle\" is not known - check for existence in resource"
            )
        )
    }
}