package woowacourse.movie.moviereservation

import woowacourse.movie.moviereservation.uimodel.HeadCountUiModel
import woowacourse.movie.moviereservation.uimodel.MovieReservationUiModel
import woowacourse.movie.moviereservation.uimodel.ScreeningDateTimeUiModel

interface MovieReservationContract {
    interface View {
        fun showMovieInfo(reservation: MovieReservationUiModel)

        fun showCantDecreaseError(minCount: Int)

        fun showScreeningDateTime(screeningDateTimeUiModels: List<ScreeningDateTimeUiModel>)

        fun updateHeadCount(updatedCount: HeadCountUiModel)

        fun navigateToReservationResultView(reservationId: Long)

        fun showScreeningMovieError()

        fun showMovieReservationError()
    }

    interface Presenter {
        fun loadMovieDetail(screenMovieId: Long)

        fun plusCount(currentCount: HeadCountUiModel)

        fun minusCount(currentCount: HeadCountUiModel)

        fun completeReservation(
            screenMovieId: Long,
            currentCount: HeadCountUiModel,
        )
    }
}
