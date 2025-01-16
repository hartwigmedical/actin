package com.hartwig.actin.clinical.feed.standard.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.ComorbidityConfig
import com.hartwig.actin.clinical.curation.extraction.CurationExtractionEvaluation
import com.hartwig.actin.clinical.feed.standard.ProvidedPatientRecord
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance

class StandardIntolerancesExtractor(
    private val comorbidityCuration: CurationDatabase<ComorbidityConfig>
) :
    StandardDataExtractor<List<Intolerance>> {
    override fun extract(ehrPatientRecord: ProvidedPatientRecord): ExtractionResult<List<Intolerance>> {
        val patientId = ehrPatientRecord.patientDetails.hashedId

        return ehrPatientRecord.allergies.map { providedIntolerance ->
            val input = providedIntolerance.name
            val curationResponse = CurationResponse.createFromConfigs(
                comorbidityCuration.find(input),
                patientId,
                CurationCategory.INTOLERANCE,
                input,
                "intolerance",
                requireUniqueness = true
            )
            val intoleranceCuration = curationResponse.config()?.curated
            val intolerance = with(providedIntolerance) {
                Intolerance(
                    intoleranceCuration?.name ?: name,
                    intoleranceCuration?.icdCodes ?: setOf(IcdCode("", null)),
                    clinicalStatus = clinicalStatus,
                    verificationStatus = verificationStatus,
                    criticality = severity
                )
            }
            ExtractionResult(listOf(intolerance), curationResponse.extractionEvaluation)
        }.fold(
            ExtractionResult(emptyList(), CurationExtractionEvaluation())
        ) { (intolerances, aggregatedEval), (intolerance, eval) ->
            ExtractionResult(intolerances + intolerance, aggregatedEval + eval)
        }
    }
}