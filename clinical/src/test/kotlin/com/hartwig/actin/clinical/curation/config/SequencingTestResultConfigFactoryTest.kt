package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDatabaseReader
import com.hartwig.actin.clinical.curation.TestCurationFactory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
import com.hartwig.actin.datamodel.molecular.driver.VirusType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SequencingTestResultConfigFactoryTest {
    private val fields: Map<String, Int> = TestCurationFactory.curationHeaders(CurationDatabaseReader.SEQUENCING_TEST_RESULT_TSV)
    private val array = Array(fields.size) { "" }

    @Test
    fun `Should return correct config in case of valid inputs`() {
        array[0] = "input"
        array[19] = "Ebv"
        val resultConfig = SequencingTestResultConfigFactory().create(fields, array)
        assertThat(resultConfig.errors).isEmpty()
        assertThat(resultConfig.config.input).isEqualTo("input")
        assertThat(resultConfig.config.virus).isEqualTo(VirusType.EBV)
    }

    @Test
    fun `Should return warning in case of non-allowed virus input`() {
        array[0] = "input"
        array[19] = "OTHER"
        val resultConfig = SequencingTestResultConfigFactory().create(fields, array)
        assertThat(resultConfig.errors).containsExactly(
            CurationConfigValidationError(
                CurationCategory.SEQUENCING_TEST_RESULT,
                "input",
                "virus",
                "OTHER",
                "VirusType",
                "Accepted values are [EBV, HBV, HHV8, HPV, MCV, OTHER] excluding values [OTHER]"
            )
        )
    }
}