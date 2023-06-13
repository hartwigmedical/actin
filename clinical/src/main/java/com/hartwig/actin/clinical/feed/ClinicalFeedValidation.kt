package com.hartwig.actin.clinical.feed

import com.google.common.collect.Sets
import com.hartwig.actin.clinical.feed.patient.PatientEntry

internal object ClinicalFeedValidation {
    @JvmStatic
    fun validate(feed: ClinicalFeed) {
        enforceUniquePatients(feed.patientEntries())
    }

    private fun enforceUniquePatients(patients: List<PatientEntry?>) {
        val subjects: MutableSet<String> = Sets.newHashSet()
        for (patient in patients) {
            val subject = patient!!.subject()
            check(!subjects.contains(subject)) { "Duplicate subject found in clinical feed patient entries: $subject" }
            subjects.add(subject)
        }
    }
}