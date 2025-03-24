package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class InfectionConfigFactoryTest {
    private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.INFECTION_TSV)

    @Test
    fun `Should return InfectionConfig from valid inputs`() {
        val (config, errors) = InfectionConfigFactory(TestIcdFactory.createTestModel()).create(
            fields,
            arrayOf("input", "interpretation", "node 1")
        )
        assertThat(errors).isEmpty()
        assertThat(config.input).isEqualTo("input")
        assertThat(config.ignore).isFalse()
        assertThat(config.curated).isNotNull
        assertThat(config.curated!!.name).isEqualTo("interpretation")
    }

    @Test
    fun `Should return validation error when ICD code is invalid`() {
        val config = InfectionConfigFactory(TestIcdFactory.createTestModel()).create(fields, arrayOf("input", "interpretation", "invalid"))
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.INFECTION,
                "input",
                "icd",
                "invalid",
                "icd",
                "ICD title \"invalid\" is not known - check for existence in ICD model"
            )
        )
    }
}