package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.datamodel.clinical.Ecg
import com.hartwig.actin.datamodel.clinical.EcgMeasure
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
import com.hartwig.actin.icd.TestIcdFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val INPUT = "input"
private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.ECG_TSV)

class ECGConfigFactoryTest {
    private val icdModel = TestIcdFactory.createTestModel()
    private val factory = EcgConfigFactory(icdModel)

    @Test
    fun `Should return ECG config from valid inputs`() {
        val icdMainCode = icdModel.codeToNodeMap.keys.first()
        val icdCodesCheck = setOf(IcdCode(icdMainCode, null))
        val config = createConfig(
            interpretation = "interpretation",
            icd = icdModel.titleToCodeMap.keys.first(),
            isQTCF = "1",
            qtcfValue = "1",
            qtcfUnit = "ms"
        )
        assertThat(config.errors).isEmpty()
        assertThat(config.config.input).isEqualTo(INPUT)
        assertThat(config.config.curated).isNotNull
        val curated = config.config.curated as Ecg
        with(curated) {
            assertThat(name).isEqualTo("interpretation")
            assertThat(qtcfMeasure).isEqualTo(EcgMeasure(1, "ms"))
            assertThat(jtcMeasure).isNull()
            assertThat(icdCodes).isEqualTo(icdCodesCheck)
        }
    }

    @Test
    fun `Should return ignore config when input evaluates to false`() {
        val config = createConfig(input = "nvt", interpretation = "<ignore>")
        assertThat(config.errors).isEmpty()
        assertThat(config.config).isEqualTo(ComorbidityConfig("nvt", ignore = true, curated = null))
    }

    @Test
    fun `Should return ignore config when input evaluates to null`() {
        val config = createConfig(input = "possible", interpretation = "<ignore>")
        assertThat(config.errors).isEmpty()
        assertThat(config.config).isEqualTo(ComorbidityConfig("possible", ignore = true, curated = null))
    }

    @Test
    fun `Should return empty config when input evaluates to true`() {
        val config = createConfig(input = "Ja", interpretation = "")
        assertThat(config.errors).isEmpty()
        assertThat(config.config).isEqualTo(ComorbidityConfig("Ja", ignore = false, curated = Ecg(null, null, null)))
    }

    @Test
    fun `Should return validation error when QTCF is not a number`() {
        val config = createConfig(isQTCF = "1", qtcfValue = "invalid")
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.ECG,
                INPUT,
                "qtcfValue",
                "invalid",
                "integer"
            )
        )
    }

    @Test
    fun `Should return validation error when JTC is not a number`() {
        val config = createConfig(isJTC = "1", jtcValue = "invalid")
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.ECG,
                INPUT,
                "jtcValue",
                "invalid",
                "integer"
            )
        )
    }

    @Test
    fun `Should return validation error when ICD code is invalid`() {
        val config = createConfig(icd = "invalid")
        assertThat(config.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.ECG,
                INPUT,
                "icd",
                "invalid",
                "icd",
                "ICD title \"invalid\" is not known - check for existence in ICD model"
            )
        )
    }

    private fun createConfig(
        input: String = INPUT,
        interpretation: String = "",
        icd: String = "",
        isJTC: String = "0",
        isQTCF: String = "0",
        qtcfValue: String = "",
        qtcfUnit: String = "",
        jtcValue: String = "",
        jtcUnit: String = ""
    ): ValidatedCurationConfig<ComorbidityConfig> {
        return factory.create(
            fields, arrayOf(input, interpretation, icd, isQTCF, isJTC, qtcfValue, qtcfUnit, jtcValue, jtcUnit)
        )
    }
}