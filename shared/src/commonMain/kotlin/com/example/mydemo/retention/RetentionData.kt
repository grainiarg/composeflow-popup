package com.example.mydemo.retention

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal val dslJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
}

// 页面根模型

@Serializable
data class DslPage(
    val version: String = "1.0",
    val pageId: String = "",
    val content: DslNode = DialogNode(),
)

// Dialog

@Serializable
data class OverlayProps(
    val backgroundColor: String = "#000000",
    val opacity: Float = 0.6f,
)

@Serializable
data class DialogProps(
    val width: String = "85%",
    val maxWidth: String? = "360dp",
    val minWidth: String? = null,
    val backgroundColor: String? = null,
    val backgroundImage: String? = null,
    val cornerRadius: String = "16dp",
    val cancelable: Boolean = false,
    val overlay: OverlayProps? = null,
)

@Serializable
@SerialName("dialog")
data class DialogNode(
    override val id: String? = null,
    val props: DialogProps = DialogProps(),
    val children: List<DslNode> = emptyList(),
) : DslNode

// 间距模型

@Serializable
data class SpacingProps(
    val top: String? = null,
    val bottom: String? = null,
    val left: String? = null,
    val right: String? = null,
)

// 组件属性

@Serializable
data class TextProps(
    val text: String = "",
    val fontSize: String? = null,
    val color: String? = null,
    val fontWeight: String? = null,
    val textAlign: String? = null,
    val maxLines: Int? = null,
    val lineHeight: String? = null,
    val includeFontPadding: Boolean? = null,
    val padding: SpacingProps? = null,
    val margin: SpacingProps? = null,
)

@Serializable
data class AnimatedTextProps(
    val text: String = "",
    val fontSize: String? = null,
    val color: String? = null,
    val fontWeight: String? = null,
    val animate: String? = null,
    val animateFrom: String? = null,
    val padding: SpacingProps? = null,
    val margin: SpacingProps? = null,
)

@Serializable
data class ImageProps(
    val src: String = "",
    val width: String? = null,
    val height: String? = null,
    val cornerRadius: String? = null,
    val padding: SpacingProps? = null,
    val margin: SpacingProps? = null,
)

@Serializable
data class ButtonProps(
    val text: String = "",
    val fontSize: String? = null,
    val textColor: String? = null,
    val backgroundColor: String? = null,
    val cornerRadius: String? = null,
    val width: String? = null,
    val height: String? = null,
    val padding: SpacingProps? = null,
    val margin: SpacingProps? = null,
)

@Serializable
data class CarouselProps(
    val width: String? = null,
    val height: String? = null,
    val itemPerPage: Int = 4,
    val autoPlay: Boolean = false,
    val loop: Boolean = false,
    val interval: Long = 2000,
    val padding: SpacingProps? = null,
    val margin: SpacingProps? = null,
)

@Serializable
data class ContainerProps(
    val width: String? = null,
    val padding: SpacingProps? = null,
    val margin: SpacingProps? = null,
    val backgroundColor: String? = null,
    val alignItems: String? = null,
    val justifyContent: String? = null,
    val gap: String? = null,
)

// 事件

@Serializable
data class UiEvents(
    val click: UiEvent? = null,
    val show: UiEvent? = null,
)

@Serializable
sealed class UiEvent {
    abstract val type: String

    @Serializable
    @SerialName("toast")
    data class Toast(override val type: String = "toast", val msg: String) : UiEvent()

    @Serializable
    @SerialName("navigate")
    data class Navigate(override val type: String = "navigate", val route: String, val closeDialog: Boolean = false) : UiEvent()

    @Serializable
    @SerialName("track")
    data class Track(override val type: String = "track", val eventId: String, val extra: String = "") : UiEvent()

    @Serializable
    @SerialName("dismiss")
    data class Dismiss(override val type: String = "dismiss") : UiEvent()
}

// DSL 节点树

@Serializable
sealed interface DslNode {
    val id: String?
}

@Serializable
sealed interface DslContainerNode : DslNode {
    val containerProps: ContainerProps
    val children: List<DslNode>
}

