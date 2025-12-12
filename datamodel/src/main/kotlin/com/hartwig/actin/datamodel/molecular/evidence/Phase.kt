package com.hartwig.actin.datamodel.molecular.evidence

enum class Phase(val text: String?, val isLatePhase: Boolean) {
    EXPANDED_ACCESS(null, true),
    PHASE_0("Phase 0", false),
    PHASE_I("Phase 1", false),
    PHASE_IB_II("Phase 1b/2", false),
    PHASE_II("Phase 2", true),
    PHASE_III("Phase 3", true),
    FDA_APPROVED(null, true),
    UNKNOWN(null, false)
}