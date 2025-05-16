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
    fun `Should return MolecularTestConfig from valid inputs`() {
        val config = IhcTestConfigFactory(CurationCategory.MOLECULAR_TEST_IHC).create(
            fields,
            arrayOf("input", "test", "item", "measure", "scoreText", "scoreValuePrefix", "1.0", "scoreValueUnit", "1")
        )
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.ignore).isEqualTo(false)
        val curated = config.config.curated!!
        assertThat(curated.item).isEqualTo("item")
        assertThat(curated.measure).isEqualTo("measure")
        assertThat(curated.scoreValuePrefix).isEqualTo("scoreValuePrefix")
        assertThat(curated.scoreValue).isEqualTo(1.0)
        assertThat(curated.scoreValueUnit).isEqualTo("scoreValueUnit")
        assertThat(curated.impliesPotentialIndeterminateStatus).isEqualTo(true)
    }

    @Test
    fun `Should set curated test to null when ignore is set`() {
        val config: ValidatedCurationConfig<IhcTestConfig> = IhcTestConfigFactory(CurationCategory.MOLECULAR_TEST_IHC).create(
            fields,
            arrayOf("input", "<ignore>", "item", "measure", "scoreText", "scoreValuePrefix", "1.0", "scoreValueUnit", "1")
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
            arrayOf("input", "test", "item", "measure", "scoreText", "scoreValuePrefix", "1.0", "scoreValueUnit", "invalid")
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

}