@Serializable
@SerialName("text")
data class TextNode(
    override val id: String? = null,
    val props: TextProps = TextProps(),
) : DslNode

@Serializable
@SerialName("animatedText")
data class AnimatedTextNode(
    override val id: String? = null,
    val props: AnimatedTextProps = AnimatedTextProps(),
) : DslNode

@Serializable
@SerialName("image")
data class ImageNode(
    override val id: String? = null,
    val props: ImageProps = ImageProps(),
) : DslNode

@Serializable
@SerialName("button")
data class ButtonNode(
    override val id: String? = null,
    val props: ButtonProps = ButtonProps(),
    val events: UiEvents? = null,
) : DslNode

@Serializable
@SerialName("column")
data class ColumnNode(
    override val id: String? = null,
    @SerialName("props") override val containerProps: ContainerProps = ContainerProps(),
    override val children: List<DslNode> = emptyList(),
) : DslContainerNode

@Serializable
@SerialName("row")
data class RowNode(
    override val id: String? = null,
    @SerialName("props") override val containerProps: ContainerProps = ContainerProps(),
    override val children: List<DslNode> = emptyList(),
) : DslContainerNode

@Serializable
@SerialName("box")
data class BoxNode(
    override val id: String? = null,
    @SerialName("props") override val containerProps: ContainerProps = ContainerProps(),
    override val children: List<DslNode> = emptyList(),
) : DslContainerNode

@Serializable
@SerialName("carousel")
data class CarouselNode(
    override val id: String? = null,
    val props: CarouselProps = CarouselProps(),
    val dataKey: String? = null,
    val itemTemplate: DslNode? = null,
) : DslNode

// 数据绑定

data class DataContext(
    val fields: Map<String, String> = emptyMap(),
    val lists: Map<String, List<String>> = emptyMap(),
) {
    fun resolve(template: String): String {
        // Step 1: ternary expressions {key == 'val' ? 'a' : 'b'}
        val afterTernary = TERNARY_REGEX.replace(template) { match ->
            val key = match.groupValues[1]
            val op = match.groupValues[2]
            val compareVal = match.groupValues[3]
            val trueVal = match.groupValues[4]
            val falseVal = match.groupValues[5]
            val actual = fields[key] ?: ""
            val condition = if (op == "==") actual == compareVal else actual != compareVal
            if (condition) trueVal else falseVal
        }
        // Step 2: simple placeholders {key}
        return PLACEHOLDER_REGEX.replace(afterTernary) { match ->
            fields[match.groupValues[1]] ?: match.value
        }
    }

    fun resolveList(key: String): List<String> = lists[key] ?: emptyList()

    fun withItem(item: String): DataContext = copy(
        fields = fields + ("item" to item)
    )

    companion object {
        private val PLACEHOLDER_REGEX = Regex("\\{(\\w+)\\}")
        private val TERNARY_REGEX = Regex("\\{(\\w+)\\s*(==|!=)\\s*'([^']*)'\\s*\\?\\s*'([^']*)'\\s*:\\s*'([^']*)'\\}")
    }
}

// 工具函数

internal fun parseHexColor(hex: String): Int? {
    if (hex.equals("transparent", ignoreCase = true)) return 0x00000000
    val sanitized = hex.removePrefix("#")
    if (sanitized.length !in listOf(6, 8)) return null
    return try {
        val value = sanitized.toLong(16)
        if (sanitized.length == 6) (0xFF000000L or value).toInt() else value.toInt()
    } catch (_: NumberFormatException) {
        null
    }
}

internal fun String.parseDp(referenceDp: Float? = null): Float? {
    if (endsWith("%")) {
        val pct = removeSuffix("%").toFloatOrNull() ?: return null
        return referenceDp?.let { it * pct / 100f }
    }
    removeSuffix("dp").toFloatOrNull()?.let { return it }
    return toFloatOrNull()
}

internal fun String.parseSp(): Float? {
    removeSuffix("sp").toFloatOrNull()?.let { return it }
    return toFloatOrNull()
}

internal fun String.parseNumber(): Float? = toFloatOrNull()
