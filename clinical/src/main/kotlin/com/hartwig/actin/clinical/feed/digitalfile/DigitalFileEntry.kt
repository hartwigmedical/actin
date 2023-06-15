package com.hartwig.actin.clinical.feed.digitalfile

import com.hartwig.actin.clinical.feed.FeedEntry
import java.time.LocalDate

data class DigitalFileEntry(
    override val subject: String,
    val authored: LocalDate,
    val description: String,
    val itemText: String,
    val itemAnswerValueValueString: String,
    val isBloodTransfusionEntry: Boolean = description == BLOOD_TRANSFUSION_DESCRIPTION,
    val isToxicityEntry: Boolean = description == TOXICITY_DESCRIPTION
) : FeedEntry {

    companion object {
        private const val BLOOD_TRANSFUSION_DESCRIPTION = "Aanvraag bloedproducten_test"
        private const val TOXICITY_DESCRIPTION = "ONC Kuuroverzicht"
    }
}