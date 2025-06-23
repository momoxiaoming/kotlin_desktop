import ToolsManager.exportApp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.DragData
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.onExternalDrag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation.weight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import com.sun.tools.javac.jvm.ByteCodes.ret
import kotlinx.serialization.json.JsonNull.content
import mapping.MappingManager
import org.jetbrains.compose.ui.tooling.preview.Preview
import so_dex.DexSoManager
import so_dex.DexSoManager.DexVerData
import utils.AesUtils


@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    MaterialTheme {
        var fuckDoorState by remember { mutableStateOf(ToolsManager.getFuckDoorState()) }
        var bhState by remember { mutableStateOf(ToolsManager.getBhState()) }
        var modeInfoState by remember { mutableStateOf(ToolsManager.getPhoneMode()) }
        var verInfoState by remember { mutableStateOf(ToolsManager.getPhoneVersion()) }


        fun updateAll() {
            modeInfoState = ToolsManager.getPhoneMode()
            verInfoState = ToolsManager.getPhoneVersion()
            fuckDoorState = ToolsManager.getFuckDoorState()
            bhState = ToolsManager.getBhState()
        }

        fun updateDoor() {
            fuckDoorState = ToolsManager.getFuckDoorState()
        }

        fun updateBhState() {
            bhState = ToolsManager.getBhState()
        }
        Column(Modifier.padding(8.dp)) {
            //状态view
            statusView(modeInfoState, verInfoState, fuckDoorState, bhState) {
                updateAll()
            }
            updateAll()

            Spacer(modifier = Modifier.height(8.dp)) // 添加 8dp 的水平间距

            Row(Modifier.fillMaxWidth().height(120f.dp)) {
                //金手指
                logView(Modifier.weight(1f).fillMaxHeight().border(1f.dp, Color.Black), {
                    ToolsManager.openFuckDoor()
                    updateDoor()
                    updateBhState()
                }, {
                    ToolsManager.closeFuckDoor()
                    updateDoor()
                    updateBhState()
                })
                Spacer(modifier = Modifier.width(8.dp)) // 添加 8dp 的水平间距
                //查看签名
                signView(Modifier.weight(1f).fillMaxHeight().border(1f.dp, Color.Black).padding(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp)) // 添加 8dp 的水平间距

            Row(Modifier.fillMaxWidth().height(120f.dp)) {
                jarToDexView(Modifier.weight(1f).fillMaxHeight().border(1f.dp, Color.Black).padding(8.dp))
                Spacer(modifier = Modifier.width(8.dp)) // 添加 8dp 的水平间距
                exportApp(Modifier.weight(1f).fillMaxHeight().border(1f.dp, Color.Black).padding(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp)) // 添加 8dp 的水平间距

            Row(Modifier.fillMaxWidth().height(120f.dp)) {
                dumpActivityView(Modifier.weight(1f).fillMaxHeight().border(1f.dp, Color.Black))
                Spacer(modifier = Modifier.width(8.dp)) // 添加 8dp 的水平间距
                aesEncrypt(Modifier.weight(3f).fillMaxHeight().border(1f.dp, Color.Black))
            }
            Row(Modifier.fillMaxWidth().height(120f.dp)) {
                dexMemPublish(Modifier.weight(1f).fillMaxHeight().border(1f.dp, Color.Black))
            }
        }
    }
}


@Composable
fun dexMemPublish(modifier: Modifier) {
    var showInfoDialog by remember { mutableStateOf(false) }
    var ret by remember { mutableStateOf("") }
    val expanded = remember { mutableStateOf(false) }
    val expanded2 = remember { mutableStateOf(false) }

    var singleId by remember { mutableStateOf("") }

    val data = DexSoManager.getDexVerJson()
    val dex_items = data.list
    val dex_Type = data.sourceType

    val selectedDexVer = remember { mutableStateOf<DexSoManager.DexVerData>(dex_items.get(0)) }
    val selectedType = remember { mutableStateOf<String>(dex_Type.get(0)) }

    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.width(8.dp)) // 添加 8dp 的水平间距
        Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                singleId,
                onValueChange = {
                    singleId = it.replace("\\s".toRegex(), "")
                },
                placeholder = {
                    Text("项目id", style = TextStyle(fontSize = 15.sp))
                },
                modifier = Modifier.weight(1f).border(1.dp, Color.Black).height(50.dp),
                singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f).border(1.dp, Color.Black).height(50.dp)
                    .background(Color(224, 224, 224)),
                verticalArrangement = Arrangement.Center, // 垂直居中
                horizontalAlignment = Alignment.CenterHorizontally // 水平居中
            ) {
                // 1. 显示当前选中的项
                Text(
                    text = selectedDexVer.value.name,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    style = TextStyle(fontSize = 15.sp),
                    modifier = Modifier
                        .clickable { expanded.value = !expanded.value } // 点击文本来展开下拉菜单
                )
                // 2. 下拉菜单（DropdownMenu）
                DropdownMenu(
                    expanded = expanded.value,
                    onDismissRequest = { expanded.value = false }
                ) {
                    dex_items.forEach { item ->
                        DropdownMenuItem(onClick = {
                            selectedDexVer.value = item
                            expanded.value = false
                        }) {
                            Text(text = item.name)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f).border(1.dp, Color.Black).height(50.dp)
                    .background(Color(224, 224, 224)),
                verticalArrangement = Arrangement.Center, // 垂直居中
                horizontalAlignment = Alignment.CenterHorizontally // 水平居中
            ) {
                // 1. 显示当前选中的项
                Text(
                    text = selectedType.value,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    style = TextStyle(fontSize = 15.sp),
                    modifier = Modifier.fillMaxWidth()
                        .clickable { expanded2.value = !expanded2.value } // 点击文本来展开下拉菜单
                )
                // 2. 下拉菜单（DropdownMenu）
                DropdownMenu(
                    expanded = expanded2.value,
                    onDismissRequest = { expanded2.value = false }
                ) {
                    dex_Type.forEach { item ->
                        DropdownMenuItem(onClick = {
                            selectedType.value = item
                            expanded2.value = false
                        }) {
                            Text(text = item)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                if (selectedDexVer.value == null) {
                    ret = "没有选择dex版本"
                    showInfoDialog = true
                    return@Button
                }

                if (selectedType.value.isEmpty()) {
                    ret = "没有选择dex类型"
                    showInfoDialog = true
                    return@Button
                }
                if (singleId.isEmpty()) {
                    ret = "请输入项目id"
                    showInfoDialog = true
                    return@Button
                }
                DexSoManager.build(selectedDexVer.value, selectedType.value, singleId)
            }, content = {
                Text("编译mem版本")
            })
            Spacer(modifier = Modifier.width(8.dp))
        }

    }
    if (showInfoDialog) {
        inputTipsDialog("提示", ret) {
            showInfoDialog = false
        }
    }
}


/**
 * aes加密
 * @param modifier Modifier
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun aesEncrypt(modifier: Modifier) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        var oriderId by remember { mutableStateOf("") }
        var firebaseConfig by remember { mutableStateOf("") }
        val showWindow = remember { mutableStateOf(false) }


        var strContent by remember { mutableStateOf("") }
        var appId by remember { mutableStateOf("") }
        var showTips by remember { mutableStateOf("") }

        var showInfoDialog by remember { mutableStateOf(false) }
        var outputPath by remember { mutableStateOf("") }
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {

            Spacer(modifier = Modifier.width(8.dp))

            TextField(
                oriderId,
                onValueChange = {
                    oriderId = it.trim()
                },
                placeholder = {
                    Text("需求单号", style = TextStyle(fontSize = 15.sp))
                },
                modifier = Modifier.weight(1f).border(1.dp, Color.Black).height(50.dp),
                singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp)
            )

            Spacer(modifier = Modifier.width(8.dp))
            Button(modifier = Modifier.weight(1f), onClick = {
                if (oriderId.isEmpty()) {
                    firebaseConfig = "请输入需求单id"
                    return@Button
                }
//                println(MappingManager.post("https://sunny.careduka.com/user-api/blacklist/check",""))
//
//                println(MappingManager.get2("https://www.ipinfo.io"))

                firebaseConfig = ToolsManager.getWbToolParamsData(oriderId)
                showWindow.value = firebaseConfig.isNotEmpty()

            }, content = {
                Text("拉旧中台配置")
            })
            Spacer(modifier = Modifier.width(8.dp))

            Button(modifier = Modifier.weight(1f), onClick = {
                if (oriderId.isEmpty()) {
                    firebaseConfig = "请输入需求单id"
                    return@Button
                }
//                println(MappingManager.post("https://sunny.careduka.com/user-api/blacklist/check",""))
//
//                println(MappingManager.get2("https://www.ipinfo.io"))

                firebaseConfig = ToolsManager.getNewToolParamsData(oriderId)
                showWindow.value = firebaseConfig.isNotEmpty()

            }, content = {
                Text("拉新中台配置")
            })
            Spacer(modifier = Modifier.width(8.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
//            Spacer(modifier = Modifier.width(8.dp))
//            TextField(appId, onValueChange = {
//                appId = it.trim()
//            }, placeholder = {
//                Text("需求单号", style = TextStyle(fontSize = 15.sp))
//            }, modifier= Modifier.weight(1f).border(1.dp, Color.Black).height(50.dp),singleLine = true, textStyle = TextStyle(fontSize = 15.sp))

            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                strContent,
                onValueChange = {
                    strContent = it.replace("\\s".toRegex(), "")
                },
                placeholder = {
                    Text("加解密字符串", style = TextStyle(fontSize = 15.sp))
                },
                modifier = Modifier.weight(1f).border(1.dp, Color.Black).height(50.dp),
                singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Row(modifier = Modifier.weight(1f)) {
                Button(modifier = Modifier.weight(1f), onClick = {
                    val content = ToolsManager.encodeContent(oriderId, strContent)
                    showTips = content
                }, content = {
                    Text("AES加密")
                })
                Spacer(modifier = Modifier.width(8.dp))

                Button(modifier = Modifier.weight(1f), onClick = {
                    if (oriderId.isEmpty() || strContent.isEmpty()) {
                        showTips = "需求单id为空或者加密字符串为空"
                        return@Button
                    }
                    val ret = ToolsManager.decodeContent(oriderId, strContent)
                    showTips = ret
                }, content = {
                    Text("AES解密")
                })

            }

            Spacer(modifier = Modifier.width(8.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (showWindow.value) {
            newWindow(showWindow, firebaseConfig)
        }

        if (showTips.isNotEmpty()) {
            inputTipsDialog("提示", showTips) {
                showTips = ""
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun exportApp(modifier: Modifier) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        var pkgInfo by remember { mutableStateOf("") }

        var showInfoDialog by remember { mutableStateOf(false) }
        var outputPath by remember { mutableStateOf("") }


        Text("导出apk")

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {

            TextField(
                pkgInfo,
                onValueChange = {
                    pkgInfo = it
                },
                placeholder = {
                    Text("输入包名", style = TextStyle(fontSize = 15.sp))
                },
                modifier = Modifier.weight(1f).border(1.dp, Color.Black).height(50.dp),
                singleLine = true,
                textStyle = TextStyle(fontSize = 15.sp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                val ret = ToolsManager.exportApp(pkgInfo)
                if (ret.isNotEmpty()) {
                    outputPath = ret
                    showInfoDialog = true
                }
            }, content = {
                Text("确定")
            })
        }
        Spacer(modifier = Modifier.height(8.dp))

        if (showInfoDialog) {
            inputTipsDialog("提示", "导出成功\n $outputPath") {
                showInfoDialog = false
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun jarToDexView(modifier: Modifier) {
    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        var jarPath by remember { mutableStateOf("") }
        var outputPath by remember { mutableStateOf("") }

        var showInfoDialog by remember { mutableStateOf(false) }


        Text("JAR转DEX")

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            val text_modifier = Modifier.weight(1f).border(1.dp, Color.Black).height(50.dp).onExternalDrag(
                onDrop = { externalDragValue -> //监听鼠标文件拖动事件, 并取出文件path
                    val dragData = externalDragValue.dragData
                    if (dragData is DragData.FilesList) {
                        val urlPath = dragData.readFiles().map { it.removePrefix("file:/") }.first()
                        jarPath = urlPath
                    }
                },
            )

            TextField(jarPath, onValueChange = {
                jarPath = it
            }, placeholder = {
                Text("输入jar路径", style = TextStyle(fontSize = 15.sp))
            }, modifier = text_modifier, singleLine = true, textStyle = TextStyle(fontSize = 15.sp))

            Spacer(modifier = Modifier.width(8.dp))

            Button(onClick = {
                val ret = ToolsManager.jarToDex(jarPath)
                outputPath = ret
                showInfoDialog = true
            }, content = {
                Text("转换")
            })
        }
        Spacer(modifier = Modifier.height(8.dp))


        if (showInfoDialog) {
            inputTipsDialog("提示", "转换成功\n $outputPath") {
                showInfoDialog = false
            }
        }
    }

}

@Composable
fun dumpActivityView(modifier: Modifier) {
    var showInfoDialog by remember { mutableStateOf(false) }
    var actInfo by remember { mutableStateOf("") }

    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("..App Activity栈信息")
        Spacer(modifier = Modifier.width(8.dp)) // 添加 8dp 的水平间距
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Button(onClick = {
                val ret = ToolsManager.dumpActivity()
                println("堆栈:$ret")
                if (ret.isNotEmpty()) {
                    showInfoDialog = true
                    actInfo = ret
                }
            }, content = {
                Text("查看")
            })
        }

    }
    if (showInfoDialog) {
        inputTipsDialog("堆栈", actInfo) {
            showInfoDialog = false
        }
    }
}


@Composable
fun logView(modifier: Modifier, openBlock: () -> Unit, closeBlock: () -> Unit) {

    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("金手指+日志+bh解限")
        Spacer(modifier = Modifier.width(8.dp)) // 添加 8dp 的水平间距
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Button(onClick = openBlock, content = {
                Text("开启")
            })
            Button(onClick = closeBlock, content = {
                Text("关闭")
            })
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun signView(modifier: Modifier) {
    var showTips by remember { mutableStateOf(false) }

    var showInfoDialog by remember { mutableStateOf(false) }

    var signInfo by remember { mutableStateOf("") }

    Column(
        modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        var apkPath by remember { mutableStateOf("") }
        val modifier = Modifier.fillMaxWidth().border(1.dp, Color.Black).onExternalDrag(
            onDrop = { externalDragValue -> //监听鼠标文件拖动事件, 并取出文件path
                val dragData = externalDragValue.dragData
                if (dragData is DragData.FilesList) {
                    val urlPath = dragData.readFiles().map { it.removePrefix("file:/") }.first()
                    apkPath = urlPath
                }
            },
        )
        TextField(apkPath, onValueChange = {
            apkPath = it
        }, placeholder = {
            Text("输入apk,aab路径")
        }, modifier = modifier, singleLine = true, textStyle = TextStyle())

        Button(onClick = {
            if (apkPath.isEmpty() || !(apkPath.endsWith(".apk") || apkPath.endsWith(".aab"))) {
                showTips = true
            } else {
                showTips = false
                val ret = ToolsManager.lookJks(apkPath)
                println("签名信息:$ret")
                signInfo = ret
                showInfoDialog = true
            }
        }, content = {
            Text("查看签名")
        })
        if (showTips) {
            inputTipsDialog("提示", "输入不合法") {
                showTips = false
            }
        }

        if (showInfoDialog) {
            inputTipsDialog("签名信息", signInfo) {
                showInfoDialog = false
            }
        }
    }
}

@Composable
fun newWindow(showWindow: MutableState<Boolean>, msg: String) {
    Window(onCloseRequest = { showWindow.value = false }, title = "提示") {
        Box(modifier = Modifier.fillMaxWidth().padding(10.dp)) {
            SelectionContainer() {
                Text(
                    msg, color = Color.Black, fontSize = 10.sp, modifier = Modifier
                        .fillMaxWidth() // 占满父容器
                        .verticalScroll(rememberScrollState())
                )
            }
        }
    }
}

@Composable
fun inputTipsDialog(title: String = "提示", content: String, action: () -> Unit) {
    AlertDialog(onDismissRequest = {

    }, buttons = {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                action.invoke()
            }, content = {
                Text("确定")
            })
        }
    }, title = {
        Text(title)
    }, text = {
        Box(modifier = Modifier.width(600.dp)) {
            SelectionContainer() {
                Text(
                    content, color = Color.Black, fontSize = 10.sp, modifier = Modifier
                        .fillMaxSize() // 占满父容器
                        .verticalScroll(rememberScrollState())
                )
            }
        }

    })
}


@Composable
fun statusView(
    modeInfoState: String,
    verInfoState: String,
    fuckDoorState: Boolean,
    bhState: Boolean,
    update: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().border(1f.dp, Color.Black).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("状态信息", style = MaterialTheme.typography.body1)
            Text("(刷新) ", modifier = Modifier.clickable {

                update.invoke()
            }, style = TextStyle(color = Color.Blue))
        }
        Row {
            Column(
                Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "型号: ",
                        textAlign = TextAlign.End,
                        style = TextStyle(color = Color.Black),
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // 添加 8dp 的水平间距

                    Text(
                        modifier = Modifier.weight(1f),
                        text = modeInfoState,
                        style = TextStyle(color = Color.Green)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End,
                        text = "版本: ",
                        style = TextStyle(color = Color.Black)
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // 添加 8dp 的水平间距

                    Text(
                        modifier = Modifier.weight(1f),
                        text = verInfoState,
                        style = TextStyle(color = Color.Green)
                    )
                }
            }
            Column(
                Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "金手指: ",
                        textAlign = TextAlign.End,
                        style = TextStyle(color = Color.Black)
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // 添加 8dp 的水平间距

                    Text(
                        modifier = Modifier.weight(1f),
                        text = if (fuckDoorState) "已开启" else "未开启",
                        style = TextStyle(color = if (fuckDoorState) Color.Green else Color.Red)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "bh解限: ",
                        textAlign = TextAlign.End,
                        style = TextStyle(color = Color.Black)
                    )
                    Spacer(modifier = Modifier.width(8.dp)) // 添加 8dp 的水平间距

                    Text(
                        modifier = Modifier.weight(1f),
                        text = if (bhState) "已开启" else "未开启",
                        style = TextStyle(color = if (bhState) Color.Green else Color.Red)
                    )
                }

            }
        }
    }
}