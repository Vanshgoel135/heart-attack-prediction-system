package com.haps.android.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Dashboard : Screen("dashboard")
    object Prediction : Screen("prediction")
    object PredictionResult : Screen("prediction_result/{id}") {
        fun createRoute(id: Long) = "prediction_result/$id"
    }
    object History : Screen("history")
    object PredictionDetail : Screen("prediction_detail/{id}") {
        fun createRoute(id: Long) = "prediction_detail/$id"
    }
    object Analytics : Screen("analytics")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object About : Screen("about")
}
