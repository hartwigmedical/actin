package com.hartwig.actin.trial.status

interface TrialStatusEntryReader {
    fun read(inputPath: String): List<TrialStatusEntry>
}