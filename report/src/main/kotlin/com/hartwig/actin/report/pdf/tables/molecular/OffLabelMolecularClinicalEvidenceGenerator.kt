package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.itextpdf.layout.element.Table

class OffLabelMolecularClinicalEvidenceGenerator(val molecularHistory: MolecularHistory, val width: Float) : TableGenerator {

    private val wrapped = MolecularClinicalEvidenceGenerator(molecularHistory, width, isOnLabel = false)
    
    override fun title(): String {
        return wrapped.title()
    }

    override fun forceKeepTogether(): Boolean {
        return wrapped.forceKeepTogether()
    }

    override fun contents(): Table {
        return wrapped.contents()
    }
}