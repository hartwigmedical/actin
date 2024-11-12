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
    val hasActiveBrainLesions: Boolean? = null,
    val brainLesionsCount: Int? = null,
    val hasCnsLesions: Boolean? = null,
    val hasSuspectedCnsLesions: Boolean? = null,
    val hasActiveCnsLesions: Boolean? = null,
    val cnsLesionsCount: Int? = null,
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

    fun hasConfirmedBrainLesions() = hasBrainLesions == true || hasActiveBrainLesions == true
    fun hasConfirmedCnsLesions() = hasCnsLesions == true || hasActiveCnsLesions == true

    fun confirmedCategoricalLesionList(): List<Boolean?> {
        return listOf(hasLiverLesions, hasCnsLesions, hasBrainLesions, hasBoneLesions, hasLungLesions, hasLymphNodeLesions)
    }

    fun suspectedCategoricalLesionList(): List<Boolean?> {
        return listOf(
            hasSuspectedLiverLesions,
            hasSuspectedCnsLesions,
            hasSuspectedBrainLesions,
            hasSuspectedBoneLesions,
            hasSuspectedLungLesions,
            hasSuspectedLymphNodeLesions
        )
    }

    fun hasConfirmedLesions() = confirmedCategoricalLesionList().any { it == true } || !otherLesions.isNullOrEmpty()
    fun hasSuspectedLesions() = suspectedCategoricalLesionList().any { it == true } || !otherSuspectedLesions.isNullOrEmpty()

    companion object {
        const val BONE = "Bone"
        const val LIVER = "Liver"
        const val LUNG = "Lung"
        const val LYMPH_NODE = "Lymph node"
        const val CNS = "CNS"
        const val BRAIN = "Brain"
    }
}