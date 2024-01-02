package com.hartwig.actin.clinical.datamodel

data class TumorDetails(
    val primaryTumorLocation: String? = null,
    val primaryTumorSubLocation: String? = null,
    val primaryTumorType: String? = null,
    val primaryTumorSubType: String? = null,
    val primaryTumorExtraDetails: String? = null,
    val doids: Set<String>? = null,
    val stage: TumorStage? = null,
    val hasMeasurableDisease: Boolean? = null,
    val hasBrainLesions: Boolean? = null,
    val hasActiveBrainLesions: Boolean? = null,
    val hasCnsLesions: Boolean? = null,
    val hasActiveCnsLesions: Boolean? = null,
    val hasBoneLesions: Boolean? = null,
    val hasLiverLesions: Boolean? = null,
    val hasLungLesions: Boolean? = null,
    val hasLymphNodeLesions: Boolean? = null,
    val otherLesions: List<String>? = null,
    val biopsyLocation: String? = null
)
