import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * CmdUtils
 *
 * @author mmxm
 * @date 2024/7/11 10:40
 */
object CmdUtils {

    const val OE="output_exception"
    /**
     * 执行非root命令
     * @param command
     * @return
     */
    fun execCmdSilent(command: String): String {
        var process: Process? = null
        try {
            process = Runtime.getRuntime().exec(command)
            return process.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }.replaceAfterLast("\r\n","")
        } catch (e: IOException) {
            e.printStackTrace()
            return OE
        }
    }
}