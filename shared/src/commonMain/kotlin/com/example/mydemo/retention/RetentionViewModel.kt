package com.example.mydemo.retention

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DslSample(
    val name: String,
    val json: String,
    val isErrorDemo: Boolean = false,
)

class RetentionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<RetentionUiState>(RetentionUiState.Loading)
    val uiState: StateFlow<RetentionUiState> = _uiState.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    val dataContext = DataContext(
        fields = retentionData,
        lists = mapOf("benefits" to listOf("image1", "image2", "image3", "image4", "image5", "image6", "image7", "image8")),
    )

    val samples: List<DslSample> = RetentionViewModel.samples

    companion object {
        val retentionData = mapOf(
            "title" to "确定要放弃VIP优惠吗？",
            "discount" to "1",
            "unit" to "折",
            "subtitle" to "365万+会员权益等你来体验",
            "btn_stay" to "我再想想",
            "btn_leave" to "狠心离开",
        )

        val samples = listOf(
            DslSample("标准VIP挽留", MockSamples.vipRetention),
            DslSample("简化弹窗", MockSamples.simpleDialog),
            DslSample("强提醒弹窗", MockSamples.strongDialog),
            DslSample("错误-非法尺寸", MockSamples.errorInvalidSize, isErrorDemo = true),
            DslSample("错误-非法颜色", MockSamples.errorInvalidColor, isErrorDemo = true),
        )
    }

    init {
        loadSample(0)
    }

    fun switchSample(index: Int) {
        _currentIndex.value = index
        loadSample(index)
    }

    private fun loadSample(index: Int) {
        try {
            val result = DslParser.parse(samples[index].json)
            when (result) {
                is ParseResult.Success -> _uiState.value = RetentionUiState.Success(result.root)
                is ParseResult.Failure -> _uiState.value = RetentionUiState.Error(result.errors)
            }
        } catch (e: Exception) {
            _uiState.value = RetentionUiState.Error(listOf("DSL 加载异常: ${e.message}"))
        }
    }
}

private object MockSamples {

