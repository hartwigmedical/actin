package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.itextpdf.layout.element.Table

class OnLabelMolecularClinicalEvidenceGenerator(val molecularTests: List<MolecularTest>) : TableGenerator {

    private val wrapped = MolecularClinicalEvidenceGenerator(molecularTests, isOnLabel = true)
    
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