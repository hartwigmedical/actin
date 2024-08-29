package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.itextpdf.layout.element.Table

class OffLabelMolecularClinicalEvidenceGenerator(val molecularHistory: MolecularHistory, private val width: Float) : TableGenerator {

    override fun title(): String {
        return MolecularClinicalEvidenceGenerator(molecularHistory, width, onLabel = false).title()
    }

    override fun contents(): Table {
        return MolecularClinicalEvidenceGenerator(molecularHistory, width, onLabel = false).contents()
    }
}