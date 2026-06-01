package com.example.mydemo.retention

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RetentionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<RetentionUiState>(RetentionUiState.Loading)
    val uiState: StateFlow<RetentionUiState> = _uiState.asStateFlow()

    val dataContext = DataContext(emptyMap())

    init {
        loadDsl()
    }

    private fun loadDsl() {
        try {
            val result = DslParser.parse(MockDsl.retentionPage)
            when (result) {
                is ParseResult.Success -> _uiState.value = RetentionUiState.Success(result.root)
                is ParseResult.Failure -> _uiState.value = RetentionUiState.Error(result.errors)
            }
        } catch (e: Exception) {
            _uiState.value = RetentionUiState.Error(listOf("DSL 加载异常: ${e.message}"))
        }
    }
}

private object MockDsl {

    val retentionPage = """
{
  "version": "1.0",
  "pageId": "vip_retention_001",
  "content": {
    "type": "dialog",
    "id": "root_dialog",
    "props": {
      "width": "315dp",
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
              "justifyContent": "flex-start"
            },
            "children": [
              {
                "type": "image",
                "id": "vip_badge_img",
                "props": {
                  "src": "vip",
                  "width": "54dp",
                  "height": "20dp"
                }
              }
            ]
          },
          {
            "type": "text",
            "id": "title",
            "props": {
              "text": "确定要放弃VIP优惠吗？",
              "fontSize": "18sp",
              "color": "#1A1A1A",
              "fontWeight": "bold",
              "margin": { "top": "24dp", "bottom": "16dp" }
            }
          },
          {
            "type": "row",
            "id": "discount_wrapper",
            "props": {
              "alignItems": "baseline",
              "justifyContent": "center",
              "margin": { "bottom": "36dp" }
            },
            "children": [
              {
                "type": "text",
                "id": "discount_num",
                "props": {
                  "text": "1",
                  "fontSize": "72sp",
                  "color": "#1A1A1A",
                  "fontWeight": "bold"
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
                  "margin": { "left": "4dp" }
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
              "margin": { "bottom": "0dp" }
            }
          },
          {
            "type": "row",
            "id": "benefits_list",
            "props": {
              "gap": "8dp",
              "margin": { "bottom": "32dp" }
            },
            "children": [
              { "type": "image", "props": { "src": "image1", "width": "56dp", "height": "56dp", "cornerRadius": "8dp" } },
              { "type": "image", "props": { "src": "image2", "width": "56dp", "height": "56dp", "cornerRadius": "8dp" } },
              { "type": "image", "props": { "src": "image3", "width": "56dp", "height": "56dp", "cornerRadius": "8dp" } },
              { "type": "image", "props": { "src": "image4", "width": "56dp", "height": "56dp", "cornerRadius": "8dp" } }
            ]
          },
          {
            "type": "button",
            "id": "btn_stay",
            "props": {
              "text": "我再想想",
              "textColor": "#FFFFFF",
              "backgroundColor": "#FE315D",
              "width": "248dp",
              "height": "48dp",
              "cornerRadius": "8dp",
              "fontSize": "16sp"
            },
            "events": {
              "onClick": "stay"
            }
          },
          {
            "type": "button",
            "id": "btn_leave",
            "props": {
              "text": "狠心离开",
              "textColor": "#666666",
              "backgroundColor": "transparent",
              "width": "248dp",
              "height": "44dp",
              "cornerRadius": "8dp",
              "fontSize": "14sp",
              "margin": { "top": "8dp" }
            },
            "events": {
              "onClick": "leave"
            }
          }
        ]
      }
    ]
  }
}
""".trimIndent()
}
