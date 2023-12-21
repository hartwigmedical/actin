package com.hartwig.actin.clinical.datamodel

import org.immutables.value.Value
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable

@Value.Immutable
@Value.Style(passAnnotations = [NotNull::class, Nullable::class])
abstract class TumorDetails {
    abstract fun primaryTumorLocation(): String?
    abstract fun primaryTumorSubLocation(): String?
    abstract fun primaryTumorType(): String?
    abstract fun primaryTumorSubType(): String?
    abstract fun primaryTumorExtraDetails(): String?
    abstract fun doids(): Set<String?>?
    abstract fun stage(): TumorStage?
    abstract fun hasMeasurableDisease(): Boolean?
    abstract fun hasBrainLesions(): Boolean?
    abstract fun hasActiveBrainLesions(): Boolean?
    abstract fun hasCnsLesions(): Boolean?
    abstract fun hasActiveCnsLesions(): Boolean?
    abstract fun hasBoneLesions(): Boolean?
    abstract fun hasLiverLesions(): Boolean?
    abstract fun hasLungLesions(): Boolean?
    abstract fun hasLymphNodeLesions(): Boolean?
    abstract fun otherLesions(): List<String?>?
    abstract fun biopsyLocation(): String?
}
