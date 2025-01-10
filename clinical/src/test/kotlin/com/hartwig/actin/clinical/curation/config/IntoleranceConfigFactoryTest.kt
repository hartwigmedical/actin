package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.icd.IcdModel
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IntoleranceConfigFactoryTest {
    private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.INTOLERANCE_TSV)

    private val icdModel = mockk<IcdModel>()
    private val icdTitle = "icdTitle"
    private val icdCode = IcdCode("main", null)

    private val victim = IntoleranceConfigFactory(icdModel)

    @Test
    fun `Should return IntoleranceConfig from valid inputs`() {
        every { icdModel.isValidIcdTitle(icdTitle) } returns true
        every { icdModel.resolveCodeForTitle(icdTitle) } returns icdCode

        val config = victim.create(fields, arrayOf("input", "name", icdTitle, TreatmentCategory.IMMUNOTHERAPY.display()))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.ignore).isFalse()
        assertThat(config.config.name).isEqualTo("name")
        assertThat(config.config.icd).isEqualTo(setOf(icdCode))
    }

    @Test
    fun `Should return an empty set for the treatmentCategories property if curation input is an empty string`() {
        every { icdModel.isValidIcdTitle(icdTitle) } returns true
        every { icdModel.resolveCodeForTitle(icdTitle) } returns icdCode

        val config = victim.create(fields, arrayOf("input", "name", icdTitle, ""))
        assertThat(config.errors).isEmpty()
    }

    @Test
    fun `Should return validation error when ICD title cannot be resolved to any code`() {
        every { icdModel.resolveCodeForTitle(icdTitle) } returns null
        val config = victim.create(fields, arrayOf("input", "name", icdTitle, ""))
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.INTOLERANCE.categoryName,
                "input",
                "icd",
                "icdTitle",
                "icd",
                "ICD title \"icdTitle\" is not known - check for existence in ICD model"
            )
        )
    }
}