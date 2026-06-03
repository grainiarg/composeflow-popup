package com.example.mydemo.retention

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DslParserTest {

    // 基于标准 VIP 挽留 DSL，注入 4 个故意的属性错误
    private val vipRetentionWithWarningsJson = """
{
  "version": "1.0",
  "pageId": "vip_retention_warn_001",
  "content": {
    "type": "dialog",
    "id": "root_dialog",
    "props": {
      "width": "75%",
      "maxWidth": "360dp",
      "backgroundImage": "bg_dialog",
      "cancelable": false,
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
          "width": "100%",
          "alignItems": "center",
          "padding": { "top": "24dp", "bottom": "24dp", "left": "24dp", "right": "24dp" }
        },
        "children": [
          {
            "type": "row",
            "id": "vip_badge_wrapper",
            "props": {
              "width": "100%",
              "justifyContent": "flex-start",
              "margin":{ "top": "10dp" }
            },
            "children": [
              {
                "type": "image",
                "id": "vip_badge_img",
                "props": { "src": "vip", "width": "54dp", "height": "20dp" }
              }
            ]
          },
          {
            "type": "text",
            "id": "title",
            "props": {
              "text": "确定要放弃VIP优惠吗？",
              "fontSize": "big",
              "color": "#1A1A1A",
              "fontWeight": "bold",
              "margin": { "top": "28dp", "bottom": "16dp" }
            }
          },
          {
            "type": "row",
            "id": "discount_wrapper",
            "props": {
              "alignItems": "baseline",
              "justifyContent": "center",
              "margin": { "top": "2dp", "bottom": "14dp" }
            },
            "children": [
              {
                "type": "animatedText",
                "id": "discount_num",
                "props": {
                  "text": "1",
                  "fontSize": "80sp",
                  "color": "#GG0000",
                  "fontWeight": "bold",
                  "animate": "numberScroll"
                }
              },
              {
                "type": "text",
                "id": "discount_unit",
                "props": {
                  "text": "折",
                  "fontSize": "24sp",
                  "color": "#1A1A1A",
                  "fontWeight": "bold",
                  "includeFontPadding": false,
                  "margin": { "left": "2dp" }
                }
              }
            ]
          },
          {
            "type": "text",
            "id": "subtitle",
            "props": {
              "text": "365万+会员权益等你来体验",
              "fontSize": "12sp",
              "color": "#999999",
              "margin": { "top": "6dp","bottom": "12dp" }
            }
          },
          {
            "type": "carousel",
            "id": "benefits_carousel",
            "props": {
              "width": "100%",
              "height": "auto",
              "itemPerPage": 4,
              "autoPlay": true,
              "loop": true,
              "interval": 2000,
              "margin": { "bottom": "12dp" }
            },
            "dataKey": "benefits",
            "itemTemplate": {
              "type": "image",
              "props": {
                "src": "{item}",
                "width": "56dp",
                "height": "56dp",
                "cornerRadius": "8dp"
              }
            }
          },
          {
            "type": "text",
            "id": "empty_placeholder",
            "props": {
              "text": "",
              "fontSize": "12sp",
              "margin": { "bottom": "8dp" }
            }
          },
          {
            "type": "button",
            "id": "btn_stay",
            "props": {
              "text": "我再想想",
              "textColor": "#FFFFFF",
              "backgroundColor": "#FE315D",
              "width": "fillMaxWidth",
              "height": "48dp",
              "cornerRadius": "8dp",
              "fontSize": "16sp"
            },
            "events": { "click": { "type": "navigate", "route": "", "closeDialog": true } }
          },
          {
            "type": "button",
            "id": "btn_leave",
            "props": {
              "text": "狠心离开",
              "textColor": "#666666",
              "backgroundColor": "transparent",
              "width": "fillMaxWidth",
              "height": "48dp",
              "cornerRadius": "8dp",
              "fontSize": "16sp"
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
    fun `vip retention with warnings should succeed with 4 warnings`() {
        val result = DslParser.parse(vipRetentionWithWarningsJson)
        assertTrue(result is ParseResult.Success, "Expected Success but got: $result")
        val success = result as ParseResult.Success
        assertEquals(4, success.warnings.size, "Expected 4 warnings but got ${success.warnings}")
        val paths = success.warnings.map { it.path }.toSet()
        assertTrue("root.children[0].children[1].props.fontSize" in paths, "missing fontSize warning")
        assertTrue("root.children[0].children[2].children[0].props.color" in paths, "missing color warning")
        assertTrue("root.children[0].children[4].props.height" in paths, "missing height warning")
        assertTrue("root.children[0].children[5]" in paths, "missing empty text warning")
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
