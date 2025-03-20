package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class OtherConditionConfigFactoryTest {
    private val fields: Map<String, Int> =
        TestCurationFactory.curationHeaders(CurationDatabaseReader.NON_ONCOLOGICAL_HISTORY_TSV)
    private val icdModel = TestIcdFactory.createTestModel()
    private val icdMainCode = icdModel.codeToNodeMap.keys.first()
    private val icdExtensionCode = icdModel.codeToNodeMap.keys.last()
    private val icdCodes = setOf(IcdCode(icdMainCode, icdExtensionCode))
    private val icdTitle = icdModel.codeToNodeMap[icdMainCode]!!.title
    private val icdExtension = icdModel.codeToNodeMap[icdExtensionCode]!!.title

    @Test
    fun `Should return NonOncologicalHistoryConfig with no other condition from valid inputs is lvef`() {
        val config = OtherConditionConfigFactory(icdModel).create(
            fields,
            arrayOf("input", "name", "2023", "12", icdTitle, "1", "1.0", "")
        )
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.ignore).isEqualTo(false)
        assertThat(config.config.lvef).isEqualTo(1.0)
        assertThat(config.config.curated).isNull()
    }

    @Test
    fun `Should return NonOncologicalHistoryConfig with other condition from valid inputs is not lvef`() {
        assertConfigCreation("name", "name")
    }

    @Test
    fun `Should return NonOncologicalHistoryConfig with other condition with null name from valid inputs with empty name curation`() {
        assertConfigCreation(" ", null)
    }

    private fun assertConfigCreation(curatedName: String, expectedName: String?) {
        val config = OtherConditionConfigFactory(icdModel).create(
            fields,
            arrayOf("input", curatedName, "2023", "12", "$icdTitle&$icdExtension", "0", "", "1")
        )
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.ignore).isEqualTo(false)
        assertThat(config.config.lvef).isNull()
        val otherCondition = config.config.curated!!
        assertThat(otherCondition.icdCodes).isEqualTo(icdCodes)
        assertThat(otherCondition.name).isEqualTo(expectedName)
        assertThat(otherCondition.year).isEqualTo(2023)
        assertThat(otherCondition.month).isEqualTo(12)
    }

    @Test
    fun `Should return validation error when year and month are not numbers`() {
        val config = OtherConditionConfigFactory(icdModel).create(
            fields,
            arrayOf("input", "name", "year", "month", icdTitle, "0", "", "1")
        )
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(CurationCategory.NON_ONCOLOGICAL_HISTORY, "input", "year", "year", "integer"),
            CurationConfigValidationError(CurationCategory.NON_ONCOLOGICAL_HISTORY, "input", "month", "month", "integer")
        )
    }

    @Test
    fun `Should return validation error when is lvef and lvef value is not a number`() {
        val config = OtherConditionConfigFactory(icdModel).create(
            fields,
            arrayOf("input", "name", "2023", "12", icdTitle, "1", "invalid", "1")
        )
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(CurationCategory.NON_ONCOLOGICAL_HISTORY, "input", "lvefValue", "invalid", "double")
        )
    }

    @Test
    fun `Should return validation error when impossible to solve ICD code for title`() {
        val config = OtherConditionConfigFactory(icdModel).create(
            fields,
            arrayOf("input", "name", "2023", "12", "unknown title", "0", "", "1")
        )
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.NON_ONCOLOGICAL_HISTORY,
                "input",
                "icd",
                "unknown title",
                "icd",
                "ICD title \"unknown title\" is not known - check for existence in ICD model"
            )
        )
    }

    @Test
    fun `Should return validation error when impossible to solve ICD code for extension of title`() {
        val config = OtherConditionConfigFactory(icdModel).create(
            fields,
            arrayOf("input", "name", "2023", "12", "$icdTitle&unknownExtension", "0", "", "1")
        )
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.NON_ONCOLOGICAL_HISTORY,
                "input",
                "icd",
                "$icdTitle&unknownExtension",
                "icd",
                "ICD title \"$icdTitle&unknownExtension\" is not known - check for existence in ICD model"
            )
        )
    }
}