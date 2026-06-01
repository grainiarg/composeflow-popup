# MyDemo — 剪映 VIP 挽留弹窗（Server-Driven UI 演示）

基于 Kotlin Multiplatform + Compose Multiplatform 的 Server-Driven UI (SDUI) 弹窗系统。通过 JSON DSL 动态下发 UI 布局，无需发版即可更新弹窗样式与交互。

## 架构

```
JSON DSL  →  DslParser  →  DslNode 树  →  DslRenderer  →  Compose UI
                ↓
          ParseResult (Success / Failure)
                ↓
          RetentionUiState (Loading / Success / Error)
```

## 工程结构

```
shared/src/
├── commonMain/kotlin/com/example/mydemo/retention/
│   ├── RetentionData.kt       # 数据模型、DSL 节点、事件、工具函数
│   ├── DslParser.kt           # JSON 解析与校验
│   ├── RetentionScreen.kt     # Compose 渲染引擎
│   ├── RetentionViewModel.kt  # ViewModel + Mock 样例
│   ├── RetentionUiState.kt    # UI 状态定义
│   └── PlatformUtil.kt        # expect 平台抽象
├── androidMain/.../retention/
│   └── PlatformUtil.kt        # Android actual 实现
└── iosMain/.../retention/
    └── PlatformUtil.kt        # iOS actual 实现
```

## 已实现功能

- **JSON DSL 解析**：类型安全的节点树，支持 Dialog / Column / Row / Box / Text / Image / Button
- **数据绑定**：`{key}` 占位符，运行时注入文案
- **统一事件模型**：Toast / Navigate / Track / Dismiss 四种事件
- **样式校验**：尺寸、颜色、字号等非法值降级为错误页面，不影响宿主 App
- **多样例切换**：内置 3 个合法样例 + 2 个错误演示（非法尺寸、非法颜色）
- **自适应宽高**：支持 `dp`、`%`、纯数字三种单位，通过 BoxWithConstraints 适配屏幕

## DSL 示例

```json
{
  "version": "1.0",
  "pageId": "simple_vip_001",
  "content": {
    "type": "dialog",
    "props": {
      "width": "75%",
      "maxWidth": "340dp",
      "cornerRadius": "16dp",
      "overlay": { "backgroundColor": "#000000", "opacity": 0.5 }
    },
    "children": [
      {
        "type": "column",
        "props": { "alignItems": "center", "padding": { "top": "32dp" } },
        "children": [
          { "type": "text", "props": { "text": "{title}", "fontSize": "20sp" } },
          {
            "type": "button",
            "props": { "text": "立即续费", "backgroundColor": "#FE315D" },
            "events": { "click": { "type": "track", "eventId": "click_renew" } }
          }
        ]
      }
    ]
  }
}
```

## 运行

```bash
# Android
./gradlew :androidApp:assembleDebug

# iOS — 在 Xcode 中打开 iosApp/ 目录运行
```

## 技术栈

| 层 | 技术 |
|---|---|
| 跨平台框架 | Kotlin Multiplatform |
| UI | Compose Multiplatform |
| 序列化 | kotlinx.serialization |
| 架构 | ViewModel + StateFlow |
| 平台隔离 | expect / actual |
