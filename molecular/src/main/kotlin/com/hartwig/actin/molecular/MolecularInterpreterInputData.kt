package com.hartwig.actin.molecular

import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.molecular.driverlikelihood.DndsDatabase
import com.hartwig.actin.molecular.evidence.ServeLoader
import com.hartwig.actin.molecular.panel.PanelGeneSpecificationsFile
import com.hartwig.actin.molecular.panel.PanelSpecifications
import com.hartwig.actin.molecular.util.EnsemblUtil.toHmfRefGenomeVersion
import com.hartwig.hmftools.common.ensemblcache.EnsemblDataCache
import com.hartwig.hmftools.common.fusion.KnownFusionCache
import com.hartwig.hmftools.datamodel.OrangeJson
import com.hartwig.hmftools.datamodel.orange.OrangeRecord
import com.hartwig.serve.datamodel.ServeDatabase
import com.hartwig.serve.datamodel.serialization.ServeJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

data class MolecularInterpreterInputData(
    val clinical: ClinicalRecord,
    val orange: OrangeRecord? = null,
    val serveDatabase: ServeDatabase,
    val doidEntry: DoidEntry,
    val ensemblDataCache: EnsemblDataCache,
    val dndsDatabase: DndsDatabase,
    val knownFusionCache: KnownFusionCache,
    val panelSpecifications: PanelSpecifications
)

object InputDataLoader {
    val LOGGER: Logger = LogManager.getLogger(InputDataLoader::class.java)

    suspend fun load(config: MolecularInterpreterConfig, clinicalRefGenomeVersion: RefGenomeVersion): MolecularInterpreterInputData =
        coroutineScope {
            val serveJsonFilePath = ServeJson.jsonFilePath(config.serveDirectory)

            val clinical = async {
                withContext(Dispatchers.IO) {
                    LOGGER.info("Loading clinical json from {}", config.clinicalJson)
                    ClinicalRecordJson.read(config.clinicalJson)
                }
            }
            val orange = async {
                withContext(Dispatchers.IO) {
                    if (config.orangeJson != null) {
                        LOGGER.info("Reading ORANGE json from {}", config.orangeJson)
                        OrangeJson.getInstance().read(config.orangeJson)
                    } else {
                        null
                    }
                }
            }
            val deferredServeDatabase = async {
                withContext(Dispatchers.IO) {
                    LOGGER.info("Loading SERVE database from {}", serveJsonFilePath)
                    val serveDatabase = ServeLoader.loadServeDatabase(serveJsonFilePath)
                    LOGGER.info(" Loaded evidence and known events from SERVE version {}", serveDatabase.version())
                    serveDatabase
                }
            }
            val deferredDoidEntry = async {
                withContext(Dispatchers.IO) {
                    LOGGER.info("Loading DOID tree from {}", config.doidJson)
                    val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
                    LOGGER.info(" Loaded {} nodes from DOID tree", doidEntry.nodes.size)
                    doidEntry
                }
            }
            //  TODO check v37 vs v38, could we need both in any single run? can we ensure ref genome version is consistent with ensembl cache path?
            val deferredEnsemblDataCache = async {
                withContext(Dispatchers.IO) {
                    val ensemblRefGenomeVersion = toHmfRefGenomeVersion(clinicalRefGenomeVersion)
                    LOGGER.info("Loading ensemble cache from ${config.ensemblCachePath}")
                    val ensemblCache = EnsemblDataCache(config.ensemblCachePath, ensemblRefGenomeVersion)
                    ensemblCache.setRequiredData(true, true, true, false)
                    ensemblCache.load(false)
                    ensemblCache
                }
            }
            val deferredDndsDatabase = async {
                withContext(Dispatchers.IO) {
                    LOGGER.info(
                        "Loading dnds database for driver likelihood annotation from " +
                                "${config.oncoDndsDatabasePath} and ${config.tsgDndsDatabasePath}"
                    )
                    DndsDatabase.create(config.oncoDndsDatabasePath, config.tsgDndsDatabasePath)
                }
            }
            val deferredKnownFusionCache = async {
                withContext(Dispatchers.IO) {
                    LOGGER.info("Loading known fusions from " + config.knownFusionsPath)
                    val knownFusionCache = KnownFusionCache()
                    if (!knownFusionCache.loadFromFile(config.knownFusionsPath)) {
                        throw IllegalArgumentException("Failed to load known fusions from ${config.knownFusionsPath}")
                    }
                    knownFusionCache
                }
            }
            val panelSpecifications = async {
                withContext(Dispatchers.IO) {
                    LOGGER.info("Loading panel specifications from {}", config.panelSpecificationsFilePath)
                    val panelSpecifications =
                        config.panelSpecificationsFilePath?.let { PanelGeneSpecificationsFile.create(it) }
                            ?: PanelSpecifications(emptyMap())
                    panelSpecifications
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
                panelSpecifications = panelSpecifications.await()
            )
        }
}

