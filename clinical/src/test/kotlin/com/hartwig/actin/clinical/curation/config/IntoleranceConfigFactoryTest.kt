package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
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

    private val factory = IntoleranceConfigFactory(icdModel)

    @Test
    fun `Should return IntoleranceConfig from valid inputs`() {
        assertConfigCreation("name", "name")
    }

    @Test
    fun `Should return IntoleranceConfig with null name from valid inputs with empty curated name`() {
        assertConfigCreation(" ", null)
    }

    private fun assertConfigCreation(curatedName: String, expectedName: String?) {
        every { icdModel.isValidIcdTitle(icdTitle) } returns true
        every { icdModel.resolveCodeForTitle(icdTitle) } returns icdCode

        val config = factory.create(fields, arrayOf("input", curatedName, icdTitle, TreatmentCategory.IMMUNOTHERAPY.display()))
        assertThat(config.errors).isEmpty()
        with(config.config) {
            assertThat(input).isEqualTo("input")
            assertThat(ignore).isFalse()
            assertThat(curated?.name).isEqualTo(expectedName)
            assertThat(curated?.icdCodes).isEqualTo(setOf(icdCode))
        }
    }

    @Test
    fun `Should return an empty set for the treatmentCategories property if curation input is an empty string`() {
        every { icdModel.isValidIcdTitle(icdTitle) } returns true
        every { icdModel.resolveCodeForTitle(icdTitle) } returns icdCode

        val config = factory.create(fields, arrayOf("input", "name", icdTitle, ""))
        assertThat(config.errors).isEmpty()
    }

    @Test
    fun `Should return validation error when ICD title cannot be resolved to any code`() {
        every { icdModel.resolveCodeForTitle(icdTitle) } returns null
        val config = factory.create(fields, arrayOf("input", "name", icdTitle, ""))
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.INTOLERANCE,
                "input",
                "icd",
                "icdTitle",
                "icd",
                "ICD title \"icdTitle\" is not known - check for existence in ICD model"
            )
        )
    }
}