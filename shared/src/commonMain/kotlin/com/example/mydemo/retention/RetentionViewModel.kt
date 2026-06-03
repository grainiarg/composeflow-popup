package com.example.mydemo.retention

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    var dataContext: DataContext = DataContext()
        private set

    val samples: List<DslSample> = RetentionViewModel.samples

    companion object {
        val retentionData = mapOf(
            "userLevel" to "VIP",
            "discount" to "1",
            "unit" to "折",
        )

        val svipRetentionData = mapOf(
            "userLevel" to "SVIP",
            "discount" to "0.5",
            "unit" to "折",
        )

        private val benefitsList = listOf("image1", "image2", "image3", "image4", "image5", "image6", "image7", "image8")

        val samples = listOf(
            DslSample("标准VIP挽留", MockSamples.vipRetention),
            DslSample("SVIP挽留", MockSamples.vipRetention),
            DslSample("简化弹窗", MockSamples.simpleDialog),
            DslSample("强提醒弹窗", MockSamples.strongDialog),
            DslSample("取值非法（组件降级）", MockSamples.errorInvalidValue, isErrorDemo = true),
            DslSample("字段缺失（组件降级）", MockSamples.errorMissingField, isErrorDemo = true),
            DslSample("空数据", MockSamples.emptyDialog, isErrorDemo = true),
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
        _uiState.value = RetentionUiState.Loading
        viewModelScope.launch {
            delay(400)
            val sample = samples[index]
            dataContext = when (sample.name) {
                "SVIP挽留" -> DataContext(fields = svipRetentionData, lists = mapOf("benefits" to benefitsList))
                else -> DataContext(fields = retentionData, lists = mapOf("benefits" to benefitsList))
            }
            try {
                val result = DslParser.parse(sample.json)
                when (result) {
                    is ParseResult.Success -> {
                        val root = result.root
                        val isEmpty = when (root) {
                            is DialogNode -> root.children.isEmpty()
                            else -> false
                        }
                        println("[ViewModel] parse SUCCESS, isEmpty=$isEmpty, warnings=${result.warnings.size}")
                        _uiState.value = if (isEmpty) RetentionUiState.Empty else RetentionUiState.Success(root, result.warnings)
                    }
                    is ParseResult.Failure -> {
                        println("[ViewModel] parse FAILURE, errors=${result.errors}")
                        _uiState.value = RetentionUiState.Error(result.errors)
                    }
                }
            } catch (e: Exception) {
                println("[ViewModel] parse EXCEPTION: ${e.message}")
                _uiState.value = RetentionUiState.Error(listOf("DSL 加载异常: ${e.message}"))
            }
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
                "props": { "src": "{userLevel == 'SVIP' ? 'svip' : 'vip'}", "width": "54dp", "height": "20dp" }
              }
            ]
          },
          {
            "type": "text",
            "id": "title",
            "props": {
              "text": "{userLevel == 'SVIP' ? 'SVIP专属优惠限时开启' : '确定要放弃VIP优惠吗？'}",
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
              "text": "{userLevel == 'SVIP' ? '电脑/手机/pad多端通用的顶级权益' : '365万+会员权益等你来体验'}",
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
              "text": "{userLevel == 'SVIP' ? '立即续费' : '我再想想'}",
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
              "text": "{userLevel == 'SVIP' ? '暂不续费' : '狠心离开'}",
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

    val errorInvalidValue = """
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
              "text": "每个组件右上角有 ⚠，点击查看详情",
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

    val errorMissingField = """
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
              "text": "每个缺失组件右上角有 ⚠，点击查看详情",
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

    val emptyDialog = """
{
  "version": "1.0",
  "pageId": "empty_demo",
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
    "children": []
  }
}
""".trimIndent()
}
