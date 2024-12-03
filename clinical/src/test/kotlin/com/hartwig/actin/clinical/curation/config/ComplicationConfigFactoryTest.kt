package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ComplicationConfigFactoryTest {

    private val icdModel = TestIcdFactory.createTestModel()
    private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.COMPLICATION_TSV)
    private val icdCode = icdModel.codeToNodeMap.keys.first()
    private val icdTitle = icdModel.codeToNodeMap[icdCode]!!.title

    @Test
    fun `Should return complication config from valid data`() {
        val configFactory = ComplicationConfigFactory(icdModel)
        val data = arrayOf("input", "1", "name", "categories", icdTitle, "2023", "12")
        val config = configFactory.create(fields, data)
        println(config.config.curated)

        val errors = config.errors
        val configObj = config.config
        val curated = configObj.curated
        val curatedCategories = curated!!.categories
        val curatedIcd = curated.icdCode

        assertThat(errors).isEmpty()

        assertThat(configObj.input).isEqualTo("input")
        assertThat(configObj.ignore).isEqualTo(false)
        assertThat(configObj.impliesUnknownComplicationState).isTrue

        assertThat(curated.name).isEqualTo("name")
        assertThat(curatedCategories).containsExactly("categories")
        assertThat(curatedIcd).isEqualTo(icdCode)
        assertThat(curated.year).isEqualTo(2023)
        assertThat(curated.month).isEqualTo(12)
    }

    @Test
    fun `Should return validation error when year is not a number`() {
        assertThat(
            ComplicationConfigFactory(icdModel).create(
                fields, arrayOf("input", "1", "name", "categories", icdTitle, "year", "12")
            ).errors
        ).containsExactly(
            CurationConfigValidationError("Complication", "input", "year", "year", "integer")
        )
    }

    @Test
    fun `Should return validation error when month is not a number`() {
        assertThat(
            ComplicationConfigFactory(icdModel).create(
                fields, arrayOf("input", "1", "name", "categories", icdTitle, "2023", "month")
            ).errors
        ).containsExactly(
            CurationConfigValidationError("Complication", "input", "month", "month", "integer")
        )
    }

    @Test
    fun `Should return validation error when impliesUnknownComplicationState is not boolean`() {
        assertThat(
            ComplicationConfigFactory(icdModel).create(
                fields, arrayOf("input", "A", "name", "categories", icdTitle, "2023", "12")
            ).errors
        ).containsExactly(
            CurationConfigValidationError("Complication", "input", "impliesUnknownComplicationState", "A", "boolean")
        )
    }

    @Test
    fun `Should return validation error when impossible to solve icd code for title`() {
        assertThat(
            ComplicationConfigFactory(icdModel).create(
                fields, arrayOf("input", "1", "name", "categories", "unknown title", "2023", "12")
            ).errors
        ).containsExactly(
            CurationConfigValidationError(
                "Complication",
                "input",
                "icd",
                "unknown title",
                "string",
                "ICD title \"unknown title\" is not known - check for existence in resource"
            )
        )
    }
}