# composeflow-popup — Server-Driven UI 弹窗系统

基于 Kotlin Multiplatform + Compose Multiplatform 的 Server-Driven UI (SDUI) 弹窗演示项目。通过 JSON DSL 动态下发 UI 布局，支持数据绑定、动画渲染、优雅降级与版本兼容，无需发版即可更新弹窗样式与交互。

## 架构

```
JSON DSL  →  DslParser  →  DslNode 树  →  DslRenderer  →  Compose UI
                ↓
          ParseResult (Success + warnings / Failure)
                ↓
          RetentionUiState (Loading / Success / Empty / Error)
```

- **JSON 反序列化失败** → `Failure` → 红色错误页，弹窗不渲染
- **属性校验不通过** → `Success(warnings)` → 弹窗正常渲染，非法值使用默认值降级，组件标记 ⚠ 并在顶部展示可展开警告面板

## 工程结构

```
shared/src/
├── commonMain/kotlin/com/example/mydemo/retention/
│   ├── RetentionData.kt       # 数据模型、DSL 节点、事件模型、数据绑定、工具函数
│   ├── DslParser.kt           # JSON 解析与属性校验（生成 warnings）
│   ├── RetentionScreen.kt     # Compose 渲染引擎 + WarningBadge + WarningsPanel
│   ├── RetentionViewModel.kt  # ViewModel + 6 个 Mock 样例
│   ├── RetentionUiState.kt    # UI 状态定义
│   └── PlatformUtil.kt        # expect 平台抽象
├── androidMain/.../retention/
│   └── PlatformUtil.kt        # Android actual 实现
└── iosMain/.../retention/
    └── PlatformUtil.kt        # iOS actual 实现
```

## 已实现功能

### 组件类型

| 类型 | 说明 |
|---|---|
| **Dialog** | 弹窗容器：遮罩层（overlay）、背景图（backgroundImage）、圆角、宽度约束、点击遮罩关闭 |
| **Column / Row / Box** | 布局容器：alignItems、justifyContent、baseline 对齐、gap 间距、padding/margin、背景色 |
| **Text** | 文本：字号(sp)、颜色(#RRGGBB)、字重、行高、IncludeFontPadding、最大行数、文本对齐 |
| **AnimatedText** | 动画数字：`numberScroll` 滚动动画（1.5s tween），支持 `animateFrom` 指定起始值 |
| **Image** | 图片：资源映射、圆角、固定宽高 |
| **Button** | 按钮：填充宽度（fillMaxWidth）、自定义颜色/圆角/字号，支持点击事件 |
| **Carousel** | 轮播：自动播放、无限循环、每页数量、间隔时间，通过 dataKey + itemTemplate 绑定列表数据 |

### 数据绑定

- **简单占位符**：`{key}` 从 DataContext 注入运行时数据
- **三元表达式**：`{key == 'val' ? '真值' : '假值'}` 支持条件渲染
- **列表绑定**：Carousel 通过 `dataKey` + `withItem()` 循环渲染

### 事件模型

| 事件类型 | 说明 |
|---|---|
| `toast` | Toast 提示，携带 `msg` |
| `navigate` | 页面跳转，可携带 `route` + `closeDialog` 关闭弹窗 |
| `track` | 埋点上报，携带 `eventId` + `extra` |
| `dismiss` | 关闭弹窗 |

### 样式校验与优雅降级

- 属性校验（尺寸、颜色、字号、字重等）不阻断渲染，以 **警告** 形式收集
- 非法值使用**默认值降级**（字号 → 14sp、颜色 → Unspecified、尺寸 → null）
- 问题组件右上角标记琥珀色 **⚠ 徽章**，点击展开查看该组件警告明细
- 弹窗顶部展示**可展开警告面板**「⚠ 检测到 N 个警告」，列出全部警告路径与信息
- 只有 JSON 反序列化异常才是致命错误（红色错误页）

### 版本兼容

- 未知 JSON 字段自动忽略（`ignoreUnknownKeys = true`）
- 不可解析的属性值降级为默认值并记录警告
- 内置 V2→V1 降级演示样例，模拟 `clamp()` 响应式字号、`gradient()` 渐变色等高版本语法在低版本的降级行为

### 屏幕适配

- `%` / `dp` / 纯数字三种尺寸单位
- `BoxWithConstraints` 获取屏幕宽度作为 `%` 计算基准
- `statusBarsPadding()` + `navigationBarsPadding()` 系统栏安全区适配

## DSL 示例

```json
{
  "version": "1.0",
  "pageId": "vip_retention_001",
  "content": {
    "type": "dialog",
    "props": {
      "width": "75%",
      "maxWidth": "360dp",
      "backgroundImage": "bg_dialog",
      "cancelable": true,
      "overlay": { "backgroundColor": "#000000", "opacity": 0.6 }
    },
    "children": [
      {
        "type": "column",
        "props": { "alignItems": "center", "padding": { "top": "24dp" } },
        "children": [
          { "type": "image", "props": { "src": "{userLevel == 'SVIP' ? 'svip' : 'vip'}", "width": "54dp", "height": "20dp" } },
          { "type": "text", "props": { "text": "确定要放弃VIP优惠吗？", "fontSize": "19sp", "color": "#1A1A1A", "fontWeight": "bold" } },
          {
            "type": "row", "props": { "alignItems": "baseline", "justifyContent": "center" },
            "children": [
              { "type": "animatedText", "props": { "text": "{discount}", "fontSize": "80sp", "fontWeight": "bold", "animate": "numberScroll" } },
              { "type": "text", "props": { "text": "{unit}", "fontSize": "24sp", "fontWeight": "bold" } }
            ]
          },
          { "type": "text", "props": { "text": "365万+会员权益等你来体验", "fontSize": "12sp", "color": "#999999" } },
          {
            "type": "carousel",
            "props": { "width": "100%", "height": "72dp", "itemPerPage": 4, "autoPlay": true, "loop": true, "interval": 2000 },
            "dataKey": "benefits",
            "itemTemplate": { "type": "image", "props": { "src": "{item}", "width": "56dp", "height": "56dp", "cornerRadius": "8dp" } }
          },
          {
            "type": "button",
            "props": { "text": "立即续费", "backgroundColor": "#FE315D", "textColor": "#FFFFFF", "width": "fillMaxWidth", "height": "48dp", "cornerRadius": "8dp" },
            "events": { "click": { "type": "navigate", "route": "", "closeDialog": true } }
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
