package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.icd.TestIcdFactory
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NonOncologicalHistoryConfigFactoryTest {
    private val fields: Map<String, Int> =
        TestCurationFactory.curationHeaders(CurationDatabaseReader.NON_ONCOLOGICAL_HISTORY_TSV)
    private val icdModel = TestIcdFactory.createTestModel()
    private val icdMainCode = icdModel.codeToNodeMap.keys.first()
    private val icdExtensionCode = icdModel.codeToNodeMap.keys.last()
    private val icdTitle = icdModel.codeToNodeMap[icdMainCode]!!.title
    private val icdExtension = icdModel.codeToNodeMap[icdExtensionCode]!!.title

    @Test
    fun `Should return NonOncologicalHistoryConfig with no prior other condition from valid inputs is lvef`() {
        val doidValidator = setupDoidValidator()
        every { doidValidator.isValidDiseaseDoidSet(setOf("123")) } returns true
        val config = NonOncologicalHistoryConfigFactory(doidValidator, icdModel).create(
            fields,
            arrayOf("input", "name", "2023", "12", "123", "category", icdTitle, "1", "1.0", "")
        )
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.ignore).isEqualTo(false)
        assertThat(config.config.lvef).isEqualTo(1.0)
        assertThat(config.config.priorOtherCondition).isNull()
    }

    @Test
    fun `Should return NonOncologicalHistoryConfig with prior other condition from valid inputs is not lvef`() {
        val doidValidator = setupDoidValidator()
        val config = NonOncologicalHistoryConfigFactory(doidValidator, icdModel).create(
            fields,
            arrayOf("input", "name", "2023", "12", "123", "category", "$icdTitle&$icdExtension", "0", "", "1")
        )
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.ignore).isEqualTo(false)
        assertThat(config.config.lvef).isNull()
        val priorOtherCondition = config.config.priorOtherCondition!!
        assertThat(priorOtherCondition.icdCode.mainCode).isEqualTo(icdMainCode)
        assertThat(priorOtherCondition.icdCode.extensionCode).isEqualTo(icdExtensionCode)
        assertThat(priorOtherCondition.doids).containsExactly("123")
        assertThat(priorOtherCondition.name).isEqualTo("name")
        assertThat(priorOtherCondition.year).isEqualTo(2023)
        assertThat(priorOtherCondition.month).isEqualTo(12)
        assertThat(priorOtherCondition.category).isEqualTo("category")
        assertThat(priorOtherCondition.isContraindicationForTherapy).isEqualTo(true)
    }

    @Test
    fun `Should return validation error when year and month are not numbers`() {
        val doidValidator = setupDoidValidator()
        val config = NonOncologicalHistoryConfigFactory(doidValidator, icdModel).create(
            fields,
            arrayOf("input", "name", "year", "month", "123", "category", icdTitle, "0", "", "1")
        )
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.NON_ONCOLOGICAL_HISTORY.categoryName,
                "input",
                "year",
                "year",
                "integer"
            ), CurationConfigValidationError(
                CurationCategory.NON_ONCOLOGICAL_HISTORY.categoryName,
                "input",
                "month",
                "month",
                "integer"
            )
        )
    }

    @Test
    fun `Should return validation error when is lvef and lvef value is not a number`() {
        val doidValidator = setupDoidValidator()
        val config = NonOncologicalHistoryConfigFactory(doidValidator, icdModel).create(
            fields,
            arrayOf("input", "name", "2023", "12", "123", "category", icdTitle, "1", "invalid", "1")
        )
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.NON_ONCOLOGICAL_HISTORY.categoryName,
                "input",
                "lvefValue",
                "invalid",
                "double"
            )
        )
    }

    @Test
    fun `Should return validation error when is isContraindicationForTherapy is not a boolean`() {
        val doidValidator = setupDoidValidator()
        val config = NonOncologicalHistoryConfigFactory(doidValidator, icdModel).create(
            fields,
            arrayOf("input", "name", "2023", "12", "123", "category", icdTitle, "string", "1.0", "no")
        )
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.NON_ONCOLOGICAL_HISTORY.categoryName,
                "input",
                "isContraindicationForTherapy",
                "no",
                "boolean"
            )
        )
    }

    @Test
    fun `Should return validation error when not lvef and doids are not valid`() {
        val doidValidator = setupDoidValidator(false)
        val config = NonOncologicalHistoryConfigFactory(doidValidator, icdModel).create(
            fields,
            arrayOf("input", "name", "2023", "12", "123", "category", icdTitle, "0", "1.0", "1")
        )
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.NON_ONCOLOGICAL_HISTORY.categoryName,
                "input",
                "doids",
                "[123]",
                "doids"
            )
        )
    }

    @Test
    fun `Should return validation error when impossible to solve ICD code for title`() {
        val doidValidator = setupDoidValidator()
        val config = NonOncologicalHistoryConfigFactory(doidValidator, icdModel).create(
            fields,
            arrayOf("input", "name", "2023", "12", "123", "category", "unknown title", "0", "", "1")
        )
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.NON_ONCOLOGICAL_HISTORY.categoryName,
                "input",
                "icd",
                "unknown title",
                "string",
                "ICD title \"unknown title\" is not known - check for existence in resource"
            )
        )
    }

    @Test
    fun `Should return validation error when impossible to solve ICD code for extension of title`() {
        val doidValidator = setupDoidValidator()
        val config = NonOncologicalHistoryConfigFactory(doidValidator, icdModel).create(
            fields,
            arrayOf("input", "name", "2023", "12", "123", "category", "$icdTitle&unknownExtension", "0", "", "1")
        )
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.NON_ONCOLOGICAL_HISTORY.categoryName,
                "input",
                "icd",
                "$icdTitle&unknownExtension",
                "string",
                "ICD title \"$icdTitle&unknownExtension\" is not known - check for existence in resource"
            )
        )
    }

    private fun setupDoidValidator(valid: Boolean = true): CurationDoidValidator {
        val doidValidator = mockk<CurationDoidValidator>()
        every { doidValidator.isValidDiseaseDoidSet(setOf("123")) } returns valid
        return doidValidator
    }
}