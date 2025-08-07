package com.pillbox.laporbox.domain.models

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import com.pillbox.laporbox.R

sealed class OnboardingModel(
    @DrawableRes val image: Int,
    @StringRes val title: Int,
    @StringRes val description: Int?
) {
    data object FirstPages: OnboardingModel(
        image = R.drawable.page1,
        title = R.string.onboarding_title_first,
        description = null
    )

    data object SecondPages: OnboardingModel(
        image = R.drawable.page2,
        title = R.string.onboarding_title_second,
        description = R.string.onboarding_description_second
    )

    data object ThirdPages: OnboardingModel(
        image = R.drawable.page3,
        title = R.string.onboarding_title_third,
        description = R.string.onboarding_description_third
    )

    data object FourthPages: OnboardingModel(
        image = R.drawable.page4,
        title = R.string.onboarding_title_fourth,
        description = R.string.onboarding_description_fourth
    )

    data object FifthPages: OnboardingModel(
        image = R.drawable.page5,
        title = R.string.onboarding_title_fifth,
        description = null
    )

    data object SixthPages: OnboardingModel(
        image = R.drawable.page6,
        title = R.string.onboarding_title_sixth,
        description = null
    )

    data object SeventhPages: OnboardingModel(
        image = R.drawable.page7,
        title = R.string.onboarding_title_seventh,
        description = null
    )

    data object EighthPages: OnboardingModel(
        image = R.drawable.page7,
        title = R.string.onboarding_title_eighth,
        description = null
    )

    data object NinthPages: OnboardingModel(
        image = R.drawable.page7,
        title = R.string.onboarding_title_ninth,
        description = null
    )
}