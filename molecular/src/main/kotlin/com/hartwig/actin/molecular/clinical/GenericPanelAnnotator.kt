package com.hartwig.actin.molecular.clinical

import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanel

class GenericPanelAnnotator : MolecularAnnotator<GenericPanel> {
    override fun annotate(input: GenericPanel): GenericPanel {
        return input
    }
}