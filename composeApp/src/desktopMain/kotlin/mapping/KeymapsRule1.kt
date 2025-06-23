package mapping

import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import java.util.Locale
import java.util.TreeMap

/**
 * KeymapsRule1
 *
 * @author mmxm
 * @date 2024/12/4 10:38
 */
object KeymapsRule1 {
    fun hashInt1(s: String): Int {
        val k: ByteString = s.encodeUtf8().md5()
        val kBytes = k.toByteArray()
        val rBytes = ByteArray(4)
        rBytes[0] =
            ((kBytes[0].toInt() xor kBytes[4].toInt() xor kBytes[8].toInt() xor kBytes[12].toInt()) and 0xFF and 0x7F).toByte()
        rBytes[1] =
            ((kBytes[1].toInt() xor kBytes[5].toInt() xor kBytes[9].toInt() xor kBytes[13].toInt()) and 0xFF).toByte()
        rBytes[2] =
            ((kBytes[2].toInt() xor kBytes[6].toInt() xor kBytes[10].toInt() xor kBytes[14].toInt()) and 0xFF).toByte()
        rBytes[3] =
            ((kBytes[3].toInt() xor kBytes[7].toInt() xor kBytes[11].toInt() xor kBytes[15].toInt()) and 0xFF).toByte()

        return ((rBytes[0].toInt() shl 24) and -0x1000000) or
                ((rBytes[1].toInt() shl 16) and 0x00FF0000) or
                ((rBytes[2].toInt() shl 8) and 0x0000FF00) or
                (rBytes[3].toInt() and 0x000000FF)
    }

    fun seed(pkg: String): String {
        return pkg.encodeUtf8().md5().hex()
    }

    /**
     * 规则1
     *
     * @param key
     * @param seedHash [seed]
     * @return
     */
    fun mapKeyRule1(key: String, seedHash: String): String {
        // 规则
        // 1. seed = hex(md5(pkg))
        // 2. k1 = md5(原key + seed)  // 原始byte数组，不转hex字符串
        // 3. r1 = base64(k1)
        // 4. r2 = 在r1里面取前4个，纯字母的字符（跳过数字和特殊字符）。如果前面跳过了N个非字母的字符，则在取N个纯字母添加在前面的4个字母后面
        // 5. 最终的结果， r = lowercase(r2)

        val k1: ByteString = (key + seedHash).encodeUtf8().md5()
        val r1 = k1.base64()

        val firstN = 4
        val sb = StringBuilder()
        var skiped = 0

        val chars = r1.toCharArray()
        for (c in chars) {
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                if (sb.length < firstN + skiped) {
                    sb.append(c)
                } else {
                    break
                }
            } else if (sb.length < firstN) {
                skiped++
            }
        }

        return sb.toString().lowercase(Locale.getDefault())
    }

    /**
     * 验证这个kv对是不是原始key[originKey]生成的seed
     *
     * @param key
     * @param value 原始seed
     * @return null: 不是想要的kv； key匹配[originKey]时，返回[value]
     */
    fun validateSeed(key: String?, value: String?, originKey: String): String? {
        if (key == null || key.isEmpty()) {
            return null
        }
        if (value == null || value.isEmpty()) {
            return null
        }
        val s = seed(value)
        val k1 = mapKeyRule1(originKey, s)
        if (k1 == key) {
            return value
        }
        return null
    }

    /**
     * 扩充同一个seed的keymap
     */

    class SeedKeymapSet(val seed: String) {
        val ruleVersion: Int = 1
        private val keymaps = HashMap<String, String?>(64)
        private val mapsCount = TreeMap<String?, Int>()

        var duplicateCallback: OnDuplicateCallback? = null

        fun mapKeyRule1(key: String): String? {
            var v = keymaps[key]
            if (v != null && !v.isEmpty()) {
                return v
            }
            synchronized(keymaps) {
                v = mapKeyRule1(key, seed)
                keymaps[key] = v
                // assert
                var count = mapsCount[v]
                if (count == null) {
                    count = 1
                } else {
                    count = count + 1
                    // warning： 重复的结果
                    onDuplicateResult(key, v)
                }
                mapsCount.put(v, count)
            }
            return v
        }

        private fun onDuplicateResult(key: String, v: String?) {
            println("Warning: $key, get a duplicate result [$v]")
            if (duplicateCallback != null) {
                duplicateCallback!!.onDuplicateResult(key, v)
            }
        }

        interface OnDuplicateCallback {
            fun onDuplicateResult(key: String?, v: String?)
        }
    }
}