package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.datamodel.ImmutableIntolerance
import com.hartwig.actin.clinical.datamodel.Intolerance
import com.hartwig.actin.clinical.feed.intolerance.IntoleranceEntry

class IntoleranceExtractor(private val curation: CurationDatabase, private val atcModel: AtcModel) {

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
                    curation.findIntoleranceConfigs(it.name()), patientId, CurationCategory.INTOLERANCE, it.name(), "intolerance", true
                )
                val builder = ImmutableIntolerance.builder().from(it)
                curationResponse.config()?.let { config ->
                    builder.name(config.name).doids(config.doids)
                    if (it.category().equals("medication", ignoreCase = true)) {
                        builder.subcategories(atcModel.resolveByName(config.name.lowercase()))
                    }
                }

                ExtractionResult(listOf(builder.build()), curationResponse.extractionEvaluation)
            }
            .fold(ExtractionResult(emptyList(), ExtractionEvaluation())) { (intolerances, aggregatedEval), (intolerance, eval) ->
                ExtractionResult(intolerances + intolerance, aggregatedEval + eval)
            }
    }
}