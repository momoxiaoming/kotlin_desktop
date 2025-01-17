package utils

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * AesUtils
 *
 * @author mmxm
 * @date 2024/3/6 11:38
 */
object AesUtils {




    /**
     * 根据项目id和渠道,得到密码和iv
     * @param appId String
     * @return String
     */
    fun getAesKey(appId:String,cha:String):String{
        var key="${appId}_${cha}"
        if (key.length<=16){
            val append= StringBuilder()
            for (i in 0 until (16-key.length)){
                append.append("0")
            }
            key="$key${append.toString()}"
        }else{
            //大于16位,截取前16位
            key= key.substring(0,16)
        }
        println("加密key:$key")

        return key
    }


    @OptIn(ExperimentalEncodingApi::class)
    fun encrypt(text: String, key: String, iv: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")
        val ivParameterSpec = IvParameterSpec(iv.toByteArray())
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        val encryptedBytes = cipher.doFinal(text.toByteArray())
        return Base64.encode(encryptedBytes)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun decrypt(encryptedText: String, key: String, iv: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val secretKeySpec = SecretKeySpec(key.toByteArray(), "AES")
        val ivParameterSpec = IvParameterSpec(iv.toByteArray())
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        val decryptedBytes = cipher.doFinal(Base64.decode(encryptedText.toByteArray()))
        return String(decryptedBytes)
    }
}