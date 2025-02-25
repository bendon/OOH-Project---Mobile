package com.edgetech.bbscout.features.onboarding_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diracks.app.app.app_state.BBScoutAppState
import com.edgetech.bbscout.R
import com.edgetech.bbscout.components.utils.setPreference
import com.edgetech.bbscout.features.dashboard.BBScoutDashboard
import com.edgetech.bbscout.features.navigation.AppDestinations
import com.edgetech.bbscout.ui.theme.mainBlue
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    appState: BBScoutAppState?
) {

    val context = LocalContext.current

    val pages = listOf(
        OnboardingPage(
            title = "Welcome to Billboard Hunter",
            description = "Join our community of advertisers mapping billboards across the globe",
            icon = R.drawable.map_mono
        ),
        OnboardingPage(
            title = "Capture & Earn",
            description = "Earn points and rewards for each verified billboard capture",
            icon = R.drawable.camera_svgrepo_com
        ),
        OnboardingPage(
            title = "Track progress",
            description = "Monitor your impact and climb the leaderboard",
            icon = R.drawable.award_badge
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == pages.size - 1){
            setPreference(context, "ONBOARDING_SHOWN", true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(mainBlue), // Primary blue
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
        ) {
            pages.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            if (pagerState.currentPage == index) Color.White else Color.Gray,
                            shape = CircleShape
                        )
                        .padding(4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { page ->
            OnboardingPageView(pages[page], modifier = Modifier)
        }

     //   Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = {
                    if (pagerState.currentPage - 1  >= 0) {
                        appState?.coroutineScope?.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }

                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Back", color = Color.White)
            }

            Button(
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = mainBlue),
                onClick = {
                    if (pagerState.currentPage + 1 < pages.size) {
                        appState?.coroutineScope?.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }

                    } else {
                        appState?.navController?.navigate(AppDestinations.Dashboard)
                    }
                },
                modifier = Modifier.padding(16.dp),
                shape = MaterialTheme.shapes.small
            ) {

                Text(if (pagerState.currentPage == pages.size - 1) "Get started" else "Next")
            }
        }
    }
}

@Composable
fun OnboardingPageView(page: OnboardingPage, modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        Image(
            painter = painterResource(id = page.icon),
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = page.description,
            fontSize = 16.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

data class OnboardingPage(val title: String, val description: String, val icon: Int)

