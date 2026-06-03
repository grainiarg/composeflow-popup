package com.example.mydemo.retention

import kotlinx.serialization.SerializationException

sealed class ParseResult {
    data class Success(
        val root: DslNode,
        val warnings: List<ParseError> = emptyList(),
    ) : ParseResult()
    data class Failure(val errors: List<String>) : ParseResult()
}

data class ParseError(val path: String, val message: String)

object DslParser {

    fun parse(json: String): ParseResult {
        val page: DslPage = try {
            dslJson.decodeFromString<DslPage>(json)
        } catch (e: SerializationException) {
            println("[DslParser] FAIL deserialization: ${e.message}")
            return ParseResult.Failure(listOf("DSL 解析失败: ${e.message}"))
        } catch (e: IllegalArgumentException) {
            println("[DslParser] FAIL illegal arg: ${e.message}")
            return ParseResult.Failure(listOf("DSL 格式错误: ${e.message}"))
        }

        val root = page.content

        val warnings = validate(root, "root")
        println("[DslParser] SUCCESS, warnings count: ${warnings.size}")
        warnings.forEach { println("[DslParser]   warning: [${it.path}] ${it.message}") }
        return ParseResult.Success(root, warnings)
    }

    private fun validate(node: DslNode, path: String): List<ParseError> {
        val errors = mutableListOf<ParseError>()

        when (node) {
            is DialogNode -> {
                node.props.width.let { wStr ->
                    val w = wStr.parseDp(REFERENCE_WIDTH)
                    if (w == null) errors.add(ParseError("$path.props.width", "无法解析的尺寸: $wStr"))
                    else if (w <= 0) errors.add(ParseError("$path.props.width", "width 必须 > 0"))
                }
                node.props.backgroundColor?.let { bg ->
                    if (parseHexColor(bg) == null) errors.add(ParseError("$path.props.backgroundColor", "非法颜色值: $bg"))
                }
                node.props.cornerRadius.let { rStr ->
                    val r = rStr.parseDp()
                    if (r == null) errors.add(ParseError("$path.props.cornerRadius", "无法解析的尺寸: $rStr"))
                    else if (r < 0) errors.add(ParseError("$path.props.cornerRadius", "cornerRadius 不能为负数"))
                }
                node.props.overlay?.backgroundColor?.let { oc ->
                    if (parseHexColor(oc) == null) errors.add(ParseError("$path.props.overlay.backgroundColor", "非法颜色值: $oc"))
                }
                node.children.forEachIndexed { i, child ->
                    errors.addAll(validate(child, "$path.children[$i]"))
                }
            }

            is TextNode -> {
                if (node.props.text.isBlank()) {
                    errors.add(ParseError(path, "Text 组件缺少 text 属性"))
                }
                node.props.fontSize?.let { fsStr ->
                    val fs = fsStr.parseSp()
                    if (fs == null) errors.add(ParseError("$path.props.fontSize", "无法解析的字号: $fsStr"))
                    else if (fs <= 0) errors.add(ParseError("$path.props.fontSize", "fontSize 必须 > 0"))
                }
                node.props.color?.let { c ->
                    if (parseHexColor(c) == null) errors.add(ParseError("$path.props.color", "非法颜色值: $c"))
                }
                node.props.fontWeight?.let { fw ->
                    val valid = setOf("normal", "bold", "light", "medium", "400", "500", "700", "300")
                    if (fw !in valid) errors.add(ParseError("$path.props.fontWeight", "未知 fontWeight: $fw"))
                }
                validateSpacing(node.props.padding, "$path.props.padding", errors)
                validateSpacing(node.props.margin, "$path.props.margin", errors)
            }

            is AnimatedTextNode -> {
                if (node.props.text.isBlank()) {
                    errors.add(ParseError(path, "AnimatedText 组件缺少 text 属性"))
                }
                node.props.fontSize?.let { fsStr ->
                    val fs = fsStr.parseSp()
                    if (fs == null) errors.add(ParseError("$path.props.fontSize", "无法解析的字号: $fsStr"))
                    else if (fs <= 0) errors.add(ParseError("$path.props.fontSize", "fontSize 必须 > 0"))
                }
                node.props.color?.let { c ->
                    if (parseHexColor(c) == null) errors.add(ParseError("$path.props.color", "非法颜色值: $c"))
                }
                validateSpacing(node.props.padding, "$path.props.padding", errors)
                validateSpacing(node.props.margin, "$path.props.margin", errors)
            }

            is ImageNode -> {
                if (node.props.src.isBlank()) {
                    errors.add(ParseError(path, "Image 组件缺少 src 属性"))
                }
                node.props.width?.let { wStr ->
                    val w = wStr.parseDp(REFERENCE_WIDTH)
                    if (w == null) errors.add(ParseError("$path.props.width", "无法解析的尺寸: $wStr"))
                    else if (w <= 0) errors.add(ParseError("$path.props.width", "width 必须 > 0"))
                }
                validateSpacing(node.props.padding, "$path.props.padding", errors)
                validateSpacing(node.props.margin, "$path.props.margin", errors)
            }

            is ButtonNode -> {
                if (node.props.text.isBlank()) {
                    errors.add(ParseError(path, "Button 组件缺少 text 属性"))
                }
                node.props.backgroundColor?.let { bg ->
                    if (parseHexColor(bg) == null) errors.add(ParseError("$path.props.backgroundColor", "非法颜色值: $bg"))
                }
                node.props.textColor?.let { c ->
                    if (parseHexColor(c) == null) errors.add(ParseError("$path.props.textColor", "非法颜色值: $c"))
                }
                validateSpacing(node.props.padding, "$path.props.padding", errors)
                validateSpacing(node.props.margin, "$path.props.margin", errors)
            }

            is DslContainerNode -> {
                node.containerProps.backgroundColor?.let { bg ->
                    if (parseHexColor(bg) == null) errors.add(ParseError("$path.containerProps.backgroundColor", "非法颜色值: $bg"))
                }
                node.containerProps.gap?.let { gStr ->
                    val g = gStr.parseDp()
                    if (g == null) errors.add(ParseError("$path.containerProps.gap", "无法解析的间距: $gStr"))
                    else if (g < 0) errors.add(ParseError("$path.containerProps.gap", "gap 不能为负数"))
                }
                validateSpacing(node.containerProps.padding, "$path.containerProps.padding", errors)
                validateSpacing(node.containerProps.margin, "$path.containerProps.margin", errors)
                node.children.forEachIndexed { i, child ->
                    errors.addAll(validate(child, "$path.children[$i]"))
                }
            }

            is CarouselNode -> {
                if (node.dataKey.isNullOrBlank()) {
                    errors.add(ParseError(path, "Carousel 组件缺少 dataKey"))
                }
                if (node.itemTemplate == null) {
                    errors.add(ParseError(path, "Carousel 组件缺少 itemTemplate"))
                } else {
                    errors.addAll(validate(node.itemTemplate, "$path.itemTemplate"))
                }
                node.props.width?.let { wStr ->
                    val w = wStr.parseDp(REFERENCE_WIDTH)
                    if (w == null) errors.add(ParseError("$path.props.width", "无法解析的尺寸: $wStr"))
                    else if (w <= 0) errors.add(ParseError("$path.props.width", "width 必须 > 0"))
                }
                node.props.height?.let { hStr ->
                    val h = hStr.parseDp()
                    if (h == null) errors.add(ParseError("$path.props.height", "无法解析的尺寸: $hStr"))
                    else if (h <= 0) errors.add(ParseError("$path.props.height", "height 必须 > 0"))
                }
                validateSpacing(node.props.padding, "$path.props.padding", errors)
                validateSpacing(node.props.margin, "$path.props.margin", errors)
            }
        }

        return errors
    }

    private fun validateSpacing(spacing: SpacingProps?, path: String, errors: MutableList<ParseError>) {
        if (spacing == null) return
        listOf("top" to spacing.top, "bottom" to spacing.bottom, "left" to spacing.left, "right" to spacing.right)
            .forEach { (key, value) ->
                value?.let { vStr ->
                    val v = vStr.parseDp()
                    if (v == null) errors.add(ParseError("$path.$key", "无法解析的间距: $vStr"))
                }
            }
    }

    private const val REFERENCE_WIDTH = 375f
}
