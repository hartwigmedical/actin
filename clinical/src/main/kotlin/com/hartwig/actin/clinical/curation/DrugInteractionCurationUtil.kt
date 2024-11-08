package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.DrugInteractionConfig
import com.hartwig.actin.datamodel.clinical.DrugInteraction

object DrugInteractionCurationUtil {
    fun curateMedicationCypInteractions(
        cypInteractionCuration: CurationDatabase<DrugInteractionConfig>,
        medicationName: String
    ): List<DrugInteraction> {
        return cypInteractionCuration.find(medicationName).flatMap(DrugInteractionConfig::cypInteractions)
    }

    fun curateMedicationTransporterInteractions(
        cypInteractionCuration: CurationDatabase<DrugInteractionConfig>,
        medicationName: String
    ): List<DrugInteraction> {
        return cypInteractionCuration.find(medicationName).flatMap(DrugInteractionConfig::transporterInteractions)
    }
}