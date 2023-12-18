package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.TestCurationFactory
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val DOID = "123"

class IntoleranceConfigFactoryTest {
    private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.INTOLERANCE_TSV)

    private val curationDoidValidator = mockk<CurationDoidValidator>()
    private val victim = IntoleranceConfigFactory(curationDoidValidator)

    @Test
    fun `Should return IntoleranceConfig from valid inputs`() {
        every {
            curationDoidValidator.isValidDiseaseDoidSet(
                setOf(DOID)
            )
        } returns true
        val config = victim.create(fields, arrayOf("input", "name", DOID))
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo("input")
        assertThat(config.config.ignore).isFalse()
        assertThat(config.config.name).isEqualTo("name")
        assertThat(config.config.doids).containsExactly(DOID)
    }

    @Test
    fun `Should return validation error when doids are invalid`() {
        val doidValidator: CurationDoidValidator = curationDoidValidator
        every {
            doidValidator.isValidDiseaseDoidSet(
                setOf(DOID)
            )
        } returns false
        val config: ValidatedCurationConfig<IntoleranceConfig> =
            IntoleranceConfigFactory(doidValidator).create(fields, arrayOf("input", "name", DOID))
        assertThat(config.errors).containsExactly(CurationConfigValidationError("Intolerance config with input 'input' contains at least one invalid doid: '[123]'"))
    }
}