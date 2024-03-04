package com.hartwig.actin.clinical.feed.standard

import com.hartwig.actin.clinical.ExtractionResult
import com.hartwig.actin.clinical.curation.CurationDatabase
import com.hartwig.actin.clinical.curation.config.MolecularTestConfig
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest

class EhrMolecularTestExtractor(
    private val molecularTestIhcCuration: CurationDatabase<MolecularTestConfig>,
    private val molecularTestPdl1Curation: CurationDatabase<MolecularTestConfig>,
) : EhrExtractor<List<PriorMolecularTest>> {
    override fun extract(ehrPatientRecord: EhrPatientRecord): ExtractionResult<List<PriorMolecularTest>> {
        TODO("Not yet implemented")
    }
}
