package com.hartwig.actin.molecular.clinical

import com.hartwig.actin.molecular.MolecularAnnotator
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.panel.generic.GenericPanel

class GenericPanelAnnotator : MolecularAnnotator<GenericPanel> {
    override fun annotate(input: MolecularTest<GenericPanel>): MolecularTest<GenericPanel> {
        return input
    }
}