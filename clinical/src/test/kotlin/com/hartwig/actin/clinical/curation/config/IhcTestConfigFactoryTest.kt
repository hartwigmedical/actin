package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IhcTestConfigFactoryTest {

    private val fields: Map<String, Int> =
        TestCurationFactory.curationHeaders(CurationDatabaseReader.MOLECULAR_TEST_IHC_TSV)

    @Test
    fun `Should return IhcTestConfig from valid inputs`() {
        val config = IhcTestConfigFactory(CurationCategory.MOLECULAR_TEST_IHC).create(
            fields,
            arrayOf("input", "item", "measure", "scoreText", "scoreValuePrefix", "1.0", "scoreValueUnit", "1")
        )
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.ignore).isEqualTo(false)
        with(config.config.curated!!) {
            assertThat(item).isEqualTo("item")
            assertThat(measure).isEqualTo("measure")
            assertThat(scoreValuePrefix).isEqualTo("scoreValuePrefix")
            assertThat(scoreValue).isEqualTo(1.0)
            assertThat(scoreValueUnit).isEqualTo("scoreValueUnit")
            assertThat(impliesPotentialIndeterminateStatus).isEqualTo(true)
        }
    }

    @Test
    fun `Should set curated test to null when ignore is set`() {
        val config: ValidatedCurationConfig<IhcTestConfig> = IhcTestConfigFactory(CurationCategory.MOLECULAR_TEST_IHC).create(
            fields,
            arrayOf("input", "<ignore>", "measure", "scoreText", "scoreValuePrefix", "1.0", "scoreValueUnit", "1")
        )
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.ignore).isEqualTo(true)
        assertThat(config.config.curated).isNull()
    }

    @Test
    fun `Should return validation error and set curated test to null when invalid impliesPotentialIndeterminateStatus`() {
        val config: ValidatedCurationConfig<IhcTestConfig> = IhcTestConfigFactory(CurationCategory.MOLECULAR_TEST_IHC).create(
            fields,
            arrayOf("input", "item", "measure", "scoreText", "scoreValuePrefix", "1.0", "scoreValueUnit", "invalid")
        )
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.MOLECULAR_TEST_IHC,
                "input",
                "impliesPotentialIndeterminateStatus",
                "invalid",
                "boolean"
            )
        )
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.ignore).isEqualTo(false)
        assertThat(config.config.curated).isNull()
    }

    @Test
    fun `Should set impliesPotentialIndeterminateStatus to false when not provided and return IhcTestConfig`() {
        val config = IhcTestConfigFactory(CurationCategory.MOLECULAR_TEST_IHC).create(
            fields,
            arrayOf("input", "item", "measure", "scoreText", "scoreValuePrefix", "1.0", "scoreValueUnit", "")
        )
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.ignore).isEqualTo(false)
        with(config.config.curated!!) {
            assertThat(item).isEqualTo("item")
            assertThat(measure).isEqualTo("measure")
            assertThat(scoreValuePrefix).isEqualTo("scoreValuePrefix")
            assertThat(scoreValue).isEqualTo(1.0)
            assertThat(scoreValueUnit).isEqualTo("scoreValueUnit")
            assertThat(impliesPotentialIndeterminateStatus).isEqualTo(false)
        }
    }
}