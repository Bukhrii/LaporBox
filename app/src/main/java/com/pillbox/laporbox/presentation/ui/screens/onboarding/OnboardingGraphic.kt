package com.pillbox.laporbox.presentation.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.pillbox.laporbox.domain.models.OnboardingModel
import com.pillbox.laporbox.presentation.ui.theme.TextHeading
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

@Composable
fun WelcomePage(onboardingModel: OnboardingModel) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Image(
            painter = painterResource(id = onboardingModel.image),
            contentDescription = "Onboarding Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth(),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 50.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(180.dp))

            Text(
                text = stringResource(id = onboardingModel.title),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                lineHeight = MaterialTheme.typography.displayLarge.fontSize * 1.4,
                fontSize = 84.sp
            )
        }
    }
}

@Composable
fun SecondPage(onboardingModel: OnboardingModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 56.dp, vertical = 42.dp)
        .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        Text(
            text = stringResource(id = onboardingModel.title),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displayLarge,
            color = Color.White,
            lineHeight = MaterialTheme.typography.displayLarge.fontSize * 1
        )


        Image(
            painter = painterResource(id = onboardingModel.image),
            contentDescription = "Onboarding Background",
            modifier = Modifier.width(250.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        onboardingModel.description?.let { descriptionResId ->
            Text(
                text = stringResource(id = descriptionResId),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall,
                fontSize = 13.sp,
                color = TextHeading
            )
        }
    }
}

@Composable
fun ThirdPage(onboardingModel: OnboardingModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp, vertical = 42.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        Text(
            text = stringResource(id = onboardingModel.title),
            textAlign = TextAlign.Left,
            style = MaterialTheme.typography.displayLarge,
            color = Color.White,
            lineHeight = MaterialTheme.typography.displayLarge.fontSize * 1,
            modifier = Modifier.padding(start = 46.dp, end = 156.dp, top = 0.dp, bottom = 0.dp)
        )

        Image(
            painter = painterResource(id = onboardingModel.image),
            contentDescription = "Onboarding Background",
            modifier = Modifier.fillMaxSize(),
        )

        Spacer(modifier = Modifier.height(18.dp))

        onboardingModel.description?.let { descriptionResId ->
            Text(
                text = stringResource(id = descriptionResId),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall,
                color = TextHeading,
                modifier = Modifier.padding(horizontal = 46.dp)
            )
        }
    }
}

@Composable
fun FourthPage(onboardingModel: OnboardingModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 0.dp, vertical = 42.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {

        Text(
            text = stringResource(id = onboardingModel.title),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.displayLarge,
            color = Color.White,
            lineHeight = MaterialTheme.typography.displayLarge.fontSize * 1,
            modifier = Modifier.padding(horizontal = 46.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Image(
            painter = painterResource(id = onboardingModel.image),
            contentDescription = "Onboarding Background",
            modifier = Modifier.fillMaxSize(),
        )

        onboardingModel.description?.let { descriptionResId ->
            Text(
                text = stringResource(id = descriptionResId),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleSmall,
                color = TextHeading,
                modifier = Modifier.padding(horizontal = 46.dp)
            )
        }
    }
}

@Composable
fun StartPage(onboardingModel: OnboardingModel) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        Image(
            painter = painterResource(id = onboardingModel.image),
            contentDescription = "Onboarding Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth(),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 50.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(180.dp))

            Text(
                text = stringResource(id = onboardingModel.title),
                textAlign = TextAlign.Right,
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                lineHeight = MaterialTheme.typography.displayLarge.fontSize * 1.1,
                fontSize = 56.sp,
                modifier = Modifier.padding(start = 70.dp)
            )
        }
    }
}

@Composable
fun StartReminderPage(onboardingModel: OnboardingModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(id = onboardingModel.image),
            contentDescription = "Onboarding Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(vertical = 60.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp)
                .verticalScroll(rememberScrollState())
                .align(Alignment.BottomCenter),
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(id = onboardingModel.title),
                textAlign = TextAlign.Left,
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                lineHeight = MaterialTheme.typography.displayLarge.fontSize * 1.1,
                fontSize = 56.sp,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPage(
    onboardingModel: OnboardingModel,
    selectedTime: String, // Waktu yang sudah dipilih (misal: "07:00")
    onTimeSelected: (String) -> Unit // Callback sekarang mengembalikan String waktu
) {
    val timeOptions = remember {
        (0..23).flatMap { hour ->
            listOf(
                String.format(Locale.ROOT, "%02d:00", hour),
                String.format(Locale.ROOT, "%02d:30", hour)
            )
        }
    }
    var isExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        ) {
        Image(
            painter = painterResource(id = onboardingModel.image),
            contentDescription = "Onboarding Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth(),
            )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 50.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            Text(
                text = stringResource(id = onboardingModel.title),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                lineHeight = MaterialTheme.typography.displayLarge.fontSize * 1.1,
                fontSize = 56.sp,
                )

            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it },
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                // Ini adalah tampilan TextField yang terlihat oleh pengguna
                TextField(
                    value = selectedTime,
                    onValueChange = {},
                    readOnly = true,
                    textStyle = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 94.sp,
                        textAlign = TextAlign.Center
                    ),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryEditable)
                )

                // Ini adalah menu dropdown yang muncul saat diklik
                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false }
                ) {
                    timeOptions.forEach { time ->
                        DropdownMenuItem(
                            text = { Text(time) },
                            onClick = {
                                onTimeSelected(time) // Kirim waktu yang dipilih
                                isExpanded = false   // Tutup menu
                            }
                        )
                    }
                }
            }
        }

    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomNavigator(
    pagerState: PagerState,
    totalPages: Int,
    onFinish: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 32.dp, bottom = 40.dp)
    ) {
        if (pagerState.currentPage == 0) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(56.dp).width(150.dp)
                ) {
                    Text("Mulai", style = MaterialTheme.typography.titleMedium)
                }
                TextButton(onClick = onNavigateToLogin) {
                    Text(
                        "Saya sudah memiliki akun",
                        color = Color.Black,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        } else {
            val buttonText = when (pagerState.currentPage) {
                0 -> "Mulai"
                1 -> "Selanjutnya  >"
                2 -> "Selanjutnya  >"
                3 -> "Selanjutnya  >"
                4 -> "Selanjutnya  >"
                5 -> "Buat Pengingat  >"
                6 -> "Selanjutnya  >"
                7 -> "Selanjutnya  >"
                else -> "Buat Pengingat  >"
            }
            Button(
                onClick = {
                    if (pagerState.currentPage == totalPages - 1) {
                        onFinish()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                modifier = Modifier.align(Alignment.Center).height(56.dp).width(230.dp)
            ) {
                Text(
                    text = buttonText,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}