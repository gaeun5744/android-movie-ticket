package woowacourse.movie.model

data class ReservationResultUiModel(
    val title: String,
    val cancelDeadLine: Int,
    val date: String,
    val headCount: Int,
    val totalPrice: Int,
)