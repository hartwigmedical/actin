package com.hartwig.actin.clinical.feed.questionnaire

import com.hartwig.actin.clinical.datamodel.ECG
import com.hartwig.actin.clinical.datamodel.InfectionStatus
import com.hartwig.actin.clinical.datamodel.TumorStage
import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.time.LocalDate

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class Questionnaire {
    abstract fun date(): LocalDate
    abstract fun tumorLocation(): String?
    abstract fun tumorType(): String?
    abstract fun biopsyLocation(): String?
    abstract fun stage(): TumorStage?
    abstract fun treatmentHistoryCurrentTumor(): List<String?>?
    abstract fun otherOncologicalHistory(): List<String?>?
    abstract fun secondaryPrimaries(): List<String?>?
    abstract fun nonOncologicalHistory(): List<String?>?
    abstract fun hasMeasurableDisease(): Boolean?
    abstract fun hasBrainLesions(): Boolean?
    abstract fun hasActiveBrainLesions(): Boolean?
    abstract fun hasCnsLesions(): Boolean?
    abstract fun hasActiveCnsLesions(): Boolean?
    abstract fun hasBoneLesions(): Boolean?
    abstract fun hasLiverLesions(): Boolean?
    abstract fun otherLesions(): List<String?>?
    abstract fun ihcTestResults(): List<String?>?
    abstract fun pdl1TestResults(): List<String?>?
    abstract fun whoStatus(): Int?
    abstract fun unresolvedToxicities(): List<String?>?
    abstract fun infectionStatus(): InfectionStatus?
    abstract fun ecg(): ECG?
    abstract fun complications(): List<String?>?
    abstract fun genayaSubjectNumber(): String?
}