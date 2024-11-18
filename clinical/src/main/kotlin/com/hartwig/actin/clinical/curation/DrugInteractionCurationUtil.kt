package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.curation.config.DrugInteractionConfig
import com.hartwig.actin.datamodel.clinical.DrugInteraction

object DrugInteractionCurationUtil {
    fun curateMedicationCypInteractions(
        drugInteractionCuration: CurationDatabase<DrugInteractionConfig>,
        medicationName: String
    ): List<DrugInteraction> {
        return drugInteractionCuration.find(medicationName).flatMap(DrugInteractionConfig::cypInteractions)
    }

    fun curateMedicationTransporterInteractions(
        drugInteractionCuration: CurationDatabase<DrugInteractionConfig>,
        medicationName: String
    ): List<DrugInteraction> {
        return drugInteractionCuration.find(medicationName).flatMap(DrugInteractionConfig::transporterInteractions)
    }
}