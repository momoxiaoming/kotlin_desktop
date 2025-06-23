import mapping.ConfigInfo
import kotlinx.serialization.encodeToString
import java.io.File

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mapping.MappingManager
import mapping.MappingManager.get
import utils.AesUtils
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * ..ToolsManager
 *
 * @author mmxm
 * @date 2024/7/11 10:42
 */
object ToolsManager {

    private val DX_PATH = "./dx.jar"
    val APP_OUTPUT_PATH by lazy {
        val userDir = System.getProperty("user.dir")
        val outDir = File("$userDir/output/")
        outDir.mkdirs()
        outDir.path
    }

    /**
     * 导出app
     * @return String
     */
    fun exportApp(pkg: String): String {
        val appPath = CmdUtils.execCmdSilent("adb shell pm path $pkg").takeIf { it.isNotEmpty() } ?: ""
        if (appPath.isNotEmpty()) {
            val outPath = "${APP_OUTPUT_PATH}/$pkg.apk"
            val ret = CmdUtils.execCmdSilent("adb pull ${appPath.replace("package:", "")} $outPath")
                .takeIf { it.isNotEmpty() } ?: ""
            println("导出结果: $ret")
            return outPath
        }
        return ""
    }

    fun jarToDex(jarPath: String): String {
        val dexPath = "${APP_OUTPUT_PATH}/output.dex"
        val cmd = "java -jar $DX_PATH --dex --output $dexPath $jarPath"
        CmdUtils.execCmdSilent(cmd).takeIf { it.isNotEmpty() } ?: ""
        return dexPath
    }

    /**
     * dump activity
     * @return String
     */
    fun dumpActivity(): String {
        val ret = CmdUtils.execCmdSilent("adb shell dumpsys activity top").takeIf { it.isNotEmpty() }
            ?: return ""
        val info = StringBuilder()
        ret.split("\n").map { it.trimStart() }.filter { it.startsWith("ACTIVITY ") }.map { it.trim() }
            .forEach {
                info.append(it + "\n")
            }
        return info.toString()
    }

    /**
     * 查看签名
     * @param path String
     * @return String
     */
    fun lookJks(path: String): String {
        val ret = CmdUtils.execCmdSilent("keytool -printcert -jarfile $path").takeIf { it.isNotEmpty() }
            ?: return ""
        val signInfo = StringBuilder()
        //解析出SHA1 和 ShA256
        ret.split("\n")
            .filter { it.contains("SHA1:") || it.contains("SHA256:") }
            .map { it.replace(Regex("\\s+"), " ").trim().replace(" ", "") }
            .map { it.replace("SHA1:", "SHA1:\n").replace("SHA256:", "SHA256:\n") }
            .forEach {
                signInfo.append(it + "\n")
            }

        return signInfo.toString()
    }


    fun getPhoneMode(): String {
        return CmdUtils.execCmdSilent("adb shell getprop ro.product.model").replace("\r\n", "")
            .takeIf { it.isNotEmpty() }
            ?: "未知"
    }

    fun getPhoneVersion(): String {
        return CmdUtils.execCmdSilent("adb shell getprop ro.build.version.release").replace("\r\n", "")
            .takeIf { it.isNotEmpty() }
            ?: "未知"
    }

    fun getFuckDoorState(): Boolean {
        val ret = CmdUtils.execCmdSilent("adb shell getprop debug.privacy").replace("\r\n", "")
        val ret2 = CmdUtils.execCmdSilent("adb shell getprop debug.gg").replace("\r\n", "")

        return ret == "fuck_door_888" && ret2=="gg_body_123"
    }

    fun getBhState(): Boolean {
        val dev = CmdUtils.execCmdSilent("adb shell getprop debug.ddv.dev").replace("\r\n", "") == "1"
        val dbgapp = CmdUtils.execCmdSilent("adb shell getprop debug.ddv.dbgapp").replace("\r\n", "") == "1"
        val proxy = CmdUtils.execCmdSilent("adb shell getprop debug.ddv.proxy").replace("\r\n", "") == "1"
        val vp = CmdUtils.execCmdSilent("adb shell getprop debug.ddv.vp").replace("\r\n", "") == "1"

        return dev && dbgapp && proxy && vp
    }

    /**
     * 金手指+bh+日志
     * @return Boolean
     */
    fun openFuckDoor(): Boolean {
        CmdUtils.execCmdSilent("adb shell setprop debug.ddv.dev 1")
        CmdUtils.execCmdSilent("adb shell setprop debug.ddv.dbgapp 1")
        CmdUtils.execCmdSilent("adb shell setprop debug.ddv.proxy 1")
        CmdUtils.execCmdSilent("adb shell setprop debug.ddv.vp 1")
        CmdUtils.execCmdSilent("adb shell setprop debug.privacy fuck_door_888")
        CmdUtils.execCmdSilent("adb shell setprop debug.gg gg_body_123")
        return true
    }