    val vipRetention = """
{
  "version": "1.0",
  "pageId": "vip_retention_001",
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
              "text": "{title}",
              "fontSize": "19sp",
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
                  "text": "{discount}",
                  "fontSize": "80sp",
                  "color": "#1A1A1A",
                  "fontWeight": "bold",
                  "animate": "numberScroll"
                }
              },
              {
                "type": "text",
                "id": "discount_unit",
                "props": {
                  "text": "{unit}",
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
              "text": "{subtitle}",
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
              "height": "72dp",
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
            "type": "button",
            "id": "btn_stay",
            "props": {
              "text": "{btn_stay}",
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
              "text": "{btn_leave}",
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

    val simpleDialog = """
{
  "version": "1.0",
  "pageId": "simple_vip_001",
  "content": {
    "type": "dialog",
    "id": "root_dialog",
    "props": {
      "width": "75%",
      "maxWidth": "340dp",
      "backgroundColor": "#FFFFFF",
      "cornerRadius": "16dp",
      "cancelable": true,
      "overlay": {
        "backgroundColor": "#000000",
        "opacity": 0.5
      }
    },
    "children": [
      {
        "type": "column",
        "id": "main_content",
        "props": {
          "alignItems": "center",
          "padding": { "top": "32dp", "bottom": "24dp", "left": "24dp", "right": "24dp" }
        },
        "children": [
          {
            "type": "text",
            "id": "title",
            "props": {
              "text": "VIP会员即将到期",
              "fontSize": "20sp",
              "color": "#1A1A1A",
              "fontWeight": "bold",
              "margin": { "bottom": "12dp" }
            }
          },
          {
            "type": "text",
            "id": "desc",
            "props": {
              "text": "续费享专属优惠，立即续费锁定权益",
              "fontSize": "14sp",
              "color": "#666666",
              "textAlign": "center",
              "margin": { "bottom": "28dp" }
            }
          },
          {
            "type": "button",
            "id": "btn_renew",
            "props": {
              "text": "立即续费",
              "textColor": "#FFFFFF",
              "backgroundColor": "#FE315D",
              "width": "fillMaxWidth",
              "height": "48dp",
              "cornerRadius": "8dp",
              "fontSize": "16sp"
            },
            "events": { "click": { "type": "track", "eventId": "click_renew_vip" } }
          },
          {
            "type": "button",
            "id": "btn_cancel",
            "props": {
              "text": "暂不续费",
              "textColor": "#999999",
              "backgroundColor": "transparent",
              "width": "fillMaxWidth",
              "height": "44dp",
              "cornerRadius": "8dp",
              "fontSize": "14sp",
              "margin": { "top": "8dp" }
            },
            "events": { "click": { "type": "navigate", "route": "", "closeDialog": true } }
          }
        ]
      }
    ]
  }
}
""".trimIndent()

    val strongDialog = """
{
  "version": "1.0",
  "pageId": "strong_vip_001",
  "content": {
    "type": "dialog",
    "id": "root_dialog",
    "props": {
      "width": "80%",
      "maxWidth": "380dp",
      "backgroundColor": "#FFF5F5",
      "cornerRadius": "16dp",
      "cancelable": false,
      "overlay": {
        "backgroundColor": "#000000",
        "opacity": 0.7
      }
    },
    "children": [
      {
        "type": "column",
        "id": "main_content",
        "props": {
          "alignItems": "center",
          "padding": { "top": "28dp", "bottom": "24dp", "left": "20dp", "right": "20dp" }
        },
        "children": [
          {
            "type": "text",
            "id": "warning_title",
            "props": {
              "text": "最后机会！",
              "fontSize": "22sp",
              "color": "#D32F2F",
              "fontWeight": "bold",
              "margin": { "bottom": "8dp" }
            }
          },
          {
            "type": "text",
            "id": "warning_desc",
            "props": {
              "text": "VIP折扣还有24小时过期\n错过再等一年",
              "fontSize": "14sp",
              "color": "#666666",
              "textAlign": "center",
              "margin": { "bottom": "24dp" }
            }
          },
          {
            "type": "button",
            "id": "btn_claim",
            "props": {
              "text": "立即领取",
              "textColor": "#FFFFFF",
              "backgroundColor": "#D32F2F",
              "width": "fillMaxWidth",
              "height": "52dp",
              "cornerRadius": "8dp",
              "fontSize": "18sp",
              "fontWeight": "bold"
            },
            "events": { "click": { "type": "track", "eventId": "click_claim_urgent" } }
          },
          {
            "type": "button",
            "id": "btn_giveup",
            "props": {
              "text": "放弃优惠",
              "textColor": "#BDBDBD",
              "backgroundColor": "transparent",
              "width": "fillMaxWidth",
              "height": "44dp",
              "cornerRadius": "8dp",
              "fontSize": "14sp",
              "margin": { "top": "8dp" }
            },
            "events": { "click": { "type": "navigate", "route": "", "closeDialog": true } }
          }
        ]
      }
    ]
  }
}
""".trimIndent()

    val errorInvalidSize = """
{
  "version": "1.0",
  "pageId": "error_size",
  "content": {
    "type": "dialog",
    "props": {
      "width": "aaaaa",
      "maxWidth": "360dp",
      "backgroundImage": "bg_dialog",
      "overlay": {
        "backgroundColor": "#000000",
        "opacity": 0.6
      }
    },
    "children": [
      {
        "type": "column",
        "id": "main_content",
        "props": { "alignItems": "center" },
        "children": [
          { "type": "text", "id": "err_title", "props": { "text": "这个弹窗不会渲染出来", "fontSize": "18sp" } }
        ]
      }
    ]
  }
}
""".trimIndent()

    val errorInvalidColor = """
{
  "version": "1.0",
  "pageId": "error_color",
  "content": {
    "type": "dialog",
    "id": "root_dialog",
    "props": {
      "width": "75%",
      "maxWidth": "360dp",
      "backgroundColor": "#GG0000",
      "overlay": {
        "backgroundColor": "#000000",
        "opacity": 0.6
      }
    },
    "children": [
      {
        "type": "column",
        "id": "main_content",
        "props": { "alignItems": "center" },
        "children": [
          { "type": "text", "id": "err_title", "props": { "text": "这个弹窗不会渲染出来", "fontSize": "18sp" } }
        ]
      }
    ]
  }
}
""".trimIndent()
}
