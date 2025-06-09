package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.config.IhcTestConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.feed.datamodel.DatedEntry

class IhcTestsExtractor(
    private val molecularTestIhcCuration: CurationDatabase<IhcTestConfig>
) {

    fun extract(patientId: String, entries: List<DatedEntry>): ExtractionResult<List<IhcTest>> {
        if (entries.isEmpty()) {
            return ExtractionResult(emptyList(), CurationExtractionEvaluation())
        }

        val curation = entries.map {
            val input = CurationUtil.fullTrim(it.name)
            CurationResponse.createFromConfigs(
                molecularTestIhcCuration.find(input),
                patientId,
                CurationCategory.MOLECULAR_TEST_IHC,
                input,
                CurationCategory.MOLECULAR_TEST_IHC.categoryName
            )
        }
            .fold(CurationResponse<IhcTestConfig>()) { acc, cur -> acc + cur }

        return ExtractionResult(curation.configs.filterNot(IhcTestConfig::ignore).map { it.curated!! }, curation.extractionEvaluation)
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            IhcTestsExtractor(curationDatabaseContext.molecularTestIhcCuration)
    }
}