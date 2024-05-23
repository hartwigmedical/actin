package com.hartwig.actin.molecular.datamodel.panel

import com.hartwig.actin.molecular.interpreted.InterpretedDrivers

class PanelDrivers(override val variants: Set<PanelVariant> = emptySet(), override val fusions: Set<PanelFusion> = emptySet()) :
    InterpretedDrivers<PanelVariant, PanelFusion> {
}