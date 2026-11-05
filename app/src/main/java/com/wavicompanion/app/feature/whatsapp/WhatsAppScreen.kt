package com.wavicompanion.app.feature.whatsapp

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.webkit.PermissionRequest
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wavicompanion.app.core.common.NetworkMonitor
import com.wavicompanion.app.core.datastore.UserPreferencesRepository
import com.wavicompanion.app.core.webview.WebViewManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WhatsAppViewModel @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val userPreferencesRepository: UserPreferencesRepository,
    val webViewManager: WebViewManager,
) : ViewModel() {

    val isOnline = networkMonitor.isOnline

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadingProgress = MutableStateFlow(0)
    val loadingProgress: StateFlow<Int> = _loadingProgress.asStateFlow()

    private val _hasError = MutableStateFlow(false)
    val hasError: StateFlow<Boolean> = _hasError.asStateFlow()

    fun setLoading(loading: Boolean) { _isLoading.value = loading }
    fun setProgress(progress: Int) { _loadingProgress.value = progress }
    fun setError(error: Boolean) { _hasError.value = error }

    fun markSessionActive() {
        viewModelScope.launch {
            userPreferencesRepository.setHasActiveSession(true)
        }
    }
}

private val ALLOWED_HOSTS = setOf(
    "web.whatsapp.com",
    "whatsapp.com",
    "www.whatsapp.com",
    "static.whatsapp.net",
    "mmg.whatsapp.net",
    "media.whatsapp.net",
    "pps.whatsapp.net",
)

private fun isAllowedUrl(url: String): Boolean {
    return try {
        val uri = Uri.parse(url)
        val host = uri.host ?: return false
        ALLOWED_HOSTS.any { host == it || host.endsWith(".$it") }
    } catch (_: Exception) {
        false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsAppScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: WhatsAppViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isOnline by viewModel.isOnline.collectAsState(initial = true)
    val isLoading by viewModel.isLoading.collectAsState()
    val loadingProgress by viewModel.loadingProgress.collectAsState()
    val hasError by viewModel.hasError.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var fileUploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    val activity = context as? Activity
    DisposableEffect(Unit) {
        activity?.window?.let { window ->
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, true)
        }
        onDispose {
            activity?.window?.let { window ->
                androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
            }
        }
    }

    val webView = remember { viewModel.webViewManager.getOrCreate() }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        fileUploadCallback?.onReceiveValue(
            if (uri != null) arrayOf(uri) else emptyArray()
        )
        fileUploadCallback = null
    }

    var pendingPermissionRequest by remember { mutableStateOf<PermissionRequest?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        pendingPermissionRequest?.let { req ->
            val granted = req.resources.filter { resource ->
                when (resource) {
                    PermissionRequest.RESOURCE_VIDEO_CAPTURE ->
                        grants[Manifest.permission.CAMERA] == true
                    PermissionRequest.RESOURCE_AUDIO_CAPTURE ->
                        grants[Manifest.permission.RECORD_AUDIO] == true
                    else -> false
                }
            }
            if (granted.isNotEmpty()) {
                req.grant(granted.toTypedArray())
            } else {
                req.deny()
            }
            pendingPermissionRequest = null
        }
    }

    BackHandler {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            onBack()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> webView.onResume()
                Lifecycle.Event.ON_PAUSE -> webView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = {
                Text("This will clear your WhatsApp Web session. You will need to scan the QR code again to reconnect.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.webViewManager.clearSessionData()
                        onLogout()
                    },
                ) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WhatsApp") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { webView.reload() }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Reload")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Reload") },
                            onClick = {
                                showMenu = false
                                webView.reload()
                            },
                            leadingIcon = { Icon(Icons.Outlined.Refresh, null) },
                        )
                        DropdownMenuItem(
                            text = { Text("Clear cache") },
                            onClick = {
                                showMenu = false
                                viewModel.webViewManager.clearCache()
                            },
                            leadingIcon = { Icon(Icons.Outlined.DeleteOutline, null) },
                        )
                        DropdownMenuItem(
                            text = { Text("Open in browser") },
                            onClick = {
                                showMenu = false
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.whatsapp.com"))
                                context.startActivity(intent)
                            },
                            leadingIcon = { Icon(Icons.Outlined.OpenInBrowser, null) },
                        )
                        DropdownMenuItem(
                            text = { Text("Logout") },
                            onClick = {
                                showMenu = false
                                showLogoutDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.AutoMirrored.Outlined.Logout,
                                    null,
                                    tint = MaterialTheme.colorScheme.error,
                                )
                            },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            AndroidView(
                factory = {
                    (webView.parent as? android.view.ViewGroup)?.removeView(webView)

                    webView.webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?,
                        ): Boolean {
                            val url = request?.url?.toString() ?: return false
                            return if (isAllowedUrl(url)) {
                                false
                            } else {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                                true
                            }
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            viewModel.setLoading(false)
                            viewModel.setError(false)
                            viewModel.markSessionActive()
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            request: WebResourceRequest?,
                            error: WebResourceError?,
                        ) {
                            if (request?.isForMainFrame == true) {
                                viewModel.setError(true)
                                viewModel.setLoading(false)
                            }
                        }
                    }

                    webView.webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView?, newProgress: Int) {
                            viewModel.setProgress(newProgress)
                            if (newProgress == 100) {
                                viewModel.setLoading(false)
                            }
                        }

                        override fun onShowFileChooser(
                            webView: WebView?,
                            callback: ValueCallback<Array<Uri>>?,
                            params: FileChooserParams?,
                        ): Boolean {
                            fileUploadCallback?.onReceiveValue(null)
                            fileUploadCallback = callback
                            fileChooserLauncher.launch("*/*")
                            return true
                        }

                        override fun onPermissionRequest(request: PermissionRequest?) {
                            request?.let { req ->
                                val resources = req.resources
                                val permissions = mutableListOf<String>()
                                if (resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)) {
                                    permissions.add(Manifest.permission.CAMERA)
                                }
                                if (resources.contains(PermissionRequest.RESOURCE_AUDIO_CAPTURE)) {
                                    permissions.add(Manifest.permission.RECORD_AUDIO)
                                }
                                if (permissions.isNotEmpty()) {
                                    pendingPermissionRequest = req
                                    permissionLauncher.launch(permissions.toTypedArray())
                                } else {
                                    req.deny()
                                }
                            }
                        }
                    }

                    if (webView.url == null) {
                        webView.loadUrl("https://web.whatsapp.com")
                    }

                    webView
                },
                modifier = Modifier.fillMaxSize(),
                onRelease = { wv ->
                    (wv.parent as? android.view.ViewGroup)?.removeView(wv)
                },
            )

            if (!isOnline || hasError) {
                NetworkErrorState(
                    onRetry = {
                        viewModel.setError(false)
                        webView.reload()
                    },
                )
            }

            AnimatedVisibility(
                visible = isLoading && isOnline,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                LinearProgressIndicator(
                    progress = { loadingProgress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                )
            }
        }
    }
}

@Composable
private fun NetworkErrorState(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.WifiOff,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Connection Error",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Could not connect to WhatsApp Web. Please check your internet connection and try again.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            TextButton(onClick = onRetry) {
                Icon(Icons.Outlined.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Retry")
            }
        }
    }
}
