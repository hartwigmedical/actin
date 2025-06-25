package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.datamodel.clinical.Complication
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.OtherCondition
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class ComorbidityConfigFactoryTest {
    private val icdModel = TestIcdFactory.createTestModel()
    private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.COMORBIDITY)
    private val icdMainCode = icdModel.codeToNodeMap.keys.first()
    private val icdExtensionCode = icdModel.codeToNodeMap.keys.last()
    private val icdCodes = setOf(IcdCode(icdMainCode, icdExtensionCode))
    private val icdMainTitle = icdModel.codeToNodeMap[icdMainCode]!!.title
    private val icdExtensionTitle = icdModel.codeToNodeMap[icdExtensionCode]!!.title


    @Test
    fun `Should return null curation and validation error when type is not recognized`() {
        val configFactory = ComorbidityConfigFactory(icdModel)
        val data = arrayOf("no type", "input", "1", "unknown_type", icdMainTitle, "2023", "12", "0.5", "")
        val config = configFactory.create(fields, data)

        assertThat(config.errors).hasSize(1)
        assertThat(config.config).isEqualTo(
            ComorbidityConfig(
                input = "input",
                ignore = false,
                curated = null
            )
        )
    }

    @Test
    fun `Should return complication config from valid data`() {
        val configFactory = ComorbidityConfigFactory(icdModel)
        val data = arrayOf("complication", "input", "1", "name", "$icdMainTitle&$icdExtensionTitle", "2023", "12", "0.5", "")
        val config = configFactory.create(fields, data)

        assertThat(config.errors).isEmpty()
        assertThat(config.config).isEqualTo(
            ComorbidityConfig(
                input = "input",
                ignore = false,
                curated = Complication(
                    name = "name",
                    icdCodes = icdCodes,
                    year = 2023,
                    month = 12
                )
            )
        )
    }

    @Test
    fun `Should return intolerance config from valid data`() {
        val configFactory = ComorbidityConfigFactory(icdModel)
        val data = arrayOf("intolerance", "input", "1", "name", "$icdMainTitle&$icdExtensionTitle", "2020", "11", "", "")
        val config = configFactory.create(fields, data)

        assertThat(config.errors).isEmpty()
        assertThat(config.config).isEqualTo(
            ComorbidityConfig(
                input = "input",
                ignore = false,
                curated = Intolerance(
                    name = "name",
                    icdCodes = icdCodes,
                    year = 2020,
                    month = 11
                )
            )
        )
    }

    @Test
    fun `Should return infection config from valid data`() {
        val configFactory = ComorbidityConfigFactory(icdModel)
        val data = arrayOf("infection", "input", "1", "name", "$icdMainTitle&$icdExtensionTitle", "", "", "", "", "", "interpretation")
        val config = configFactory.create(fields, data)

        assertThat(config.errors).isEmpty()
        assertThat(config.config).isEqualTo(
            ComorbidityConfig(
                input = "input",
                ignore = false,
                curated = OtherCondition(
                    name = "interpretation",
                    icdCodes = icdCodes
                )
            )
        )
    }
}