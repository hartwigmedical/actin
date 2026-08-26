package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.itextpdf.layout.element.Table

class OffLabelMolecularClinicalEvidenceGenerator(
    val molecularTests: List<MolecularTest>,
    private val labels: ReportLabels
) : TableGenerator {

    private val wrapped = MolecularClinicalEvidenceGenerator(
        molecularTests,
        isOnLabel = false,
        labels = labels
    )

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
