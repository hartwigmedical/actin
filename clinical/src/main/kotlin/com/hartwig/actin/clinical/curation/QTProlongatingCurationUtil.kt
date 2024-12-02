package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.QTProlongatingConfig
import com.hartwig.actin.datamodel.clinical.QTProlongatingRisk

object QTProlongatingCurationUtil {

    fun annotateWithQTProlongating(
        qtProlongatingCuration: CurationDatabase<QTProlongatingConfig>,
        medicationName: String
    ): QTProlongatingRisk {
        val riskConfigs = qtProlongatingCuration.find(medicationName)
        return if (riskConfigs.isEmpty()) {
            QTProlongatingRisk.NONE
        } else if (riskConfigs.size > 1) {
            throw IllegalStateException(
                "Multiple risk configurations found for one medication name [$medicationName]. " +
                        "Check the qt_prolongating.tsv for a duplicate"
            )
        } else {
            return riskConfigs.first().status
        }
    }
}