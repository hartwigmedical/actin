package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationService
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry

class IntoleranceExtractor(private val intoleranceCuration: CurationDatabase<IntoleranceConfig>) {

    fun extract(patientId: String, intoleranceEntries: List<IntoleranceEntry>): ExtractionResult<List<Intolerance>> {
        return intoleranceEntries.map { entry: IntoleranceEntry ->
            ImmutableIntolerance.builder()
                .name(CurationUtil.capitalizeFirstLetterOnly(entry.codeText))
                .category(CurationUtil.capitalizeFirstLetterOnly(entry.category))
                .type(CurationUtil.capitalizeFirstLetterOnly(entry.isSideEffect))
                .clinicalStatus(CurationUtil.capitalizeFirstLetterOnly(entry.clinicalStatus))
                .verificationStatus(CurationUtil.capitalizeFirstLetterOnly(entry.verificationStatus))
                .criticality(CurationUtil.capitalizeFirstLetterOnly(entry.criticality))
                .build()
        }
            .map {
                val curationResponse = CurationResponse.createFromConfigs(
                    intoleranceCuration.curate(it.name()),
                    patientId,
                    CurationCategory.INTOLERANCE,
                    it.name(),
                    "intolerance",
                    true
                )
                val builder = ImmutableIntolerance.builder().from(it)
                curationResponse.config()?.let { config ->
                    builder.name(config.name).doids(config.doids)
                }
                // TODO: add ATC code of medication to subcategories
                if (it.category().equals("medication", ignoreCase = true)) {
                    builder.subcategories(emptySet())
                }
                ExtractionResult(listOf(builder.build()), curationResponse.extractionEvaluation)
            }
            .fold(ExtractionResult(emptyList(), ExtractionEvaluation())) { (intolerances, aggregatedEval), (intolerance, eval) ->
                ExtractionResult(intolerances + intolerance, aggregatedEval + eval)
            }
    }

    companion object {
        fun create(curationService: CurationService) = IntoleranceExtractor(curationService.intoleranceCuration)
    }
}