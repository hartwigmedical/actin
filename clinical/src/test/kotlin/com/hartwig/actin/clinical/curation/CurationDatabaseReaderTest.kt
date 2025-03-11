package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfig
import com.hartwig.actin.clinical.curation.config.PrimaryTumorConfigFactory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class CurationDatabaseReaderTest {
    private val curationDoidValidator = mockk<CurationDoidValidator>()

    @Before
    fun setUp() {
        every { curationDoidValidator.isValidCancerDoidSet(any()) } returns true
    }

    @Test
    fun `Should read a curation database from TSV file`() {
        val input = "Carcinoma | Unknown"
        assertThat(
            CurationDatabaseReader.read(
                CURATION_DIRECTORY,
                CurationDatabaseReader.PRIMARY_TUMOR_TSV,
                PrimaryTumorConfigFactory(curationDoidValidator),
                CurationCategory.PRIMARY_TUMOR
            ) { emptySet() }.find(input)
        ).containsOnly(
            PrimaryTumorConfig(
                primaryTumorLocation = "Unknown",
                primaryTumorType = "Carcinoma",
                primaryTumorSubLocation = "CUP",
                primaryTumorExtraDetails = "",
                primaryTumorSubType = "",
                doids = setOf("299"),
                input = input
            )
        )
    }


}