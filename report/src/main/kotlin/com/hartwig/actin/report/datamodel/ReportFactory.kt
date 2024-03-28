package com.hartwig.actin.report.datamodel

import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import org.apache.logging.log4j.LogManager

object ReportFactory {
    private val LOGGER = LogManager.getLogger(ReportFactory::class.java)

    fun fromInputs(
        clinical: ClinicalRecord, molecular: MolecularRecord?, treatmentMatch: TreatmentMatch, config: ReportConfiguration
    ): Report {
        if (clinical.patientId != treatmentMatch.patientId) {
            LOGGER.warn(
                "Clinical patientId '{}' not the same as treatment match patientId '{}'! Using clinical patientId",
                clinical.patientId,
                treatmentMatch.patientId
            )
        }
        return Report(
            patientId = clinical.patientId,
            clinical = clinical,
            molecular = molecular,
            treatmentMatch = treatmentMatch,
            config = config
        )
    }
}