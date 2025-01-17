package mapping

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.nio.file.Paths
import java.time.Duration


/**
 * MappingManager
 *
 * @author mmxm
 * @date 2024/12/4 10:21
 */
object MappingManager {
    val client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(15000)).build()


    private val keyRuleCache = HashMap<String, KeymapsRule1.SeedKeymapSet>()

    fun obtainKeyRuleSet(appId: String): KeymapsRule1.SeedKeymapSet {
        val c = keyRuleCache.get(appId)
        if (c != null) {
            return c
        }

        return synchronized(keyRuleCache) {
            keyRuleCache.get(appId).let { rc ->
                if (rc != null) {
                    return@synchronized rc
                }
                KeymapsRule1.SeedKeymapSet(KeymapsRule1.seed(appId)).also { s ->
                    s.duplicateCallback = object : KeymapsRule1.SeedKeymapSet.OnDuplicateCallback {
                        override fun onDuplicateResult(key: String?, v: String?) {
                            throw Exception("映射重复 (${key}) -> (${v})")
                        }
                    }
                    keyRuleCache.put(appId, s)
                }
            }
        }
    }


    /**
     * 拿到所有自定义参数以及他的对应的映射
     * @param appId String
     * @return List<Map<String,String>>
     */
    fun getAllMapWord(appId: String): LinkedHashMap<String, String> {
        val keyRule = obtainKeyRuleSet(appId)  //拿到映射规则
        val keyMapContent = getKeyMapContent()  //拿到候选词
        return LinkedHashMap<String, String>(128).let {
            keyMapContent.forEach { item ->
                it[item] = keyRule.mapKeyRule1(item)!!
            }
            it
        }
    }

    /**
     * 拿到所有映射候选词
     * 默认在[https://ttt.careduka.com/rdd/config?apikeylist=1001]地址添加
     * @return List<String>
     */
    fun getKeyMapContent(): List<String> {
        val res = get("https://ttt.careduka.com/rdd/keymaps?cat=cfgapi")
        val list = res.trim().split("\n")
        return list
    }



    fun post(url: String, body: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .headers("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        val res = client.send(request, HttpResponse.BodyHandlers.ofString())
        return res.body()
    }

    fun get(url: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .headers("Content-Type", "application/json")
            .GET()
            .build()
        val res = client.send(request, HttpResponse.BodyHandlers.ofString())
        return res.body()
    }

    fun get2(url: String): String {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .setHeader("user-agent", "curl/7.68.0")
            .setHeader("accept", "*/*")
            .GET()
            .build()
        val res = client.send(request, HttpResponse.BodyHandlers.ofString())
        return res.body()
    }
}