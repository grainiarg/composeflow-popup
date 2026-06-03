# DSL 核心字段设计

## 1. 元数据

```json
{
  "version": "1.0",    // DSL 版本号
  "pageId": "vip_retention_001",  // 页面唯一标识
  "content": {}         // UI 根节点（统一节点结构）
}
```

## 2. UI 节点

```json
{
  "type": "",        // 必填：dialog / column / row / box / text / image / button / carousel / animatedText
  "id": "",          // 可选：节点唯一标识
  "props": {},       // 样式与内容属性
  "events": {},      // 可选：交互事件
  "children": [],    // 可选：子节点列表（容器组件才有）
  "dataKey": "",     // 可选：数据源 key（carousel 专用）
  "itemTemplate": {} // 可选：子项渲染模板（carousel 专用）
}
```

### 组件一览

| type | 说明 | 特有字段 |
|---|---|---|
| `dialog` | 弹窗根节点，负责灰色蒙层 + 白底卡片 | `cancelable`, `overlay`, `backgroundImage` |
| `column` | 垂直布局容器 | — |
| `row` | 水平布局容器 | — |
| `box` | 层叠布局容器 | — |
| `text` | 文本 | `text` |
| `image` | 图片 | `src`, `scaleType` |
| `button` | 按钮 | `text`, `textColor` |
| `carousel` | 轮播 | `dataKey`, `itemTemplate` |
| `animatedText` | 数字滚动动画文本 | `text`, `animate` |

## 3. 其他字段

### 3.1 props 字段

#### 通用样式

| 分类 | 字段 | 类型 | 说明 |
|---|---|---|---|
| 布局 | `width` | `Dimension` | `"100%"` / `"315dp"` / `"fillMaxWidth"` |
| 布局 | `height` | `Dimension` | 同上 |
| 布局 | `maxWidth` | `Dimension` | 最大宽度 |
| 布局 | `margin` | `Spacing` | `{ "top": "12dp", "bottom": "8dp" }` |
| 布局 | `padding` | `Spacing` | 同上 |
| 布局 | `gap` | `Dimension` | 子元素间距 |
| 对齐 | `alignItems` | `String` | `"center"` / `"flex-start"` / `"baseline"` |
| 对齐 | `justifyContent` | `String` | `"center"` / `"flex-start"` |
| 外观 | `backgroundColor` | `Color` | `"#FFFFFF"` / `"transparent"` |
| 外观 | `cornerRadius` | `Dimension` | `"16dp"` / `"8dp"` |
| 外观 | `opacity` | `Float` | `0.6` |
| 文本 | `fontSize` | `Dimension` | `"18sp"` |
| 文本 | `color` | `Color` | `"#1A1A1A"` |
| 文本 | `fontWeight` | `String` | `"bold"` |
| 文本 | `textAlign` | `String` | `"center"` |
| 文本 | `includeFontPadding` | `Boolean` | `false`（去除文字内边距） |

#### 组件特有字段

**text**

| 字段 | 类型 | 说明 |
|---|---|---|
| `text` | `String` | 文本内容，支持数据绑定 `{key}` 和三元表达式 |

**image**

| 字段 | 类型 | 说明 |
|---|---|---|
| `src` | `String` | 图片资源名，支持数据绑定和表达式 |
| `scaleType` | `String` | `"fit"` / `"crop"`（默认 crop） |

**button**

| 字段 | 类型 | 说明 |
|---|---|---|
| `text` | `String` | 按钮文案，支持数据绑定和表达式 |
| `textColor` | `Color` | 文字颜色 |

**dialog**

| 字段 | 类型 | 说明 |
|---|---|---|
| `cancelable` | `Boolean` | 点击蒙层是否关闭（默认 true） |
| `backgroundImage` | `String` | 弹窗背景图资源名 |
| `overlay` | `Overlay` | 蒙层配置：`{ "backgroundColor": "#000000", "opacity": 0.6 }` |

**carousel**

| 字段 | 类型 | 说明 |
|---|---|---|
| `itemPerPage` | `Int` | 每页显示 item 数量（默认 4） |
| `autoPlay` | `Boolean` | 是否自动轮播（默认 false） |
| `loop` | `Boolean` | 是否无限循环（默认 false） |
| `interval` | `Long` | 自动轮播间隔毫秒（默认 2000） |

**animatedText**

| 字段 | 类型 | 说明 |
|---|---|---|
| `animate` | `String` | 动画类型，当前支持 `"numberScroll"`（数字从 0 滚动到目标值） |