    fun closeFuckDoor(): Boolean {
        CmdUtils.execCmdSilent("adb shell setprop debug.ddv.dev 0")
        CmdUtils.execCmdSilent("adb shell setprop debug.ddv.dbgapp 0")
        CmdUtils.execCmdSilent("adb shell setprop debug.ddv.proxy 0")
        CmdUtils.execCmdSilent("adb shell setprop debug.ddv.vp 0")
        CmdUtils.execCmdSilent("adb shell setprop debug.privacy 11")
        CmdUtils.execCmdSilent("adb shell setprop debug.gg 11")
        return true
    }


    /**
     * 根据需求单号,加密字符串, 会验证字符串和需求单是否能对应上
     * @return String
     */
    fun encodeContent(orderId: String, content: String):String{
        val info = getWbAppIdAndCha(orderId)
        val appId = info?.appid
        val cha = info?.chan
        val pid = info?.pid
        if (appId.isNullOrEmpty() || cha.isNullOrEmpty() || pid.isNullOrEmpty() ) {
            return "需求单数据请求失败"
        }
        val map = MappingManager.getAllMapWord(appId)
        val mapping_content = getMappingData(Json.parseToJsonElement(content).jsonObject, map)
        if(mapping_content.isNullOrEmpty()){
            return "加密失败, 加密字符串与需求单对应不上!"
        }

        val result = StringBuilder()
        val key = AesUtils.getAesKey(appId, cha)
        val encode_content = AesUtils.encrypt(content, key, key)
        if(encode_content.isNullOrEmpty()){
            return "加密出错!"
        }
        result.append("\nfirebase Key(老板本走appid): ${getFirebaseKey(map)}")
        result.append("\nfirebase Key(新版本走项目id): ${getFirebaseKey2(pid)}")
        result.append("\nAES加密秘钥: ${key}")
        result.append("\n解映射结果: \n$mapping_content")
        result.append("\n加密结果: \n$encode_content")
        return result.toString()
    }


    fun decodeContent(orderId: String, content: String): String {
        val info = getWbAppIdAndCha(orderId)
        val appId = info?.appid
        val cha = info?.chan
        val pid = info?.pid
        if (appId.isNullOrEmpty() || cha.isNullOrEmpty() || pid.isNullOrEmpty() ) {
            return "需求单数据请求失败"
        }
        try {
            val map = MappingManager.getAllMapWord(appId)

            val result = StringBuilder()
            val key = AesUtils.getAesKey(appId, cha)
            result.append("\nfirebase Key(老板本走appid): ${getFirebaseKey(map)}")
            result.append("\nfirebase Key(1043走项目id): ${getFirebaseKey2(pid)}")
            result.append("\n参数原始结果:\n${content}")
            //解密
            val decode_content = AesUtils.decrypt(content, key, key)
            result.append("\n参数解密结果:\n${decode_content}")

            //解映射结果
            val mapping_content = getMappingData(Json.parseToJsonElement(decode_content).jsonObject, map)
            result.append("\n参数解映射结果:\n${mapping_content?:"原始结果解析出错,请检查解密内容是否和需求单对应"}")

            return result.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return "解密内容错误,无法解密:\n${e.printStackTrace()}"
        }

    }


