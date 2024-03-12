package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.CypInteractionConfig
import com.hartwig.actin.clinical.datamodel.CypInteraction

object CypInteractionCurationUtil {
    fun curateMedicationCypInteractions(
        cypInteractionCuration: CurationDatabase<CypInteractionConfig>,
        medicationName: String
    ): List<CypInteraction> {
        return cypInteractionCuration.find(medicationName).flatMap(CypInteractionConfig::interactions)
    }
}