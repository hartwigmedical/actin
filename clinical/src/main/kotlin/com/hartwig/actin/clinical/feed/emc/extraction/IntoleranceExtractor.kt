package com.hartwig.actin.clinical.feed.emc.extraction

import com.hartwig.actin.clinical.AtcModel
import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.config.IntoleranceConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.emc.intolerance.IntoleranceEntry
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance

class IntoleranceExtractor(private val intoleranceCuration: CurationDatabase<IntoleranceConfig>, private val atcModel: AtcModel) {

    fun extract(patientId: String, intoleranceEntries: List<IntoleranceEntry>): ExtractionResult<List<Intolerance>> {
        return intoleranceEntries.map { entry: IntoleranceEntry ->
            Intolerance(
                name = CurationUtil.capitalizeFirstLetterOnly(entry.codeText),
                icdCodes = setOf(IcdCode("", null)),
                category = CurationUtil.capitalizeFirstLetterOnly(entry.category),
                type = CurationUtil.capitalizeFirstLetterOnly(entry.isSideEffect),
                clinicalStatus = CurationUtil.capitalizeFirstLetterOnly(entry.clinicalStatus),
                verificationStatus = CurationUtil.capitalizeFirstLetterOnly(entry.verificationStatus),
                criticality = CurationUtil.capitalizeFirstLetterOnly(entry.criticality),
                subcategories = emptySet()
            )
        }
            .map {
                val curationResponse = CurationResponse.createFromConfigs(
                    intoleranceCuration.find(it.name), patientId, CurationCategory.INTOLERANCE, it.name, "intolerance", true
                )
                val curatedIntolerance = curationResponse.config()?.let { config ->
                    val subcategories = if (it.category.equals("medication", ignoreCase = true)) {
                        atcModel.resolveByName(config.name.lowercase())
                    } else emptySet()
                    it.copy(
                        name = config.name,
                        icdCodes = config.icd,
                        subcategories = subcategories
                    )
                } ?: it
                ExtractionResult(listOf(curatedIntolerance), curationResponse.extractionEvaluation)
            }
            .fold(ExtractionResult(emptyList(), CurationExtractionEvaluation())) { (intolerances, aggregatedEval), (intolerance, eval) ->
                ExtractionResult(intolerances + intolerance, aggregatedEval + eval)
            }
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext, atcModel: AtcModel) =
            IntoleranceExtractor(curationDatabaseContext.intoleranceCuration, atcModel)
    }
}