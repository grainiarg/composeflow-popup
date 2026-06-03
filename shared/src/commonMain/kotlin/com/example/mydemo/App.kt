package com.example.mydemo

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.mydemo.retention.RetentionScreen
import com.example.mydemo.retention.RetentionViewModel
import composeflow_popup.shared.generated.resources.Res
import composeflow_popup.shared.generated.resources.background
import org.jetbrains.compose.resources.painterResource

@Composable
fun App() {
    MaterialTheme {
        var showRetention by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            // 第1层：截图背景
            Image(
                painter = painterResource(Res.drawable.background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            // 第2层：透明热区盖在截图的 < 上
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 10.dp, top = 45.dp),
                contentAlignment = Alignment.TopStart,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clickable { showRetention = true },
                )
            }

            // 第3层：弹窗（DialogNode 自带遮罩 + 居中卡片）
            if (showRetention) {
                val viewModel = remember { RetentionViewModel() }
                RetentionScreen(
                    viewModel = viewModel,
                    onDismiss = { showRetention = false },
                )
            }
        }
    }
}
