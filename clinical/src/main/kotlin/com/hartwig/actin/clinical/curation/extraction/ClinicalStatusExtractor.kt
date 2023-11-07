package com.hartwig.actin.clinical.curation.extraction

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.ECGConfig
import com.hartwig.actin.clinical.curation.config.InfectionConfig
import com.hartwig.actin.clinical.datamodel.ClinicalStatus
import com.hartwig.actin.clinical.datamodel.ECG
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalStatus
import com.hartwig.actin.clinical.datamodel.ImmutableECG
import com.hartwig.actin.clinical.datamodel.ImmutableECGMeasure
import com.hartwig.actin.clinical.datamodel.ImmutableInfectionStatus
import com.hartwig.actin.clinical.datamodel.InfectionStatus
import com.hartwig.actin.clinical.feed.questionnaire.Questionnaire

class ClinicalStatusExtractor(private val curation: CurationDatabase) {

    fun extract(patientId: String, questionnaire: Questionnaire?, hasComplications: Boolean?): ExtractionResult<ClinicalStatus> {
        if (questionnaire == null) {
            return ExtractionResult(ImmutableClinicalStatus.builder().build(), ExtractionEvaluation())
        }
        val ecgCuration = questionnaire.ecg?.aberrationDescription()?.let {
            CurationResponse.createFromConfigs(
                curation.findECGConfig(it), patientId, CurationCategory.ECG, it, "ECG", true
            )
        }
        val infectionCuration = questionnaire.infectionStatus?.description()?.let {
            CurationResponse.createFromConfigs(
                curation.findInfectionStatusConfig(it), patientId, CurationCategory.INFECTION, it, "infection status", true
            )
        }

        val clinicalStatus = ImmutableClinicalStatus.builder()
            .who(questionnaire.whoStatus)
            .infectionStatus(extractInfection(questionnaire.infectionStatus, infectionCuration))
            .ecg(extractECG(questionnaire.ecg, ecgCuration))
            .lvef(determineLVEF(questionnaire.nonOncologicalHistory))
            .hasComplications(hasComplications)
            .build()

        return ExtractionResult(
            clinicalStatus,
            ExtractionEvaluation() + ecgCuration?.extractionEvaluation + infectionCuration?.extractionEvaluation
        )
    }

    private fun extractECG(rawECG: ECG?, ecgCuration: CurationResponse<ECGConfig>?): ECG? {
        when (ecgCuration?.configs?.size) {
            0 -> {
                return rawECG
            }

            1 -> {
                val config = ecgCuration.configs.first()
                if (!config.ignore) {
                    return ImmutableECG.builder()
                        .from(rawECG!!)
                        .aberrationDescription(config.interpretation.ifEmpty { null })
                        .qtcfMeasure(maybeECGMeasure(config.qtcfValue, config.qtcfUnit))
                        .jtcMeasure(maybeECGMeasure(config.jtcValue, config.jtcUnit))
                        .build()
                }
            }
        }
        return null
    }

    private fun extractInfection(
        rawInfectionStatus: InfectionStatus?,
        infectionCuration: CurationResponse<InfectionConfig>?
    ): InfectionStatus? {
        when (infectionCuration?.configs?.size) {
            0 -> {
                return rawInfectionStatus
            }

            1 -> {
                val config = infectionCuration.configs.first()
                if (!config.ignore) {
                    return ImmutableInfectionStatus.builder()
                        .from(rawInfectionStatus!!)
                        .description(config.interpretation.ifEmpty { null })
                        .build()
                }
            }
        }
        return null
    }

    private fun maybeECGMeasure(value: Int?, unit: String?): ImmutableECGMeasure? {
        return if (value == null || unit == null) {
            null
        } else ImmutableECGMeasure.builder().value(value).unit(unit).build()
    }

    fun determineLVEF(nonOncologicalHistoryEntries: List<String>?): Double? {
        // We do not raise warnings or propagate evaluated inputs here since we use the same configs for priorOtherConditions
        return nonOncologicalHistoryEntries?.asSequence()
            ?.flatMap { curation.findNonOncologicalHistoryConfigs(it) }
            ?.filterNot { it.ignore }
            ?.map { it.lvef }
            ?.find { it.isPresent }
            ?.get()
    }
}