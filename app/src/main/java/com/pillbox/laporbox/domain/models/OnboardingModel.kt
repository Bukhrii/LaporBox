package com.pillbox.laporbox.domain.models

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.pillbox.laporbox.R

sealed class OnboardingModel(
    @RawRes val image: Int,
    @StringRes val title: Int,
    @StringRes val description: Int?
) {
    data object FirstPages: OnboardingModel(
        image = R.raw.page1,
        title = R.string.onboarding_title_first,
        description = R.string.onboarding_description_first
    )

    data object SecondPages: OnboardingModel(
        image = R.raw.page2,
        title = R.string.onboarding_title_second,
        description = R.string.onboarding_description_second
    )

    data object ThirdPages: OnboardingModel(
        image = R.raw.page3,
        title = R.string.onboarding_title_third,
        description = R.string.onboarding_description_third
    )

    data object FourthPages: OnboardingModel(
        image = R.raw.page4,
        title = R.string.onboarding_title_fourth,
        description = R.string.onboarding_description_fourth
    )

    data object FifthPages: OnboardingModel(
        image = R.raw.page5,
        title = R.string.onboarding_title_fifth,
        description = null
    )
}