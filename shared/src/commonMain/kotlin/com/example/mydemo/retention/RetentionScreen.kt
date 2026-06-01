package com.example.mydemo.retention

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mydemo.shared.generated.resources.Res
import mydemo.shared.generated.resources.bg_dialog
import mydemo.shared.generated.resources.vip
import mydemo.shared.generated.resources.image4
import mydemo.shared.generated.resources.image5
import mydemo.shared.generated.resources.image6
import mydemo.shared.generated.resources.image7
import mydemo.shared.generated.resources.image8
import mydemo.shared.generated.resources.image3
import mydemo.shared.generated.resources.image2
import mydemo.shared.generated.resources.image1
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun RetentionScreen(viewModel: RetentionViewModel, onDismiss: () -> Unit = {}) {
    val state by viewModel.uiState.collectAsState()
    val currentIdx by viewModel.currentIndex.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // 主内容区
        Box(modifier = Modifier.weight(1f)) {
            when (val s = state) {
                is RetentionUiState.Loading -> LoadingContent()
                is RetentionUiState.Error -> {
                    var dismissed by remember { mutableStateOf(false) }
                    if (!dismissed) {
                        Box(Modifier.fillMaxSize()) {
                            ErrorContent(
                                errors = s.errors,
                                onDismiss = { dismissed = true },
                                modifier = Modifier.align(Alignment.TopEnd),
                            )
                        }
                    }
                }
                is RetentionUiState.Success -> DslRenderer(
                    node = s.rootNode,
                    dataContext = viewModel.dataContext,
                    onEvent = { event ->
                        when (event) {
                            is UiEvent.Toast -> scope.launch { snackbarHostState.showSnackbar(event.msg) }
                            is UiEvent.Navigate -> {
                                if (event.closeDialog) onDismiss()
                                PlatformUtil.navigate(event.route)
                            }
                            is UiEvent.Track -> PlatformUtil.track(event.eventId, event.extra)
                            is UiEvent.Dismiss -> onDismiss()
                        }
                    },
                )
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
        // Tab 切换栏
        val validSamples = viewModel.samples.filter { !it.isErrorDemo }
        val errorSamples = viewModel.samples.filter { it.isErrorDemo }
        Column(modifier = Modifier.fillMaxWidth()) {
            // 合法页面
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                validSamples.forEach { sample ->
                    val idx = viewModel.samples.indexOf(sample)
                    SampleTab(sample.name, isSelected = idx == currentIdx) { viewModel.switchSample(idx) }
                }
            }
            // 错误演示
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "错误演示",
                    fontSize = 11.sp,
                    color = Color(0xFF999999.toInt()),
                )
                errorSamples.forEach { sample ->
                    val idx = viewModel.samples.indexOf(sample)
                    SampleTab(sample.name, isSelected = idx == currentIdx) { viewModel.switchSample(idx) }
                }
            }
        }
    }
}

@Composable
private fun SampleTab(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Text(
        text = name,
        fontSize = 12.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        color = if (isSelected) Color(0xFFFE315D.toInt()) else Color(0xFF666666.toInt()),
        modifier = Modifier
            .background(
                color = if (isSelected) Color(0x1AFE315D) else Color(0xFFF5F5F5.toInt()),
                shape = RoundedCornerShape(6.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
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
private fun ErrorContent(errors: List<String>, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(16.dp)
            .widthIn(max = 340.dp)
            .heightIn(max = 400.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "DSL 配置异常（${errors.size} 条）",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F.toInt()),
            )
            Text(
                "✕",
                fontSize = 18.sp,
                color = Color(0xFF999999.toInt()),
                modifier = Modifier.clickable { onDismiss() },
            )
        }
        Spacer(Modifier.size(8.dp))
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            errors.forEach { err ->
                val bracketEnd = err.indexOf("] ")
                val (path, message) = if (bracketEnd > 0) {
                    err.substring(1, bracketEnd) to err.substring(bracketEnd + 2)
                } else {
                    "?" to err
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F0.toInt())),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Column(Modifier.padding(10.dp)) {
                        Text(
                            path,
                            fontSize = 11.sp,
                            color = Color(0xFF999999.toInt()),
                        )
                        Text(
                            message,
                            fontSize = 13.sp,
                            color = Color(0xFFD32F2F.toInt()),
                        )
                    }
                }
            }
        }
    }
}

// ============================================================
// 渲染引擎
// ============================================================

