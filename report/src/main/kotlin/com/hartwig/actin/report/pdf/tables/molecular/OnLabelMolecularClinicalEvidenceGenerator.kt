package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.itextpdf.layout.element.Table

class OnLabelMolecularClinicalEvidenceGenerator(val molecularHistory: MolecularHistory, private val width: Float) : TableGenerator {

    override fun title(): String {
        return MolecularClinicalEvidenceGenerator(molecularHistory, width, isOnLabel = true).title()
    }

    override fun contents(): Table {
        return MolecularClinicalEvidenceGenerator(molecularHistory, width, isOnLabel = true).contents()
    }
}