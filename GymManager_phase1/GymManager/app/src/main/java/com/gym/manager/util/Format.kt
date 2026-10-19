package com.gym.manager.util

import java.util.Locale

fun Long.asMoney(): String = String.format(Locale.ENGLISH, "%,d", this) + " ج.م"
