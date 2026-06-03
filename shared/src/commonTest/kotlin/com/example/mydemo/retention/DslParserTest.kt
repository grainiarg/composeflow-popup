package com.example.mydemo.retention

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DslParserTest {

    // 取值非法（组件降级）: 非法属性值在子组件上，各自独立降级
    private val errorInvalidValueJson = """
{
  "version": "1.0",
  "pageId": "error_invalid_value",
  "content": {
    "type": "dialog",
    "id": "root_dialog",
    "props": {
      "width": "75%",
      "maxWidth": "360dp",
      "backgroundColor": "#FFFFFF",
      "cornerRadius": "16dp",
      "cancelable": true,
      "overlay": {
        "backgroundColor": "#000000",
        "opacity": 0.6
      }
    },
    "children": [
      {
        "type": "column",
        "id": "main_content",
        "props": {
          "alignItems": "center",
          "padding": { "top": "24dp", "bottom": "24dp", "left": "20dp", "right": "20dp" }
        },
        "children": [
          {
            "type": "text",
            "id": "title",
            "props": {
              "text": "取值非法演示",
              "fontSize": "18sp",
              "color": "#1A1A1A",
              "fontWeight": "bold",
              "margin": { "bottom": "8dp" }
            }
          },
          {
            "type": "text",
            "id": "desc",
            "props": {
              "text": "以下组件属性值非法，已降级为默认值",
              "fontSize": "13sp",
              "color": "#999999",
              "margin": { "bottom": "20dp" }
            }
          },
          {
            "type": "row",
            "id": "invalid_props_row",
            "props": {
              "justifyContent": "center",
              "gap": "12dp",
              "margin": { "bottom": "16dp" }
            },
            "children": [
              {
                "type": "text",
                "id": "bad_font",
                "props": {
                  "text": "字号非法: big",
                  "fontSize": "big",
                  "color": "#D32F2F"
                }
              },
              {
                "type": "text",
                "id": "bad_color",
                "props": {
                  "text": "颜色非法: #GG0000",
                  "fontSize": "14sp",
                  "color": "#GG0000"
                }
              }
            ]
          },
          {
            "type": "image",
            "id": "bad_width_img",
            "props": {
              "src": "vip",
              "width": "wide",
              "height": "48dp",
              "margin": { "bottom": "16dp" }
            }
          },
          {
            "type": "text",
            "id": "hint",
            "props": {
              "text": "每个组件右上角有 警告，点击查看详情",
              "fontSize": "12sp",
              "color": "#FFA726",
              "margin": { "bottom": "20dp" }
            }
          },
          {
            "type": "button",
            "id": "btn_ok",
            "props": {
              "text": "知道了",
              "textColor": "#FFFFFF",
              "backgroundColor": "#FE315D",
              "width": "fillMaxWidth",
              "height": "44dp",
              "cornerRadius": "8dp",
              "fontSize": "15sp"
            },
            "events": { "click": { "type": "navigate", "route": "", "closeDialog": true } }
          }
        ]
      }
    ]
  }
}
""".trimIndent()

    // 字段缺失（组件降级）: 缺少必要字段，降级为占位符
    private val errorMissingFieldJson = """
{
  "version": "1.0",
  "pageId": "error_missing_field",
  "content": {
    "type": "dialog",
    "id": "root_dialog",
    "props": {
      "width": "75%",
      "maxWidth": "360dp",
      "backgroundColor": "#FFFFFF",
      "cornerRadius": "16dp",
      "cancelable": true,
      "overlay": {
        "backgroundColor": "#000000",
        "opacity": 0.6
      }
    },
    "children": [
      {
        "type": "column",
        "id": "main_content",
        "props": {
          "alignItems": "center",
          "padding": { "top": "24dp", "bottom": "24dp", "left": "20dp", "right": "20dp" }
        },
        "children": [
          {
            "type": "text",
            "id": "title",
            "props": {
              "text": "字段缺失演示",
              "fontSize": "18sp",
              "color": "#1A1A1A",
              "fontWeight": "bold",
              "margin": { "bottom": "8dp" }
            }
          },
          {
            "type": "text",
            "id": "desc",
            "props": {
              "text": "以下组件缺少必要字段，已降级为占位符",
              "fontSize": "13sp",
              "color": "#999999",
              "margin": { "bottom": "20dp" }
            }
          },
          {
            "type": "text",
            "id": "empty_text",
            "props": {
              "text": "",
              "fontSize": "14sp",
              "margin": { "bottom": "12dp" }
            }
          },
          {
            "type": "image",
            "id": "empty_src_img",
            "props": {
              "src": "",
              "width": "48dp",
              "height": "48dp",
              "margin": { "bottom": "12dp" }
            }
          },
          {
            "type": "button",
            "id": "empty_btn",
            "props": {
              "text": "",
              "backgroundColor": "#E0E0E0",
              "width": "fillMaxWidth",
              "height": "44dp",
              "cornerRadius": "8dp",
              "fontSize": "14sp",
              "margin": { "bottom": "20dp" }
            },
            "events": { "click": { "type": "track", "eventId": "click_empty_btn" } }
          },
          {
            "type": "text",
            "id": "hint",
            "props": {
              "text": "每个缺失组件右上角有 警告，点击查看详情",
              "fontSize": "12sp",
              "color": "#FFA726",
              "margin": { "bottom": "20dp" }
            }
          },
          {
            "type": "button",
            "id": "btn_ok",
            "props": {
              "text": "知道了",
              "textColor": "#FFFFFF",
              "backgroundColor": "#FE315D",
              "width": "fillMaxWidth",
              "height": "44dp",
              "cornerRadius": "8dp",
              "fontSize": "15sp"
            },
            "events": { "click": { "type": "navigate", "route": "", "closeDialog": true } }
          }
        ]
      }
    ]
  }
}
""".trimIndent()

    @Test
    fun `error invalid value should succeed with 3 warnings on child components`() {
        val result = DslParser.parse(errorInvalidValueJson)
        assertTrue(result is ParseResult.Success, "Expected Success but got: $result")
        val success = result as ParseResult.Success
        assertEquals(3, success.warnings.size, "Expected 3 warnings but got ${success.warnings}")
        val paths = success.warnings.map { it.path }.toSet()
        assertTrue("root.children[0].children[2].children[0].props.fontSize" in paths)
        assertTrue("root.children[0].children[2].children[1].props.color" in paths)
        assertTrue("root.children[0].children[3].props.width" in paths)
    }

    @Test
    fun `error missing field should succeed with 3 warnings on child components`() {
        val result = DslParser.parse(errorMissingFieldJson)
        assertTrue(result is ParseResult.Success, "Expected Success but got: $result")
        val success = result as ParseResult.Success
        assertEquals(3, success.warnings.size, "Expected 3 warnings but got ${success.warnings}")
        val paths = success.warnings.map { it.path }.toSet()
        assertTrue("root.children[0].children[2]" in paths)
        assertTrue("root.children[0].children[3]" in paths)
        assertTrue("root.children[0].children[4]" in paths)
    }

    @Test
    fun `valid dialog should succeed with no warnings`() {
        val json = """
        {
          "version": "1.0",
          "pageId": "test",
          "content": {
            "type": "dialog",
            "id": "root_dialog",
            "props": {
              "width": "75%",
              "maxWidth": "360dp",
              "backgroundColor": "#FFFFFF",
              "cornerRadius": "16dp",
              "cancelable": false,
              "overlay": {
                "backgroundColor": "#000000",
                "opacity": 0.6
              }
            },
            "children": [
              {
                "type": "column",
                "props": { "alignItems": "center" },
                "children": [
                  { "type": "text", "props": { "text": "Hello", "fontSize": "19sp", "color": "#1A1A1A" } }
                ]
              }
            ]
          }
        }
        """.trimIndent()

        val result = DslParser.parse(json)
        assertTrue(result is ParseResult.Success, "Expected Success but got: $result")
        val success = result as ParseResult.Success
        assertEquals(0, success.warnings.size, "Expected 0 warnings but got: ${success.warnings}")
    }
}
