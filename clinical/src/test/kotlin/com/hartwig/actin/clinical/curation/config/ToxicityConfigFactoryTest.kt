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
    private val icdCode = IcdCode("main", null)

    @Test
    fun `Should return ToxicityConfig from valid inputs`() {
        every { icdModel.isValidIcdTitle(icdTitle) } returns true
        every { icdModel.resolveCodeForTitle(icdTitle) } returns icdCode

        val config = ToxicityConfigFactory(icdModel).create(fields, arrayOf("input", "name", "3", "$icdTitle;$icdTitle"))

        assertThat(config.errors).isEmpty()
        with(config.config) {
            assertThat(input).isEqualTo("input")
            assertThat(curated?.name).isEqualTo("name")
            assertThat((curated as? ToxicityCuration)?.grade).isEqualTo(3)
            assertThat(curated?.icdCodes).isEqualTo(setOf(icdCode))
        }
    }

    @Test
    fun `Should return validation error when grade is not an integer`() {
        every { icdModel.isValidIcdTitle(icdTitle) } returns true
        every { icdModel.resolveCodeForTitle(icdTitle) } returns icdCode

        val config = ToxicityConfigFactory(icdModel).create(fields, arrayOf("input", "name", "abc", icdTitle))
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(CurationCategory.TOXICITY.categoryName, "input", "grade", "abc", "integer")
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
                "icd",
                "ICD title \"icdTitle\" is not known - check for existence in ICD model"
            )
        )
    }
}