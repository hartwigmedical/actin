package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.datamodel.clinical.*
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
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
    private val configFactory = ComorbidityConfigFactory(icdModel)


    @Test
    fun `Should return null curation and validation error when type is not recognized`() {
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
                    year = null,
                    month = null
                )
            )
        )
    }

    @Test
    fun `Should return infection config from valid data`() {
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

    @Test
    fun `Should return OtherCondition creation`() {
        val data = arrayOf("other_condition", "input", "1", "name", "$icdMainTitle&$icdExtensionTitle", "", "", "", "", "", "interpretation")
        val config = configFactory.create(fields, data)

        assertThat(config.errors).isEmpty()
        assertThat(config.config).isEqualTo(
            ComorbidityConfig(
                input = "input",
                ignore = false,
                curated = OtherCondition(
                    name = "name",
                    icdCodes = icdCodes
                )
            )
        )

    }

    @Test
    fun `test ToxicityCuration creation`() {
        val data = arrayOf("toxicity", "input", "1", "name", "$icdMainTitle&$icdExtensionTitle", "2023", "12", "1", "")
        val config = configFactory.create(fields, data)

        assertThat(config.errors).isEmpty()
        assertThat(config.config).isEqualTo(
            ComorbidityConfig(
                input = "input",
                ignore = false,
                curated = ToxicityCuration(
                    name = "name",
                    icdCodes = icdCodes,
                    grade = 1
                )
            )
        )
    }

        @Test
    fun `Should return integer validation error`() {
        val data = arrayOf("toxicity", "input", "1", "name", "$icdMainTitle&$icdExtensionTitle", "2023", "12", "0.5", "")
        val config = configFactory.create(fields, data)

        assertThat(config.errors).isEqualTo(
            listOf(
                CurationConfigValidationError(
                    CurationCategory.COMORBIDITY,
                    "input",
                    "grade",
                    "0.5",
                    "integer",
                )
            )
        )
        assertThat(config.config).isEqualTo(
            ComorbidityConfig(
                input = "input",
                ignore = false,
                curated = ToxicityCuration(
                    name = "name",
                    icdCodes = icdCodes,
                    grade = null
                )
            )
        )
    }

    @Test
    fun `test Ecg creation`() {
        val data = arrayOf("ecg", "input", "1", "name", "$icdMainTitle&$icdExtensionTitle", "2023", "12", "0.5", "1", "1", "ecg", "1", "1", "10", "ms", "20", "ms")
        val config = configFactory.create(fields, data)

        assertThat(config.errors).isEmpty()
        assertThat(config.config).isEqualTo(
            ComorbidityConfig(
                input = "input",
                ignore = false,
                lvef = 1.0,
                curated = Ecg(
                    name = "ecg",
                    icdCodes = icdCodes,
                    qtcfMeasure = EcgMeasure(value = 10, unit = "ms"),
                    jtcMeasure = EcgMeasure(value = 20, unit = "ms")
                )
            )
        )
    }

    @Test
    fun `test Infection as OtherCondition creation`() {
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