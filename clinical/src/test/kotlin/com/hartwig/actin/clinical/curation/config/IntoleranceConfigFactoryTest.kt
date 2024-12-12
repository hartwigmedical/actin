package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.icd.IcdModel
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val DOID = "123"

class IntoleranceConfigFactoryTest {
    private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.INTOLERANCE_TSV)

    private val icdModel = mockk<IcdModel>()
    private val icdTitle = "icdTitle"
    private val icdCodes = IcdCode("main", null)

    private val curationDoidValidator = mockk<CurationDoidValidator>()
    private val victim = IntoleranceConfigFactory(curationDoidValidator, icdModel)

    @Test
    fun `Should return IntoleranceConfig from valid inputs`() {
        every { icdModel.isValidIcdTitle(icdTitle) } returns true
        every { icdModel.resolveCodeForTitle(icdTitle) } returns icdCodes

        every {
            curationDoidValidator.isValidDiseaseDoidSet(
                setOf(DOID)
            )
        } returns true
        val config = victim.create(fields, arrayOf("input", "name", icdTitle, DOID, TreatmentCategory.IMMUNOTHERAPY.display()))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.ignore).isFalse()
        assertThat(config.config.name).isEqualTo("name")
        assertThat(config.config.doids).containsExactly(DOID)
        assertThat(config.config.icd.mainCode).isEqualTo(icdCodes.mainCode)
        assertThat(config.config.icd.extensionCode).isEqualTo(icdCodes.extensionCode)
        assertThat(config.config.treatmentCategories).isEqualTo(setOf(TreatmentCategory.IMMUNOTHERAPY))
    }

    @Test
    fun `Should return an empty set for the treatmentCategories property if curation input is an empty string`() {
        every { icdModel.isValidIcdTitle(icdTitle) } returns true
        every { icdModel.resolveCodeForTitle(icdTitle) } returns icdCodes

        every {
            curationDoidValidator.isValidDiseaseDoidSet(
                setOf(DOID)
            )
        } returns true
        val config = victim.create(fields, arrayOf("input", "name", icdTitle, DOID, ""))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.treatmentCategories).isEqualTo(emptySet<TreatmentCategory>())
    }

    @Test
    fun `Should return validation error when doids are invalid`() {
        every { icdModel.isValidIcdTitle(icdTitle) } returns true
        every { icdModel.resolveCodeForTitle(icdTitle) } returns icdCodes

        val doidValidator: CurationDoidValidator = curationDoidValidator
        every {
            doidValidator.isValidDiseaseDoidSet(
                setOf(DOID)
            )
        } returns false
        val config: ValidatedCurationConfig<IntoleranceConfig> =
            IntoleranceConfigFactory(doidValidator, icdModel).create(fields, arrayOf("input", "name", icdTitle, DOID, ""))
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.INTOLERANCE.categoryName,
                "input",
                "doids",
                "[$DOID]",
                "doids"
            )
        )
    }

    @Test
    fun `Should return validation error when icd title cannot be resolved to any code`() {
        every { icdModel.resolveCodeForTitle(icdTitle) } returns null
        every { curationDoidValidator.isValidDiseaseDoidSet(setOf(DOID)) } returns true
        val config = victim.create(fields, arrayOf("input", "name", icdTitle, DOID, ""))
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.INTOLERANCE.categoryName,
                "input",
                "icd",
                "icdTitle",
                "string",
                "ICD title \"icdTitle\" is not known - check for existence in resource"
            )
        )
    }
}