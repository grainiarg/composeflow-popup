package com.example.mydemo.retention

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import mydemo.shared.generated.resources.Res
import mydemo.shared.generated.resources.bg_dialog
import mydemo.shared.generated.resources.vip
import mydemo.shared.generated.resources.image4
import mydemo.shared.generated.resources.image3
import mydemo.shared.generated.resources.image2
import mydemo.shared.generated.resources.image1
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun RetentionScreen(viewModel: RetentionViewModel, onDismiss: () -> Unit = {}) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box {
        when (val s = state) {
            is RetentionUiState.Loading -> LoadingContent()
            is RetentionUiState.Error -> ErrorContent(s.errors)
            is RetentionUiState.Success -> DslRenderer(
                node = s.rootNode,
                dataContext = viewModel.dataContext,
                onEvent = { eventStr ->
                    when {
                        eventStr == "dismiss" -> onDismiss()
                        eventStr.startsWith("track:") -> {
                            val eventName = eventStr.removePrefix("track:")
                            println("[Track] $eventName")
                        }
                        eventStr.startsWith("toast:") -> {
                            val message = eventStr.removePrefix("toast:")
                            scope.launch { snackbarHostState.showSnackbar(message) }
                        }
                        else -> println("[Event] $eventStr")
                    }
                },
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ============================================================
// 加载 & 错误
// ============================================================

@Composable
private fun LoadingContent() {
    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(errors: List<String>) {
    Column(Modifier.fillMaxWidth().padding(24.dp)) {
        Text("DSL 配置异常：", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F.toInt()))
        errors.forEach { err -> Text("- $err", color = Color(0xFFD32F2F.toInt())) }
    }
}

// ============================================================
// 渲染引擎
// ============================================================

@Composable
fun DslRenderer(
    node: DslNode,
    dataContext: DataContext,
    onEvent: (String) -> Unit = {},
) {
    when (node) {
        is DialogNode -> RenderDialog(node, dataContext, onEvent)
        is TextNode -> RenderText(node, dataContext)
        is ImageNode -> RenderImage(node)
        is ButtonNode -> RenderButton(node, dataContext, onEvent)
        is ColumnNode -> RenderColumn(node, dataContext, onEvent)
        is RowNode -> RenderRow(node, dataContext, onEvent)
        is BoxNode -> RenderBox(node, dataContext, onEvent)
    }
}

// ============================================================
// Dialog
// ============================================================

@Composable
private fun RenderDialog(
    node: DialogNode,
    dataContext: DataContext,
    onEvent: (String) -> Unit,
) {
    val overlay = node.props.overlay
    val overlayColor = overlay?.let {
        val base = parseHexColor(it.backgroundColor) ?: 0xFF000000.toInt()
        Color(base).copy(alpha = it.opacity)
    }

    val cardWidth = node.props.width.parseDp()?.dp ?: 315.dp
    val cardRadius = node.props.cornerRadius?.parseDp()?.dp
    val cardBg = node.props.backgroundColor?.let {
        if (it == "transparent") Color.Transparent else parseHexColor(it)?.let { c -> Color(c) }
    }
    val bgImage = node.props.backgroundImage?.let { imageResourceFor(it) }

    val cardContent: @Composable () -> Unit = {
        Box(
            modifier = Modifier
                .width(cardWidth)
                .then(cardRadius?.let { Modifier.clip(RoundedCornerShape(it)) } ?: Modifier)
                .then(cardBg?.let { Modifier.background(it) } ?: Modifier),
        ) {
            if (bgImage != null) {
                Image(
                    painter = painterResource(bgImage),
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.Fit,
                )
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                node.children.forEach { child ->
                    DslRenderer(child, dataContext, onEvent)
                }
            }
        }
    }

    if (overlayColor != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(overlayColor)
                .then(
                    if (node.props.cancelable) {
                        Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { onEvent("dismiss") }
                    } else Modifier
                ),
            contentAlignment = Alignment.Center,
        ) {
            cardContent()
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            cardContent()
        }
    }
}

// ============================================================
// 基础组件
// ============================================================

@Composable
private fun RenderText(node: TextNode, dataContext: DataContext) {
    Text(
        text = dataContext.resolve(node.props.text),
        fontSize = node.props.fontSize?.parseSp()?.sp ?: 14.sp,
        color = node.props.color?.let { parseHexColor(it)?.let { c -> Color(c) } } ?: Color.Unspecified,
        fontWeight = parseFontWeight(node.props.fontWeight),
        textAlign = parseTextAlign(node.props.textAlign),
        maxLines = node.props.maxLines ?: Int.MAX_VALUE,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .then(node.props.padding.toSpacingModifier())
            .then(node.props.margin.toSpacingModifier()),
    )
}

@Composable
private fun RenderImage(node: ImageNode) {
    val radius = node.props.cornerRadius?.parseDp()?.dp ?: 0.dp
    val width = node.props.width?.parseDp()?.dp ?: 48.dp
    val height = node.props.height?.parseDp()?.dp ?: 48.dp
    val resource = imageResourceFor(node.props.src)

    if (resource != null) {
        Image(
            painter = painterResource(resource),
            contentDescription = null,
            modifier = Modifier
                .size(width, height)
                .clip(RoundedCornerShape(radius)),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = Modifier
                .size(width, height)
                .clip(RoundedCornerShape(radius))
                .background(Color(0xFFE0E0E0.toInt())),
            contentAlignment = Alignment.Center,
        ) {
            Text("🖼", fontSize = 20.sp)
        }
    }
}

private fun imageResourceFor(src: String): DrawableResource? = when (src) {
    "vip" -> Res.drawable.vip
    "bg_dialog" -> Res.drawable.bg_dialog
    "image1" -> Res.drawable.image1
    "image2" -> Res.drawable.image2
    "image3" -> Res.drawable.image3
    "image4" -> Res.drawable.image4
    else -> null
}

@Composable
private fun RenderButton(
    node: ButtonNode,
    dataContext: DataContext,
    onEvent: (String) -> Unit,
) {
    val bgColor = node.props.backgroundColor?.let {
        if (it == "transparent") Color.Transparent else parseHexColor(it)?.let { c -> Color(c) }
    } ?: Color.Unspecified

    val contentColor = node.props.textColor?.let { parseHexColor(it)?.let { c -> Color(c) } } ?: Color.Unspecified

    val radius = node.props.cornerRadius?.parseDp()?.dp?.let { RoundedCornerShape(it) } ?: ButtonDefaults.shape

    val hPadding = node.props.paddingHorizontal?.parseDp()?.dp ?: 0.dp
    val vPadding = node.props.paddingVertical?.parseDp()?.dp ?: 0.dp

    val widthModifier = when {
        node.props.width == "fillMaxWidth" -> Modifier.fillMaxWidth()
        node.props.width != null -> {
            val w = node.props.width!!.parseDp()?.dp
            val h = node.props.height?.parseDp()?.dp
            if (w != null && h != null) Modifier.size(width = w, height = h)
            else if (w != null) Modifier.width(w)
            else Modifier
        }
        else -> Modifier
    }

    Button(
        onClick = { node.events?.onClick?.let { onEvent(it) } },
        modifier = widthModifier.padding(horizontal = hPadding, vertical = vPadding),
        shape = radius,
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            contentColor = contentColor,
        ),
    ) {
        Text(
            text = dataContext.resolve(node.props.text),
            fontSize = node.props.fontSize?.parseSp()?.sp ?: 14.sp,
        )
    }
}

// ============================================================
// 容器组件
// ============================================================

@Composable
private fun RenderColumn(
    node: ColumnNode,
    dataContext: DataContext,
    onEvent: (String) -> Unit,
) {
    val bgColor = node.containerProps.backgroundColor?.let { parseHexColor(it)?.let { c -> Color(c) } }
    val gap = node.containerProps.gap?.parseDp()?.dp

    Column(
        modifier = Modifier
            .then(bgColor?.let { Modifier.background(it) } ?: Modifier)
            .then(node.containerProps.padding.toSpacingModifier())
            .then(node.containerProps.margin.toSpacingModifier())
            .fillMaxWidth(),
        horizontalAlignment = parseAlignItems(node.containerProps.alignItems),
        verticalArrangement = parseJustifyContent(node.containerProps.justifyContent),
    ) {
        node.children.forEachIndexed { index, child ->
            DslRenderer(child, dataContext, onEvent)
            if (gap != null && index < node.children.lastIndex) {
                Spacer(Modifier.size(gap))
            }
        }
    }
}

@Composable
private fun RenderRow(
    node: RowNode,
    dataContext: DataContext,
    onEvent: (String) -> Unit,
) {
    val bgColor = node.containerProps.backgroundColor?.let { parseHexColor(it)?.let { c -> Color(c) } }
    val gap = node.containerProps.gap?.parseDp()?.dp

    Row(
        modifier = Modifier
            .then(bgColor?.let { Modifier.background(it) } ?: Modifier)
            .then(node.containerProps.padding.toSpacingModifier())
            .then(node.containerProps.margin.toSpacingModifier())
            .fillMaxWidth(),
        horizontalArrangement = parseRowJustify(node.containerProps.justifyContent),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        node.children.forEachIndexed { index, child ->
            DslRenderer(child, dataContext, onEvent)
            if (gap != null && index < node.children.lastIndex) {
                Spacer(Modifier.size(gap))
            }
        }
    }
}

@Composable
private fun RenderBox(
    node: BoxNode,
    dataContext: DataContext,
    onEvent: (String) -> Unit,
) {
    val bgColor = node.containerProps.backgroundColor?.let { parseHexColor(it)?.let { c -> Color(c) } }

    Box(
        modifier = Modifier
            .then(bgColor?.let { Modifier.background(it) } ?: Modifier)
            .then(node.containerProps.padding.toSpacingModifier())
            .then(node.containerProps.margin.toSpacingModifier()),
        contentAlignment = parseBoxAlign(node.containerProps.alignItems),
    ) {
        node.children.forEach { child -> DslRenderer(child, dataContext, onEvent) }
    }
}

// ============================================================
// 样式映射
// ============================================================

private fun parseFontWeight(value: String?): FontWeight? = when (value) {
    "normal", "400" -> FontWeight.Normal
    "bold", "700" -> FontWeight.Bold
    "light", "300" -> FontWeight.Light
    "medium", "500" -> FontWeight.Medium
    else -> null
}

private fun parseTextAlign(value: String?): TextAlign? = when (value) {
    "left" -> TextAlign.Left
    "center" -> TextAlign.Center
    "right" -> TextAlign.Right
    else -> null
}

private fun parseAlignItems(value: String?): Alignment.Horizontal = when (value) {
    "start" -> Alignment.Start
    "end" -> Alignment.End
    else -> Alignment.CenterHorizontally
}

private fun parseJustifyContent(value: String?): Arrangement.Vertical = when (value) {
    "top" -> Arrangement.Top
    "center" -> Arrangement.Center
    "bottom" -> Arrangement.Bottom
    "spaceBetween" -> Arrangement.SpaceBetween
    else -> Arrangement.Top
}

private fun parseRowJustify(value: String?): Arrangement.Horizontal = when (value) {
    "start" -> Arrangement.Start
    "end" -> Arrangement.End
    "center" -> Arrangement.Center
    "spaceBetween" -> Arrangement.SpaceBetween
    else -> Arrangement.Start
}

private fun parseBoxAlign(value: String?): Alignment = when (value) {
    "start" -> Alignment.TopStart
    "end" -> Alignment.TopEnd
    else -> Alignment.Center
}

private fun SpacingProps?.toSpacingModifier(): Modifier {
    val s = this ?: return Modifier
    var mod: Modifier = Modifier
    s.top?.parseDp()?.dp?.let { mod = mod.padding(top = it) }
    s.bottom?.parseDp()?.dp?.let { mod = mod.padding(bottom = it) }
    s.left?.parseDp()?.dp?.let { mod = mod.padding(start = it) }
    s.right?.parseDp()?.dp?.let { mod = mod.padding(end = it) }
    return mod
}
