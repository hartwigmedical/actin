package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.CurationDatabaseContext
import com.hartwig.actin.clinical.curation.CurationResponse
import com.hartwig.actin.clinical.curation.config.ECGConfig
import com.hartwig.actin.clinical.curation.config.InfectionConfig
import com.hartwig.actin.clinical.curation.config.NonOncologicalHistoryConfig
import com.hartwig.actin.clinical.datamodel.ClinicalStatus
import com.hartwig.actin.clinical.datamodel.ECG
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus
import com.hartwig.actin.clinical.datamodel.ImmutableECG
import com.hartwig.actin.clinical.datamodel.ImmutableECGMeasure
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus
import com.hartwig.actin.clinical.datamodel.InfectionStatus
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire

class ClinicalStatusExtractor(
    private val ecgCuration: CurationDatabase<ECGConfig>,
    private val infectionCuration: CurationDatabase<InfectionConfig>,
    private val nonOncologicalHistoryCuration: CurationDatabase<NonOncologicalHistoryConfig>
) {

    fun extract(patientId: String, questionnaire: Questionnaire?, hasComplications: Boolean?): ExtractionResult<ClinicalStatus> {
        if (questionnaire == null) {
            return ExtractionResult(ImmutableClinicalStatus.builder().build(), ExtractionEvaluation())
        }
        val ecgCuration = curateECG(patientId, questionnaire.ecg)
        val infectionCuration = curateInfection(patientId, questionnaire.infectionStatus)

        val clinicalStatus = ImmutableClinicalStatus.builder()
            .who(questionnaire.whoStatus)
            .infectionStatus(infectionCuration.extracted)
            .ecg(ecgCuration.extracted)
            .lvef(determineLVEF(questionnaire.nonOncologicalHistory))
            .hasComplications(hasComplications)
            .build()

        return ExtractionResult(clinicalStatus, ecgCuration.evaluation + infectionCuration.evaluation)
    }

    private fun curateECG(patientId: String, rawECG: ECG?): ExtractionResult<ECG?> {
        val curationResponse = rawECG?.aberrationDescription()?.let {
            CurationResponse.createFromConfigs(
                ecgCuration.find(it), patientId, CurationCategory.ECG, it, "ECG", true
            )
        }
        val ecg = when (curationResponse?.configs?.size) {
            0 -> {
                rawECG
            }

            1 -> {
                val config = curationResponse.configs.first()
                if (config.ignore) null else {
                    return ExtractionResult(
                        ImmutableECG.builder()
                            .from(rawECG)
                            .aberrationDescription(config.interpretation.ifEmpty { null })
                            .qtcfMeasure(maybeECGMeasure(config.qtcfValue, config.qtcfUnit))
                            .jtcMeasure(maybeECGMeasure(config.jtcValue, config.jtcUnit))
                            .build(),
                        curationResponse.extractionEvaluation
                    )
                }
            }

            else -> {
                null
            }
        }
        return ExtractionResult(ecg, curationResponse?.extractionEvaluation ?: ExtractionEvaluation())
    }

    private fun curateInfection(patientId: String, rawInfectionStatus: InfectionStatus?): ExtractionResult<InfectionStatus?> {
        val curationResponse = rawInfectionStatus?.description()?.let {
            CurationResponse.createFromConfigs(
                infectionCuration.find(it), patientId, CurationCategory.INFECTION, it, "infection", true
            )
        }
        val infectionStatus = when (curationResponse?.configs?.size) {
            0 -> {
                rawInfectionStatus
            }

            1 -> {
                val config = curationResponse.configs.first()
                if (config.ignore) null else {
                    ImmutableInfectionStatus.builder()
                        .from(rawInfectionStatus)
                        .description(config.interpretation.ifEmpty { null })
                        .build()
                }
            }

            else -> {
                null
            }
        }
        return ExtractionResult(infectionStatus, curationResponse?.extractionEvaluation ?: ExtractionEvaluation())
    }

    private fun maybeECGMeasure(value: Int?, unit: String?): ImmutableECGMeasure? {
        return if (value == null || unit == null) {
            null
        } else ImmutableECGMeasure.builder().value(value).unit(unit).build()
    }

    private fun determineLVEF(nonOncologicalHistoryEntries: List<String>?): Double? {
        // We do not raise warnings or propagate evaluated inputs here since we use the same configs for priorOtherConditions
        return nonOncologicalHistoryEntries?.asSequence()
            ?.flatMap { nonOncologicalHistoryCuration.find(it) }
            ?.filterNot { it.ignore }
            ?.map { it.lvef }
            ?.find { it != null }
    }

    companion object {
        fun create(curationDatabaseContext: CurationDatabaseContext) =
            ClinicalStatusExtractor(
                ecgCuration = curationDatabaseContext.ecgCuration,
                infectionCuration = curationDatabaseContext.infectionCuration,
                nonOncologicalHistoryCuration = curationDatabaseContext.nonOncologicalHistoryCuration
            )
    }
}