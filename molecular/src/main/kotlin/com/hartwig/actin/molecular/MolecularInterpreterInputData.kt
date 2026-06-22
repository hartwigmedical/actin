package com.hartwig.actin.molecular

import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.molecular.driverlikelihood.DndsDatabase
import com.hartwig.actin.molecular.evidence.ServeLoader
import com.hartwig.actin.molecular.filter.AlwaysValidFilter
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.filter.GeneFilterFactory
import com.hartwig.actin.molecular.panel.EMPTY_VARIANT_DECOMPOSITION_TABLE
import com.hartwig.actin.molecular.panel.PanelGeneSpecificationsFile
import com.hartwig.actin.molecular.panel.PanelSpecifications
import com.hartwig.actin.molecular.panel.PaveVariantDecomposition
import com.hartwig.actin.molecular.panel.VariantDecompositionTable
import com.hartwig.actin.tools.ensemblcache.EnsemblDataCache
import com.hartwig.actin.tools.ensemblcache.EnsemblDataLoader
import com.hartwig.hmftools.common.fusion.KnownFusionCache
import com.hartwig.hmftools.datamodel.OrangeJson
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.serve.datamodel.ServeDatabase
import com.hartwig.serve.datamodel.serialization.ServeJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import io.github.oshai.kotlinlogging.KotlinLogging

data class MolecularInterpreterInputData(
    val clinical: ClinicalRecord,
    val orange: OrangeRecord? = null,
    val serveDatabase: ServeDatabase,
    val doidEntry: DoidEntry,
    val ensemblDataCache: EnsemblDataCache,
    val dndsDatabase: DndsDatabase,
    val knownFusionCache: KnownFusionCache,
    val panelSpecifications: PanelSpecifications,
    val geneFilter: GeneFilter,
    val variantDecompositions: VariantDecompositionTable
)

object InputDataLoader {
    val logger = KotlinLogging.logger {}

    suspend fun load(config: MolecularInterpreterConfig, clinicalRefGenomeVersion: RefGenomeVersion): MolecularInterpreterInputData =
        coroutineScope {
            val serveJsonFilePath = ServeJson.jsonFilePath(config.serveDirectory)

            val clinical = async {
                withContext(Dispatchers.IO) {
                    logger.info { "Loading clinical json from ${config.clinicalJson}" }
                    ClinicalRecordJson.read(config.clinicalJson)
                }
            }
            val orange = async {
                withContext(Dispatchers.IO) {
                    if (config.orangeJson != null) {
                        logger.info { "Reading ORANGE json from ${config.orangeJson}" }
                        OrangeJson.getInstance().read(config.orangeJson)
                    } else {
                        null
                    }
                }
            }
            val deferredServeDatabase = async {
                withContext(Dispatchers.IO) {
                    logger.info { "Loading SERVE database from $serveJsonFilePath" }
                    val serveDatabase = ServeLoader.loadServeDatabase(serveJsonFilePath, config.removeCombinedProfilesEvidence)
                    logger.info { " Loaded evidence and known events from SERVE version ${serveDatabase.version()}" }
                    serveDatabase
                }
            }
            val deferredDoidEntry = async {
                withContext(Dispatchers.IO) {
                    logger.info { "Loading DOID tree from ${config.doidJson}" }
                    val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
                    logger.info { " Loaded ${doidEntry.nodes.size} nodes from DOID tree" }
                    doidEntry
                }
            }
            val deferredEnsemblDataCache = async {
                withContext(Dispatchers.IO) {
                    val ensemblRefGenomeVersion = toEnsemblRefGenomeVersion(clinicalRefGenomeVersion)
                    logger.info { "Loading ensemble cache from ${config.ensemblCachePath}" }
                    EnsemblDataLoader.load(config.ensemblCachePath, ensemblRefGenomeVersion)
                }
            }
            val deferredDndsDatabase = async {
                withContext(Dispatchers.IO) {
                    logger.info { "Loading dnds database for driver likelihood annotation from ${config.oncoDndsDatabasePath} and ${config.tsgDndsDatabasePath}" }
                    DndsDatabase.create(config.oncoDndsDatabasePath, config.tsgDndsDatabasePath)
                }
            }
            val deferredKnownFusionCache = async {
                withContext(Dispatchers.IO) {
                    logger.info { "Loading known fusions from " + config.knownFusionsPath }
                    val knownFusionCache = KnownFusionCache()
                    if (!knownFusionCache.loadFromFile(config.knownFusionsPath)) {
                        throw IllegalArgumentException("Failed to load known fusions from ${config.knownFusionsPath}")
                    }
                    knownFusionCache
                }
            }

            val deferredGeneFilter = async {
                val serveRecord =
                    deferredServeDatabase.await().records()[ServeLoader.toServeRefGenomeVersion(CLINICAL_TESTS_REF_GENOME_VERSION)]
                        ?: throw IllegalStateException("Serve record not present for ref genome version ${CLINICAL_TESTS_REF_GENOME_VERSION.name} in ${config.serveDirectory}")
                GeneFilterFactory.createFromKnownGenes(serveRecord.knownEvents().genes())
            }

            val deferredPanelSpecifications = async {

                withContext(Dispatchers.IO) {
                    logger.info { "Loading panel specifications from ${config.panelSpecificationsFilePath}" }
                    config.panelSpecificationsFilePath?.let { PanelGeneSpecificationsFile.create(it, deferredGeneFilter.await()) }
                        ?: PanelSpecifications(AlwaysValidFilter(), emptyMap())
                }
            }

            val deferredVariantDecompositions = async {
                withContext(Dispatchers.IO) {
                    val decompositionFilePath = config.variantDecompositionFilePath
                        ?: return@withContext EMPTY_VARIANT_DECOMPOSITION_TABLE

                    logger.info { "Loading variant decompositions from $decompositionFilePath" }
                    val entries = PaveVariantDecomposition.readFromFile(decompositionFilePath)
                    logger.info { "Loaded ${entries.size} variant decomposition entries" }
                    VariantDecompositionTable(entries)
                }
            }

            MolecularInterpreterInputData(
                clinical = clinical.await(),
                orange = orange.await(),
                serveDatabase = deferredServeDatabase.await(),
                doidEntry = deferredDoidEntry.await(),
                ensemblDataCache = deferredEnsemblDataCache.await(),
                dndsDatabase = deferredDndsDatabase.await(),
                knownFusionCache = deferredKnownFusionCache.await(),
                panelSpecifications = deferredPanelSpecifications.await(),
                geneFilter = deferredGeneFilter.await(),
                variantDecompositions = deferredVariantDecompositions.await()
            )
        }
}
