package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.icd.TestIcdFactory
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
        val config = NonOncologicalHistoryConfigFactory(icdModel).create(
            fields,
            arrayOf("input", "name", "2023", "12", "category", icdTitle, "1", "1.0", "")
        )
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.ignore).isEqualTo(false)
        assertThat(config.config.lvef).isEqualTo(1.0)
        assertThat(config.config.priorOtherCondition).isNull()
    }

    @Test
    fun `Should return NonOncologicalHistoryConfig with prior other condition from valid inputs is not lvef`() {
        val config = NonOncologicalHistoryConfigFactory(icdModel).create(
            fields,
            arrayOf("input", "name", "2023", "12", "category", "$icdTitle&$icdExtension", "0", "", "1")
        )
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.ignore).isEqualTo(false)
        assertThat(config.config.lvef).isNull()
        val priorOtherCondition = config.config.priorOtherCondition!!
        assertThat(priorOtherCondition.icdCode.mainCode).isEqualTo(icdMainCode)
        assertThat(priorOtherCondition.icdCode.extensionCode).isEqualTo(icdExtensionCode)
        assertThat(priorOtherCondition.name).isEqualTo("name")
        assertThat(priorOtherCondition.year).isEqualTo(2023)
        assertThat(priorOtherCondition.month).isEqualTo(12)
        assertThat(priorOtherCondition.isContraindicationForTherapy).isEqualTo(true)
    }

    @Test
    fun `Should return validation error when year and month are not numbers`() {
        val config = NonOncologicalHistoryConfigFactory(icdModel).create(
            fields,
            arrayOf("input", "name", "year", "month", "category", icdTitle, "0", "", "1")
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
        val config = NonOncologicalHistoryConfigFactory(icdModel).create(
            fields,
            arrayOf("input", "name", "2023", "12", "category", icdTitle, "1", "invalid", "1")
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
        val config = NonOncologicalHistoryConfigFactory(icdModel).create(
            fields,
            arrayOf("input", "name", "2023", "12", "category", icdTitle, "string", "1.0", "no")
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
    fun `Should return validation error when impossible to solve ICD code for title`() {
        val config = NonOncologicalHistoryConfigFactory(icdModel).create(
            fields,
            arrayOf("input", "name", "2023", "12", "category", "unknown title", "0", "", "1")
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
        val config = NonOncologicalHistoryConfigFactory(icdModel).create(
            fields,
            arrayOf("input", "name", "2023", "12", "category", "$icdTitle&unknownExtension", "0", "", "1")
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
}