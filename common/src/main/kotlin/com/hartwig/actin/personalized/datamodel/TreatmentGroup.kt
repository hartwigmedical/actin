package com.hartwig.actin.personalized.datamodel

enum class TreatmentGroup(val display: String, val memberTreatmentNames: List<String>) {
    CAPECITABINE_OR_FLUOROURACIL("Capecitabine / Fluorouracil", listOf("capecitabine", "fluorouracil")),
    CAPECITABINE_B_OR_FLUOROURACIL_B("Capecitabine-B / Fluorouracil-B", listOf("capecitabine+bevacizumab", "fluorouracil+bevacizumab")),
    CAPOX_OR_FOLFOX("CAPOX / FOLFOX", listOf("capox", "folfox")),
    CAPOX_B_OR_FOLFOX_B("CAPOX-B / FOLFOX-B", listOf("capox+bevacizumab", "folfox+bevacizumab")),
    FOLFIRI("FOLFIRI", listOf("folfiri")),
    FOLFIRI_B("FOLFIRI-B", listOf("folfiri+bevacizumab")),
    FOLFIRI_P("FOLFIRI-P", listOf("folfiri+panitumumab")),
    FOLFOX_P("FOLFOX-P", listOf("folfox+panitumumab")),
    FOLFOXIRI("FOLFOXIRI", listOf("folfoxiri")),
    FOLFOXIRI_B("FOLFOXIRI-B", listOf("folfoxiri+bevacizumab")),
    IRINOTECAN("Irinotecan", listOf("irinotecan")),
    NIVOLUMAB("Nivolumab", listOf("nivolumab")),
    PEMBROLIZUMAB("Pembrolizumab", listOf("pembrolizumab"));

    companion object {
        private val groupsByTreatmentName = entries.flatMap { group -> group.memberTreatmentNames.map { it to group } }.toMap()

        fun fromTreatmentName(treatmentName: String): TreatmentGroup? {
            return groupsByTreatmentName[treatmentName.lowercase()]
        }
    }
}
