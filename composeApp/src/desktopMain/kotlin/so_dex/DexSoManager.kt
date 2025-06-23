package so_dex

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

/**
 * DexSoManager
 *
 * @author mmxm
 * @date 2025/1/14 14:51
 */
object DexSoManager {
    data class DexConfigData(val list: List<DexVerData>, val sourceType: List<String>)

    data class DexVerData(val name: String, val version: String,val isAdObs:Boolean,val isNewBh:Boolean)

    val APP_OUTPUT_PATH by lazy {
        val userDir = System.getProperty("user.dir")
        val outDir = File("$userDir/config/")
        outDir.mkdirs()
        outDir.path
    }

    private const val dexVerjsonFile = "dexVer.json"
    private val buildPyPath = "$APP_OUTPUT_PATH/cd_build.py"

    fun getDexVerJson(): DexConfigData {
        val jFile = File("$APP_OUTPUT_PATH/$dexVerjsonFile")
        if(!jFile.exists()){
            return DexConfigData(emptyList(), emptyList())
        }
        println("jfile path:${jFile.path}")
        val jsonData = jFile.readText()
        val obj = Json.parseToJsonElement(jsonData).jsonObject
        val list = obj.get("dexList")?.jsonArray!!

        val dataList = mutableListOf<DexVerData>()
        list.forEach {
            val name = it.jsonObject.get("name")?.jsonPrimitive?.content!!
            val version = it.jsonObject.get("version")?.jsonPrimitive?.content!!
            val isAdObs = it.jsonObject.get("isAdObs")?.jsonPrimitive?.content!!.toBoolean()
            val isNewBh = it.jsonObject.get("isNewBh")?.jsonPrimitive?.content!!.toBoolean()
            val data = DexVerData(name,version, isAdObs,isNewBh)
            dataList.add(data)
        }
        println(dataList)

        val sourceType = obj.get("sourceType")?.jsonArray!!
        val sourceList = mutableListOf<String>()
        sourceType.forEach {
            sourceList.add(it.jsonPrimitive.content)
        }
        return DexConfigData(dataList, sourceList)
    }

    fun build(dexVer: DexVerData, type: String, singleId: String) {
        val ver102=if(dexVer.name=="102版本"){
            1
        }else{
            0
        }
        val isAdObs=if(dexVer.isAdObs){
            1
        }else{
            0
        }

        val isNewBh=if(dexVer.isNewBh){
            1
        }else{
            0
        }
        val cmd="python $buildPyPath ${dexVer.version} $singleId ${type} $ver102 $isAdObs $isNewBh"
        println("cmd ->:$cmd")
        ToolsManager.openCmdWindow(cmd)
    }
}