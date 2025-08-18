package com.carlitoswy.flashmeet.presentation.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.carlitoswy.flashmeet.R
import com.carlitoswy.flashmeet.datastore.OnboardingPrefs
import com.carlitoswy.flashmeet.localization.LocalAppLanguage
import com.carlitoswy.flashmeet.ui.navigation.Routes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val appLang = LocalAppLanguage.current
    val currentLanguage by rememberUpdatedState(newValue = appLang)

    val pages = listOf(
        OnboardingPage(
            stringResource(R.string.onboarding_page_1_title),
            stringResource(R.string.onboarding_page_1_desc),
            "onboarding_map.json"
        ),
        OnboardingPage(
            stringResource(R.string.onboarding_page_2_title),
            stringResource(R.string.onboarding_page_2_desc),
            "onboarding_create.json"
        ),
        OnboardingPage(
            stringResource(R.string.onboarding_page_3_title),
            stringResource(R.string.onboarding_page_3_desc),
            "onboarding_share.json"
        )
    )

    val pagerState = rememberPagerState { pages.size }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    if (pagerState.currentPage < pages.lastIndex) {
                        TextButton(onClick = {
                            scope.launch {
                                OnboardingPrefs.setHasSeenOnboarding(context, true)
                                navController.navigate(Routes.AUTH) {
                                    popUpTo(Routes.ONBOARDING) { inclusive = true }
                                }
                            }
                        }) {
                            Text(stringResource(R.string.onboarding_skip))
                        }
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(pages.size) { index ->
                            val color = if (pagerState.currentPage == index)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(10.dp)
                                    .background(color, shape = MaterialTheme.shapes.small)
                            )
                        }
                    }
                },
                floatingActionButton = {
                    val isLastPage = pagerState.currentPage == pages.lastIndex
                    ExtendedFloatingActionButton(
                        onClick = {
                            scope.launch {
                                if (isLastPage) {
                                    OnboardingPrefs.setHasSeenOnboarding(context, true)
                                    navController.navigate(Routes.AUTH) {
                                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                                    }
                                } else {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            }
                        }
                    ) {
                        Text(
                            if (isLastPage)
                                stringResource(R.string.onboarding_start)
                            else
                                stringResource(R.string.onboarding_next)
                        )
                    }
                }
            )
        }
    ) { padding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) { pageIndex ->
            OnboardingPageContent(pages[pageIndex])
        }
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset(page.lottieAsset))
    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

data class OnboardingPage(val title: String, val description: String, val lottieAsset: String)