    /**
     * 获取新中台单渠道appid
     * @param orderId String
     * @return Pair<String?, String?>?
     */
    fun getAppIdAndCha(orderId: String): Pair<String?, String?>? {
        try {
            val res = get("https://sunny.careduka.com/admin-api/work_order/app_detail/$orderId")
            val data = Json.parseToJsonElement(res).jsonObject.get("data")!!.jsonObject
            val appId = data.get("app_id")?.jsonPrimitive?.content
            val cha = data.get("app_source")?.jsonPrimitive?.content
            return Pair(appId, cha)
        } catch (e: Exception) {
            return null
        }
    }
    /**
     * 获取旧中台单渠道和appid
     * @param orderId String
     * @return Pair<String?, String?>?
     */
    fun getWbAppIdAndCha(orderId: String): ConfigInfo? {
        try {
            val res = get("http://dnsdk.vimedia.cn:8090/v5/FromConfigInfo?singleid=$orderId")
            val data = Json.parseToJsonElement(res).jsonObject.get("data")!!.jsonObject
            val appId = data.get("appid")?.jsonPrimitive?.content
            val cha = data.get("channelTag")?.jsonPrimitive?.content
            val app_num = data.get("app_num")?.jsonPrimitive?.content
            return ConfigInfo(appId!!, cha!!, app_num!!)
        } catch (e: Exception) {
            return null
        }
    }
    /**
     * 获取旧中台自定义参数
     * @param orderId String
     */
    fun getWbToolParamsData(orderId: String,):String{
        try {
            val res = get("http://dnsdk.vimedia.cn:8090/v5/FromConfigInfo?singleid=$orderId")
            val data = Json.parseToJsonElement(res).jsonObject.get("data")!!.jsonObject
            val appId = data.get("appid")?.jsonPrimitive?.content
            val cha = data.get("channelTag")?.jsonPrimitive?.content
            val app_num = data.get("pjId")?.jsonPrimitive?.content
            val moduleData = data.get("moduleData")?.jsonPrimitive?.content

            val mMap = hashMapOf<String, String>()
            val modules = moduleData?.split("#")
            modules?.forEach { m ->
                val data = m.split(";")
                if (data.size == 4) {
                    val key = data[2]
                    val value = data[3]
                    mMap.put(key, value)
                } else if (data.size == 3) {
                    val key = data[2]
                    val value = ""
                    mMap.put(key, value)
                }
            }
           val package_domain= mMap.getOrDefault("main_host", "")  //域名


            println("需求单data->${data.toString()}")

            if (appId.isNullOrEmpty() || cha.isNullOrEmpty() || app_num.isNullOrEmpty() || package_domain.isNullOrEmpty()) {
                //参数有误
                return "需求单请求参数为空"
            } else {
                //参数正常,请求自定义配置
                val map = MappingManager.getAllMapWord(appId)
                //先把请求参数映射出来
                val apid_mp = map.get("apid")
                val p_id_mp = map.get("pid")
                val cha_mp = map.get("c")
                val fw_mp = map.get("fw")
                val path1 = map.get("path1")
                val params = map.get("params")

                println("拿到的需求单请求参数映射: apid->$apid_mp,p_id->$p_id_mp,cha->$cha_mp,fw->$fw_mp")
                val url = "https://$package_domain/$path1"
                val body =
                    "{\"$apid_mp\":\"$appId\",\"$p_id_mp\":\"$app_num\",\"$cha_mp\":\"$cha\",\"$fw_mp\":\"1\"}"
                println("自定义参数请求地址->${url}")
                println("自定义参数请求参数->${body}")
                val paramsRes = MappingManager.post(url, body)
                println("自定义参数请求结果->${paramsRes}")
                //拿到所有自定义参数
                val params_data = try {
                    Json.parseToJsonElement(paramsRes).jsonObject.get("data")!!.jsonObject.get(params)!!.jsonObject
                } catch (e: Exception) {
                    return "自定义参数拉取异常\n:${paramsRes}"
                }
                if (params_data.isEmpty()) {
                    return "自定义参数拉取异常\n:${paramsRes}"
                }
                val result = StringBuilder()
                println("自定义参数请求结果->${params_data.toString()}")
                //拿到所有的自定义参数解密映射
                val dcode_params = getMappingData(params_data, map)
                println("自定义参数解密结果->${dcode_params.toString()}")
                val key = AesUtils.getAesKey(appId, cha)
                result.append("\nfirebase Key(老板本走appid): ${getFirebaseKey(map)}")
                result.append("\nfirebase Key(1043走项目id): ${getFirebaseKey2(app_num)}")
                result.append("\nAES加密秘钥: ${key}")
                result.append("\n参数原始结果:\n${dcode_params?.toString()?:"原始结果解析出错,请检查服务器返回和appid是否对应"}")
                result.append("\n参数映射结果:\n${params_data.toString()}")
                //拿到aes加密结果
                val aes_params = AesUtils.encrypt(params_data.toString(), key, key)
                println("自定义参数AES加密结果结果->${aes_params.toString()}")
                result.append("\n参数AES加密结果(双击全选复制):\n${aes_params.toString()}")
                return result.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "操作异常\n:${e.printStackTrace()}"
        }
        return ""
    }

    /**
     * 获取新服务器后台自定义参数, 映射版本
     */
    fun getNewToolParamsData(orderId: String): String {
        try {
            val res = get("https://sunny.careduka.com/admin-api/work_order/app_detail/$orderId")
            val data = Json.parseToJsonElement(res).jsonObject.get("data")!!.jsonObject
            val appId = data.get("app_id")?.jsonPrimitive?.content
            val ver = data.get("app_version")?.jsonPrimitive?.content
            val cha = data.get("app_source")?.jsonPrimitive?.content
            val app_num = data.get("app_num")?.jsonPrimitive?.content
            val package_domain = data.get("package_domain")?.jsonPrimitive?.content

            println("需求单data->${data.toString()}")

            if (appId.isNullOrEmpty() || ver.isNullOrEmpty() || cha.isNullOrEmpty() || app_num.isNullOrEmpty() || package_domain.isNullOrEmpty()) {
                //参数有误
                return "需求单请求参数为空"
            } else {
                //参数正常,请求自定义配置
                val map = MappingManager.getAllMapWord(appId)
                //先把请求参数映射出来
                val apid_mp = map.get("apid")
                val ver_mp = map.get("ver")
                val p_id_mp = map.get("p_id")
                val cha_mp = map.get("cha")
                val fw_mp = map.get("fw")
                println("拿到的需求单请求参数映射: apid->$apid_mp,ver->$ver_mp,p_id->$p_id_mp,cha->$cha_mp,fw->$fw_mp")
                val url = "https://sunny.careduka.com/business-api/activity/parameters"
                val body =
                    "{\"$ver_mp\":\"$ver\",\"$p_id_mp\":\"$appId\",\"$cha_mp\":\"$cha\",\"$fw_mp\":\"1\"}"
                println("自定义参数请求地址->${url}")
                println("自定义参数请求参数->${body}")
                val paramsRes = MappingManager.post(url, body)
                println("自定义参数请求结果->${paramsRes}")
                //拿到所有自定义参数
                val params_data = try {
                    Json.parseToJsonElement(paramsRes).jsonObject.get("data")!!.jsonObject
                } catch (e: Exception) {
                    return "自定义参数拉取异常\n:${paramsRes}"
                }
                if (params_data.isEmpty()) {
                    return "自定义参数拉取异常\n:${paramsRes}"
                }
                val result = StringBuilder()
                println("自定义参数请求结果->${params_data.toString()}")
                //拿到所有的自定义参数解密映射
                val dcode_params = getMappingData(params_data, map)
                println("自定义参数解密结果->${dcode_params.toString()}")
                val key = AesUtils.getAesKey(appId, cha)
                result.append("\nfirebase Key(老板本走appid): ${getFirebaseKey(map)}")
                result.append("\nfirebase Key(1043走项目id): ${getFirebaseKey2(app_num)}")
                result.append("\nAES加密秘钥: ${key}")
                result.append("\n参数原始结果:\n${dcode_params?.toString()?:"原始结果解析出错,请检查服务器返回和appid是否对应"}")
                result.append("\n参数映射结果:\n${params_data.toString()}")
                //拿到aes加密结果
                val aes_params = AesUtils.encrypt(params_data.toString(), key, key)
                println("自定义参数AES加密结果结果->${aes_params.toString()}")
                result.append("\n参数AES加密结果(双击全选复制):\n${aes_params.toString()}")
                return result.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "操作异常\n:${e.printStackTrace()}"
        }
        return ""
    }

    private fun getMappingData(data: JsonObject, map: Map<String, String>): String? {
        val newMap = hashMapOf<String, String>()
        for (key in data.keys) {
            map.forEach {
                if (it.value == key) {
                    newMap.put(it.key, data.get(key)?.jsonPrimitive?.content ?: "null")
                }
            }
        }
        if(newMap.isEmpty()){
            return null
        }
        return Json.encodeToString(newMap)
    }

    /**
     * 获取firebaseKey,走appid
     * @param map Map<String, String>
     * @return String
     */
    private fun getFirebaseKey(map:Map<String,String>):String{
        return map.get("remote_config")!!
    }

    /**
     * 获取firebaseKey2,走项目id
     * 算法逻辑如下, 取a-z的ASCII码 97-122. 总共26个字符跨度. 由于项目id中数字是0-9, 无法平分26个字符跨度, 所以按一个数字2个ASCII跨度计算
     * @param map Map<String, String>
     * @return String
     */
    private fun getFirebaseKey2(appId:String):String{
        val newKey=StringBuilder()
        appId.toCharArray().forEach {
            val c=it.toString().toIntOrNull()?:0
            val asc=c*2+97
            newKey.append(asc.toChar())
        }
        println("1042版本新firebase key:${newKey.toString()}")
        return newKey.toString()
    }

    /**
     * 打开一个cmd窗口, 并执行命令
     * "python C:\\Users\\39568\\PycharmProjects\\git_bat\\cd_build.py 2.0.0.102.241218.2 40828004 1"
     * @param cmd String
     */
    fun openCmdWindow(cmd:String){
        try {
            // 启动 cmd 进程，打开一个新的命令行窗口
            val processBuilder = ProcessBuilder("cmd", "/c", "start","cmd","/k",cmd) // 使用 "start" 打开一个新的 cmd 窗口
            val process = processBuilder.start()
            // 读取并输出命令行窗口的输出
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                println(line)
            }

            process.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}