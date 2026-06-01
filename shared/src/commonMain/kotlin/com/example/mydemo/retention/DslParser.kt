package com.example.mydemo.retention

import kotlinx.serialization.SerializationException

sealed class ParseResult {
    data class Success(val root: DslNode) : ParseResult()
    data class Failure(val errors: List<String>) : ParseResult()
}

internal data class ParseError(val path: String, val message: String)

object DslParser {

    fun parse(json: String): ParseResult {
        val page: DslPage = try {
            dslJson.decodeFromString<DslPage>(json)
        } catch (e: SerializationException) {
            return ParseResult.Failure(listOf("DSL 解析失败: ${e.message}"))
        } catch (e: IllegalArgumentException) {
            return ParseResult.Failure(listOf("DSL 格式错误: ${e.message}"))
        }

        val root = page.content

        val errors = validate(root, "root")
        if (errors.isNotEmpty()) {
            return ParseResult.Failure(errors.map { "[${it.path}] ${it.message}" })
        }

        return ParseResult.Success(root)
    }

    private fun validate(node: DslNode, path: String): List<ParseError> {
        val errors = mutableListOf<ParseError>()

        when (node) {
            is DialogNode -> {
                node.props.width.parseDp()?.let { w ->
                    if (w <= 0) errors.add(ParseError("$path.props.width", "width 必须 > 0"))
                }
                node.props.backgroundColor?.let { bg ->
                    if (parseHexColor(bg) == null) {
                        errors.add(ParseError("$path.props.backgroundColor", "非法颜色值: $bg"))
                    }
                }
                node.props.cornerRadius.parseDp()?.let { r ->
                    if (r < 0) errors.add(ParseError("$path.props.cornerRadius", "cornerRadius 不能为负数"))
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
                node.props.fontSize?.parseSp()?.let { fs ->
                    if (fs <= 0) errors.add(ParseError("$path.props.fontSize", "fontSize 必须 > 0"))
                }
                node.props.color?.let { c ->
                    if (parseHexColor(c) == null) errors.add(ParseError("$path.props.color", "非法颜色值: $c"))
                }
                node.props.fontWeight?.let { fw ->
                    val valid = setOf("normal", "bold", "light", "medium", "400", "500", "700", "300")
                    if (fw !in valid) errors.add(ParseError("$path.props.fontWeight", "未知 fontWeight: $fw"))
                }
            }

            is ImageNode -> {
                if (node.props.src.isBlank()) {
                    errors.add(ParseError(path, "Image 组件缺少 src 属性"))
                }
                node.props.width?.parseDp()?.let { w ->
                    if (w <= 0) errors.add(ParseError("$path.props.width", "width 必须 > 0"))
                }
            }

            is ButtonNode -> {
                if (node.props.text.isBlank()) {
                    errors.add(ParseError(path, "Button 组件缺少 text 属性"))
                }
                node.props.backgroundColor?.let { bg ->
                    if (bg != "transparent" && parseHexColor(bg) == null) {
                        errors.add(ParseError("$path.props.backgroundColor", "非法颜色值: $bg"))
                    }
                }
                node.props.textColor?.let { c ->
                    if (parseHexColor(c) == null) errors.add(ParseError("$path.props.textColor", "非法颜色值: $c"))
                }
            }

            is DslContainerNode -> {
                node.containerProps.backgroundColor?.let { bg ->
                    if (parseHexColor(bg) == null) errors.add(ParseError("$path.containerProps.backgroundColor", "非法颜色值: $bg"))
                }
                node.containerProps.gap?.parseDp()?.let { g ->
                    if (g < 0) errors.add(ParseError("$path.containerProps.gap", "gap 不能为负数"))
                }
                node.children.forEachIndexed { i, child ->
                    errors.addAll(validate(child, "$path.children[$i]"))
                }
            }
        }

        return errors
    }
}
