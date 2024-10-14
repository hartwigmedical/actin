package com.hartwig.actin.datamodel.clinical

data class TumorDetails(
    val primaryTumorLocation: String? = null,
    val primaryTumorSubLocation: String? = null,
    val primaryTumorType: String? = null,
    val primaryTumorSubType: String? = null,
    val primaryTumorExtraDetails: String? = null,
    val doids: Set<String>? = null,
    val stage: TumorStage? = null,
    val derivedStages: Set<TumorStage>? = null,
    val hasMeasurableDisease: Boolean? = null,
    val hasBrainLesions: Boolean? = null,
    val hasSuspectedBrainLesions: Boolean? = null,
    val brainLesionsCount: Int? = null,
    val hasActiveBrainLesions: Boolean? = null,
    val hasCnsLesions: Boolean? = null,
    val hasSuspectedCnsLesions: Boolean? = null,
    val cnsLesionsCount: Int? = null,
    val hasActiveCnsLesions: Boolean? = null,
    val hasBoneLesions: Boolean? = null,
    val hasSuspectedBoneLesions: Boolean? = null,
    val boneLesionsCount: Int? = null,
    val hasLiverLesions: Boolean? = null,
    val hasSuspectedLiverLesions: Boolean? = null,
    val liverLesionsCount: Int? = null,
    val hasLungLesions: Boolean? = null,
    val hasSuspectedLungLesions: Boolean? = null,
    val lungLesionsCount: Int? = null,
    val hasLymphNodeLesions: Boolean? = null,
    val hasSuspectedLymphNodeLesions: Boolean? = null,
    val lymphNodeLesionsCount: Int? = null,
    val otherLesions: List<String>? = null,
    val otherSuspectedLesions: List<String>? = null,
    val biopsyLocation: String? = null,
    val rawPathologyReport: String? = null
) {

    /*
        The functions below are only necessary to maintain backwards compatibility until
        the algo's are updated to handle suspected lesions (scope for ACTIN-1319).
        Until then, suspected lesions are to be considered lesions and the functions below
        were created to minimize the changes needed to keep backwards compatibility.
     */
    fun hasBrainLesions(): Boolean? {
        return combine(hasBrainLesions, hasSuspectedBrainLesions)
    }

    fun hasCnsLesions(): Boolean? {
        return combine(hasCnsLesions, hasSuspectedCnsLesions)
    }

    fun hasBoneLesions(): Boolean? {
        return combine(hasBoneLesions, hasSuspectedBoneLesions)
    }

    fun hasLiverLesions(): Boolean? {
        return combine(hasLiverLesions, hasSuspectedLiverLesions)
    }

    fun hasLungLesions(): Boolean? {
        return combine(hasLungLesions, hasSuspectedLungLesions)
    }

    fun hasLymphNodeLesions(): Boolean? {
        return combine(hasLymphNodeLesions, hasSuspectedLymphNodeLesions)
    }

    fun otherLesions(): List<String>? {
        return otherLesions?.let { otherLesions + (otherSuspectedLesions ?: emptyList()) } ?: otherSuspectedLesions
    }

    private fun combine(b1: Boolean?, b2: Boolean?): Boolean? {
        return if (b1 == true || b2 == true) {
            true
        } else {
            b1 ?: b2
        }
    }
}