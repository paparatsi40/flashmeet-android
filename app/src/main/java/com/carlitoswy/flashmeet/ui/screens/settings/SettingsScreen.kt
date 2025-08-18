package com.carlitoswy.flashmeet.ui.screens.settings

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.carlitoswy.flashmeet.R
import com.carlitoswy.flashmeet.localization.LocalAppLanguage
import com.carlitoswy.flashmeet.ui.components.LanguageSelectorDropdown
import com.carlitoswy.flashmeet.ui.navigation.Routes
import com.carlitoswy.flashmeet.utils.LocaleManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val appLang = LocalAppLanguage.current
    val selectedLang = remember { mutableStateOf(appLang.value) }
    val scope = rememberCoroutineScope()
    var restartOnboarding by remember { mutableStateOf(false) }

    // Cargar preferencia onboarding
    LaunchedEffect(Unit) {
        restartOnboarding = !com.carlitoswy.flashmeet.datastore.OnboardingPrefs.hasSeenOnboarding(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(24.dp)
        ) {
            // 🌍 Selector de idioma
            LanguageSelectorDropdown(
                selectedLang = selectedLang.value,
                onLanguageSelected = { langCode ->
                    selectedLang.value = langCode
                    LocaleManager.setAndSaveLocale(context, langCode)
                    appLang.value = langCode
                    (context as Activity).recreate()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 🔄 Control Onboarding
            Text(text = "Onboarding", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Mostrar onboarding al iniciar",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = restartOnboarding,
                    onCheckedChange = { checked ->
                        restartOnboarding = checked
                        scope.launch {
                            com.carlitoswy.flashmeet.datastore.OnboardingPrefs.setHasSeenOnboarding(context, !checked)
                        }
                        Toast.makeText(
                            context,
                            if (checked) "Se mostrará el onboarding al iniciar" else "Onboarding desactivado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        com.carlitoswy.flashmeet.datastore.OnboardingPrefs.setHasSeenOnboarding(context, false)
                    }
                    navController.navigate(Routes.ONBOARDING)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver tutorial ahora")
            }
        }
    }
}