@Composable
fun DslRenderer(
    node: DslNode,
    dataContext: DataContext,
    onEvent: (UiEvent) -> Unit = {},
) {
    when (node) {
        is DialogNode -> RenderDialog(node, dataContext, onEvent)
        is TextNode -> RenderText(node, dataContext)
        is AnimatedTextNode -> RenderAnimatedText(node, dataContext)
        is ImageNode -> RenderImage(node, dataContext)
        is ButtonNode -> RenderButton(node, dataContext, onEvent)
        is ColumnNode -> RenderColumn(node, dataContext, onEvent)
        is RowNode -> RenderRow(node, dataContext, onEvent)
        is BoxNode -> RenderBox(node, dataContext, onEvent)
        is CarouselNode -> RenderCarousel(node, dataContext, onEvent)
    }
}

// ============================================================
// Dialog
// ============================================================

@Composable
private fun RenderDialog(
    node: DialogNode,
    dataContext: DataContext,
    onEvent: (UiEvent) -> Unit,
) {
    val overlay = node.props.overlay
    val overlayColor = overlay?.let {
        val base = parseHexColor(it.backgroundColor) ?: 0xFF000000.toInt()
        Color(base).copy(alpha = it.opacity)
    }

    val cardRadius = node.props.cornerRadius?.parseDp()?.dp
    val cardBg = node.props.backgroundColor?.let { parseHexColor(it)?.let { c -> Color(c) } }
    val bgImage = node.props.backgroundImage?.let { imageResourceFor(it) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidthDp = maxWidth.value

        val cardWidth = node.props.width.parseDp(referenceDp = screenWidthDp)?.dp ?: 315.dp
        val maxW = node.props.maxWidth?.parseDp(referenceDp = screenWidthDp)?.dp
        val minW = node.props.minWidth?.parseDp(referenceDp = screenWidthDp)?.dp
        val finalWidth = when {
            maxW != null && cardWidth > maxW -> maxW
            minW != null && cardWidth < minW -> minW
            else -> cardWidth
        }

        val cardContent: @Composable () -> Unit = {
            Box(
                modifier = Modifier
                    .width(finalWidth)
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
                            ) { onEvent(UiEvent.Dismiss()) }
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
}

// ============================================================
// 基础组件
// ============================================================

@Composable
private fun RenderText(node: TextNode, dataContext: DataContext) {
    val noFontPadding = node.props.includeFontPadding == false
    Text(
        text = dataContext.resolve(node.props.text),
        fontSize = node.props.fontSize?.parseSp()?.sp ?: 14.sp,
        color = node.props.color?.let { parseHexColor(it)?.let { c -> Color(c) } } ?: Color.Unspecified,
        fontWeight = parseFontWeight(node.props.fontWeight),
        textAlign = parseTextAlign(node.props.textAlign),
        maxLines = node.props.maxLines ?: Int.MAX_VALUE,
        overflow = TextOverflow.Ellipsis,
        style = TextStyle(
            lineHeight = node.props.lineHeight?.parseSp()?.sp ?: TextUnit.Unspecified,
            lineHeightStyle = if (noFontPadding) LineHeightStyle(
                alignment = LineHeightStyle.Alignment.Proportional,
                trim = LineHeightStyle.Trim.Both,
            ) else null,
        ),
        modifier = Modifier
            .then(node.props.padding.toSpacingModifier())
            .then(node.props.margin.toSpacingModifier()),
    )
}

@Composable
private fun RenderAnimatedText(node: AnimatedTextNode, dataContext: DataContext) {
    val resolved = dataContext.resolve(node.props.text)
    val targetNumber = resolved.toFloatOrNull() ?: 0f
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetNumber) {
        animatedValue.animateTo(
            targetValue = targetNumber,
            animationSpec = tween(durationMillis = 1000),
        )
    }

    val display = animatedValue.value
    val textToShow = if (display % 1.0f == 0.0f) {
        display.toInt().toString()
    } else {
        val rounded = kotlin.math.round(display * 10) / 10
        val intPart = rounded.toInt()
        val decPart = ((rounded - intPart) * 10).toInt().let { if (it < 0) -it else it }
        "$intPart.$decPart"
    }

    Text(
        text = textToShow,
        fontSize = node.props.fontSize?.parseSp()?.sp ?: 14.sp,
        color = node.props.color?.let { parseHexColor(it)?.let { c -> Color(c) } } ?: Color.Unspecified,
        fontWeight = parseFontWeight(node.props.fontWeight),
        modifier = Modifier
            .then(node.props.padding.toSpacingModifier())
            .then(node.props.margin.toSpacingModifier()),
    )
}

@Composable
private fun RenderImage(node: ImageNode, dataContext: DataContext) {
    val radius = node.props.cornerRadius?.parseDp()?.dp ?: 0.dp
    val width = node.props.width?.parseDp()?.dp ?: 48.dp
    val height = node.props.height?.parseDp()?.dp ?: 48.dp
    val resolvedSrc = dataContext.resolve(node.props.src)
    val resource = imageResourceFor(resolvedSrc)

    if (resource != null) {
        Image(
            painter = painterResource(resource),
            contentDescription = null,
            modifier = Modifier
                .size(width, height)
                .clip(RoundedCornerShape(radius))
                .then(node.props.padding.toSpacingModifier())
                .then(node.props.margin.toSpacingModifier()),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = Modifier
                .size(width, height)
                .clip(RoundedCornerShape(radius))
                .background(Color(0xFFE0E0E0.toInt()))
                .then(node.props.padding.toSpacingModifier())
                .then(node.props.margin.toSpacingModifier()),
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
    "image5" -> Res.drawable.image5
    "image6" -> Res.drawable.image6
    "image7" -> Res.drawable.image7
    "image8" -> Res.drawable.image8
    else -> null
}

@Composable
private fun RenderButton(
    node: ButtonNode,
    dataContext: DataContext,
    onEvent: (UiEvent) -> Unit,
) {
    val bgColor = node.props.backgroundColor?.let { parseHexColor(it)?.let { c -> Color(c) } } ?: Color.Unspecified

    val contentColor = node.props.textColor?.let { parseHexColor(it)?.let { c -> Color(c) } } ?: Color.Unspecified

    val radius = node.props.cornerRadius?.parseDp()?.dp?.let { RoundedCornerShape(it) } ?: ButtonDefaults.shape

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
        onClick = { node.events?.click?.let { onEvent(it) } },
        modifier = widthModifier
            .then(node.props.padding.toSpacingModifier())
            .then(node.props.margin.toSpacingModifier()),
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
    onEvent: (UiEvent) -> Unit,
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
    onEvent: (UiEvent) -> Unit,
) {
    val bgColor = node.containerProps.backgroundColor?.let { parseHexColor(it)?.let { c -> Color(c) } }
    val gap = node.containerProps.gap?.parseDp()?.dp
    val isBaseline = node.containerProps.alignItems == "baseline"

    Row(
        modifier = Modifier
            .then(bgColor?.let { Modifier.background(it) } ?: Modifier)
            .then(node.containerProps.padding.toSpacingModifier())
            .then(node.containerProps.margin.toSpacingModifier())
            .fillMaxWidth(),
        horizontalArrangement = parseRowJustify(node.containerProps.justifyContent),
        verticalAlignment = parseRowVerticalAlign(node.containerProps.alignItems),
    ) {
        node.children.forEachIndexed { index, child ->
            if (isBaseline) {
                Box(Modifier.alignByBaseline()) { DslRenderer(child, dataContext, onEvent) }
            } else {
                DslRenderer(child, dataContext, onEvent)
            }
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
    onEvent: (UiEvent) -> Unit,
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
// 轮播组件
// ============================================================

@Composable
private fun RenderCarousel(
    node: CarouselNode,
    dataContext: DataContext,
    onEvent: (UiEvent) -> Unit,
) {
    val items = dataContext.resolveList(node.dataKey ?: return)
    val template = node.itemTemplate ?: return
    if (items.isEmpty()) return

    val itemPerPage = node.props.itemPerPage
    val actualPages = (items.size + itemPerPage - 1) / itemPerPage
    val autoPlay = node.props.autoPlay
    val loop = node.props.loop
    val interval = node.props.interval

    val pagerPageCount = if (loop) Int.MAX_VALUE else actualPages
    val initialPage = if (loop) pagerPageCount / 2 else 0
    // 对齐到实际页的起始位置，保证初始页为第0组数据
    val alignedStart = if (loop) initialPage - (initialPage % actualPages) else initialPage
    val pagerState = rememberPagerState(initialPage = alignedStart, pageCount = { pagerPageCount })

    if (autoPlay && actualPages > 1) {
        LaunchedEffect(pagerState) {
            while (true) {
                delay(interval)
                pagerState.animateScrollToPage(pagerState.currentPage + 1)
            }
        }
    }

    val heightModifier = node.props.height?.parseDp()?.dp?.let { Modifier.height(it) } ?: Modifier

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .then(heightModifier)
            .then(node.props.padding.toSpacingModifier())
            .then(node.props.margin.toSpacingModifier()),
    ) { page ->
        val actualPage = if (loop) page % actualPages else page
        val start = actualPage * itemPerPage
        val end = minOf(start + itemPerPage, items.size)
        val pageItems = items.subList(start, end)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            pageItems.forEach { item ->
                val itemContext = dataContext.withItem(item)
                DslRenderer(template, itemContext, onEvent)
            }
        }
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

private fun parseRowVerticalAlign(value: String?): Alignment.Vertical = when (value) {
    "start", "flex-start" -> Alignment.Top
    "end", "flex-end" -> Alignment.Bottom
    "center" -> Alignment.CenterVertically
    "baseline" -> Alignment.Top  // 基线对齐由子元素的 Modifier.alignByBaseline() 控制
    else -> Alignment.CenterVertically
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
