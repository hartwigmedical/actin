package com.hartwig.actin.molecular.datamodel.panel

import com.hartwig.actin.molecular.datamodel.Drivers

class PanelDrivers(override val variants: Set<PanelVariant> = emptySet(), override val fusions: Set<PanelFusion> = emptySet()) :
    Drivers<PanelVariant, PanelFusion> {
}