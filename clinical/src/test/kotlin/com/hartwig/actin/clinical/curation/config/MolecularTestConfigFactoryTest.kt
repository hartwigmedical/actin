package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CURATION_DIRECTORY
import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MolecularTestConfigFactoryTest {

    private val fields: Map<String, Int> =
        CurationConfigFile.readTsv(CURATION_DIRECTORY + CurationDatabaseReader.MOLECULAR_TEST_TSV).second

    @Test
    fun `Should return MolecularTestConfig from valid inputs`() {
        val config = MolecularTestConfigFactory().create(
            fields,
            arrayOf("input", "test", "item", "measure", "scoreText", "scoreValuePrefix", "1.0", "scoreValueUnit", "1")
        )
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.ignore).isEqualTo(false)
        val curated = config.config.curated!!
        assertThat(curated.test()).isEqualTo("test")
        assertThat(curated.item()).isEqualTo("item")
        assertThat(curated.measure()).isEqualTo("measure")
        assertThat(curated.scoreValuePrefix()).isEqualTo("scoreValuePrefix")
        assertThat(curated.scoreValue()).isEqualTo(1.0)
        assertThat(curated.scoreValueUnit()).isEqualTo("scoreValueUnit")
        assertThat(curated.impliesPotentialIndeterminateStatus()).isEqualTo(true)
    }

    @Test
    fun `Should set curated test to null when ignore is set`() {
        val config: ValidatedCurationConfig<MolecularTestConfig> = MolecularTestConfigFactory().create(
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
        val config: ValidatedCurationConfig<MolecularTestConfig> = MolecularTestConfigFactory().create(
            fields,
            arrayOf("input", "test", "item", "measure", "scoreText", "scoreValuePrefix", "1.0", "scoreValueUnit", "invalid")
        )
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                "impliesPotentialIndeterminateStatus was configured with an invalid value of 'invalid' for input 'input'"
            )
        )
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.ignore).isEqualTo(false)
        assertThat(config.config.curated).isNull()
    }

}