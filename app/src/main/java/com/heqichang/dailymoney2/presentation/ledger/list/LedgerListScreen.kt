package com.heqichang.dailymoney2.presentation.ledger.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.heqichang.dailymoney2.domain.model.Ledger
import com.heqichang.dailymoney2.presentation.shared.LedgerSelectionViewModel
import kotlinx.coroutines.launch

@Composable
fun LedgerListRoute(
    selectionViewModel: LedgerSelectionViewModel,
    onNavigateToLedgerDetail: (Long) -> Unit,
    viewModel: LedgerListViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val sharedLedgerId by selectionViewModel.selectedLedgerId.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(sharedLedgerId) {
        if (sharedLedgerId != null && sharedLedgerId != uiState.selectedLedgerId) {
            viewModel.onAction(LedgerListAction.SelectLedger(sharedLedgerId))
        }
    }
    LaunchedEffect(uiState.selectedLedgerId) {
        if (uiState.selectedLedgerId != null && uiState.selectedLedgerId != sharedLedgerId) {
            selectionViewModel.selectLedger(uiState.selectedLedgerId)
        }
    }
    
    // 监听导入成功，导航到新导入的账本
    LaunchedEffect(uiState.importSuccess) {
        if (uiState.importSuccess && uiState.selectedLedgerId != null) {
            onNavigateToLedgerDetail(uiState.selectedLedgerId)
            viewModel.onAction(LedgerListAction.ResetImportSuccess)
        }
    }
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        val jsonString = inputStream.bufferedReader().use { it.readText() }
                        val result = viewModel.importLedgerData(jsonString)
                        result.onSuccess { newLedgerId ->
                            viewModel.onAction(LedgerListAction.SetImportSuccess(newLedgerId))
                        }
                    }
                } catch (e: Exception) {
                    // 错误已在 ViewModel 中处理
                }
            }
        }
    }
    
    LedgerListScreen(
        state = uiState,
        onAction = viewModel::onAction,
        onNavigateToLedgerDetail = onNavigateToLedgerDetail,
        onImportLedger = { filePickerLauncher.launch("application/json") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerListScreen(
    state: LedgerListUiState,
    onAction: (LedgerListAction) -> Unit,
    onNavigateToLedgerDetail: (Long) -> Unit,
    onImportLedger: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("账本") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (state.ledgers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "还没有账本，点击下方按钮创建一个吧。",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                ) {
                    items(state.ledgers) { ledger ->
                        LedgerListItem(
                            ledger = ledger,
                            onClick = { onNavigateToLedgerDetail(ledger.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // 新建账本按钮
            Button(
                onClick = { onAction(LedgerListAction.EditLedger(null)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("新建账本")
            }
            
            // 导入账本按钮
            Button(
                onClick = onImportLedger,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Default.Upload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("导入账本")
            }

            state.errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }

    if (state.isEditorVisible && state.editingLedger != null) {
        LedgerEditorDialog(
            ledger = state.editingLedger,
            onDismiss = { onAction(LedgerListAction.DismissEditor) },
            onConfirm = { updated -> onAction(LedgerListAction.SaveLedger(updated)) }
        )
    }
}

@Composable
private fun LedgerListItem(
    ledger: Ledger,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 生成基于ID的图标颜色
    val iconColors = listOf(
        Color(0xFFFFB74D) to Color(0xFFFF9800), // 橙黄渐变
        Color(0xFF81D4FA) to Color(0xFF4FC3F7), // 浅蓝渐变
        Color(0xFFFFAB91) to Color(0xFFFF7043), // 橙粉渐变
        Color(0xFFCE93D8) to Color(0xFFBA68C8), // 紫粉渐变
    )
    val iconColor = iconColors[(ledger.id.toInt() % iconColors.size).coerceAtLeast(0)]
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(0.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(iconColor.first, iconColor.second)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 简单的图标占位符，可以用实际图标替换
                Text(
                    text = ledger.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 账本信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = ledger.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ledger.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LedgerEditorDialog(
    ledger: Ledger,
    onDismiss: () -> Unit,
    onConfirm: (Ledger) -> Unit
) {
    var name by rememberSaveable(ledger.id) { mutableStateOf(ledger.name) }
    var description by rememberSaveable(ledger.id) { mutableStateOf(ledger.description) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (ledger.id == 0L) "新建账本" else "编辑账本") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称") }
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述") }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onConfirm(
                        ledger.copy(
                            name = name.trim(),
                            description = description.trim()
                        )
                    )
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