### 3.2 事件字段

事件统一挂在 `events` 字段下，当前支持 `click` 事件：

```json
"events": {
  "click": {
    "type": "navigate",     // 事件类型: navigate / track / toast / dismiss
    "route": "",            // navigate 时的目标路由
    "eventId": "",          // track 时的埋点 ID
    "message": "",          // toast 时的提示文案
    "closeDialog": true     // 是否同时关闭弹窗
  }
}
```

| type | 行为 |
|---|---|
| `navigate` | 页面跳转（通常配合 `closeDialog: true`） |
| `track` | 埋点上报 |
| `toast` | 弹出 Toast 提示 |
| `dismiss` | 仅关闭弹窗 |

### 3.3 数据绑定与表达式

DSL 中文本类字段（`text`、`src` 等）支持两种动态能力：

**简单占位符 `{key}`**

从运行时注入的 `DataContext.fields` Map 中取值替换：

```json
"text": "{discount}"    // DataContext 中 discount = "1"  →  渲染为 "1"
"text": "{unit}"        // DataContext 中 unit = "折"     →  渲染为 "折"
```

**三元表达式 `{key == 'val' ? 'A' : 'B'}`**

在 DSL 模板内完成条件判断，无需调用方额外传参：

```json
"text": "{userLevel == 'SVIP' ? 'SVIP专属优惠限时开启' : '确定要放弃VIP优惠吗？'}"
"src":  "{userLevel == 'SVIP' ? 'svip' : 'vip'}"
```

支持 `==` 和 `!=` 两种运算符。

**使用原则**

| 场景 | 用数据绑定 | 用表达式 |
|---|---|---|
| 文案由后端/外部动态下发 | ✅ | — |
| 文案差异由模板内字段（如身份）决定 | — | ✅ |
| 数值型动态参数 | ✅ | — |

## 4. 嵌套结构

以 VIP 挽留弹窗为例，展示 UI 树与 DSL JSON 的对应关系：

```
dialog                          ← 弹窗根节点（蒙层 + 卡片）
└── column (main_content)       ← 主容器，垂直居中排列
    ├── row (badge_wrapper)     ← 横向容器，把角标顶到最左
    │   └── image (badge_img)   ← VIP/SVIP 角标（表达式切换）
    ├── text (title)            ← 主标题（表达式切换）
    ├── row (discount_wrapper)  ← 横向容器，一大一小文字基线对齐
    │   ├── animatedText        ← 折扣数字（从 0 滚动）
    │   └── text                ← 折扣单位
    ├── text (subtitle)         ← 副标题（表达式切换）
    ├── carousel                ← 权益图标轮播（4 个/页，自动播放，无限循环）
    ├── button (btn_stay)       ← 主按钮 - 红色高亮（表达式切换）
    └── button (btn_leave)      ← 次按钮 - 透明纯文本（表达式切换）
```

## 5. 最小完整 DSL（JSON）

```json
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
              "margin": { "top": "10dp" }
            },
            "children": [
              {
                "type": "image",
                "id": "vip_badge_img",
                "props": {
                  "src": "{userLevel == 'SVIP' ? 'svip' : 'vip'}",
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
              "margin": { "top": "6dp", "bottom": "12dp" }
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
```

## 6. 明确能力边界

### 支持的能力

- **容器组件**：`dialog`（弹窗根节点）、`column`（垂直布局）、`row`（水平布局）、`box`（层叠布局）
- **基础组件**：`text`（文本）、`image`（图片）、`button`（按钮）
- **动态组件**：`carousel`（轮播，支持自动播放 + 无限循环）、`animatedText`（数字滚动动画）
- **常用样式**：宽高、颜色、字号、间距（margin/padding/gap）、圆角、对齐、阴影、透明度
- **数据绑定**：`{key}` 占位符 + 三元表达式 `{key == 'val' ? 'A' : 'B'}`
- **基础事件**：`click` 事件，支持 navigate / track / toast / dismiss 四种类型
- **完整校验**：非法尺寸、非法颜色等解析失败时降级为错误页面，不影响宿主 App

### 不支持的能力

- 原生复杂组件（视频播放器、地图、WebView）
- 复杂动画（关键帧动画、过渡动画等；仅支持数字滚动）
- 嵌套滚动、手势、拖拽等复杂交互
- 多事件类型（仅支持 click；不支持长按、双击等）
