package com.hartwig.actin.clinical.curation

import com.hartwig.actin.clinical.DrugInteractions
import com.hartwig.actin.clinical.DrugInteractionsDatabase
import com.hartwig.actin.datamodel.clinical.DrugInteraction

object TestDrugInteractionsFactory {

    fun createProper(
        medication: String = "",
        cypInteractions: List<DrugInteraction> = emptyList(),
        transporterInteractions: List<DrugInteraction> = emptyList()
    ): DrugInteractionsDatabase {
        return DrugInteractionsDatabase(
            mapOf(
                medication to
                        DrugInteractions(cypInteractions, transporterInteractions)
            )
        )
    }
}