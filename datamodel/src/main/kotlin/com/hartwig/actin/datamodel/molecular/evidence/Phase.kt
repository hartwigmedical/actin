package com.hartwig.actin.datamodel.molecular.evidence

enum class Phase(val isLatePhase: Boolean) {
    EXPANDED_ACCESS(false),
    PHASE_0(false),
    PHASE_I(false),
    PHASE_IB_II(false),
    PHASE_II(true),
    PHASE_III(true),
    FDA_APPROVED(true),
    UNKNOWN(false)
}