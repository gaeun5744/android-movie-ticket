package woowacourse.movie.moviereservation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import woowacourse.movie.R
import woowacourse.movie.data.DummyMovies
import woowacourse.movie.moviereservation.uimodel.HeadCountUiModel
import woowacourse.movie.moviereservation.uimodel.MovieReservationUiModel
import woowacourse.movie.moviereservation.uimodel.ScreeningDateTimesUiModel
import woowacourse.movie.reservationresult.ReservationResultActivity

class MovieReservationActivity : AppCompatActivity(), MovieReservationContract.View {
    private lateinit var presenter: MovieReservationPresenter
    private lateinit var countView: TextView
    private lateinit var plusButton: Button
    private lateinit var minusButton: Button
    private lateinit var dateSpinner: Spinner
    private lateinit var dateAdapter: ArrayAdapter<String>
    private lateinit var timeSpinner: Spinner
    private lateinit var timeAdapter: ArrayAdapter<String>

    private lateinit var movie: MovieReservationUiModel
    private var count: HeadCountUiModel = HeadCountUiModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_movie_reservation)

        val id = intent.getLongExtra(EXTRA_SCREEN_MOVIE_ID, INVALID_SCREEN_MOVIE_ID)
        initView()
        initClickListener(id)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        presenter =
            MovieReservationPresenter(
                this, DummyMovies,
            )

        showInitView(id)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val currentCount = count.count
        outState.putString(STATE_COUNT_ID, currentCount)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val storedCount = savedInstanceState.getString(STATE_COUNT_ID)
        storedCount?.let {
            count = HeadCountUiModel(it)
            countView.text = count.count
        }
    }

    private fun initView() {
        countView = findViewById(R.id.tv_detail_count)
        plusButton = findViewById(R.id.btn_detail_plus)
        minusButton = findViewById(R.id.btn_detail_minus)
        dateSpinner = findViewById(R.id.spinner_detail_date)
        timeSpinner = findViewById(R.id.spinner_detail_time)
    }

    private fun initClickListener(movieId: Long) {
        plusButton.setOnClickListener {
            presenter.plusCount(count)
        }
        minusButton.setOnClickListener {
            presenter.minusCount(count)
        }
        findViewById<Button>(R.id.btn_detail_complete).setOnClickListener {
            presenter.completeReservation(movieId, count)
        }
    }

    private fun showInitView(id: Long) {
        presenter.loadMovieDetail(id)
        countView.text = count.count
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showMovieInfo(reservation: MovieReservationUiModel) {
        movie = reservation
        val (id, title, imageRes, screenDate, description, runningTime) = reservation
        findViewById<ImageView>(R.id.iv_detail_poster).setImageResource(imageRes)
        findViewById<TextView>(R.id.tv_detail_title).text = title
        findViewById<TextView>(R.id.tv_detail_movie_desc).text = description
        findViewById<TextView>(R.id.tv_detail_running_date).text = screenDate
        findViewById<TextView>(R.id.tv_detail_running_time).text = runningTime
    }

    override fun updateHeadCount(updatedCount: HeadCountUiModel) {
        count = updatedCount
        countView.text = count.count
    }

    override fun navigateToReservationResultView(reservationId: Long) {
        startActivity(ReservationResultActivity.getIntent(this, reservationId))
    }

    override fun showScreeningMovieError() {
        Toast.makeText(this, "영화 정보를 불러오는데 실패했습니다. 앱을 다시 실행해주세요.", Toast.LENGTH_SHORT).show()
    }

    override fun showMovieReservationError() {
        Toast.makeText(this, "영화 예매에 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
    }

    override fun showCantDecreaseError(minCount: Int) {
        Toast.makeText(this, "$minCount 명 이상부터 예약할 수 있습니다.", Toast.LENGTH_SHORT).show()
    }

    override fun showScreeningDateTime(screeningDateTimesUiModel: ScreeningDateTimesUiModel) {
        dateAdapter =
            ArrayAdapter(
                this,
                R.layout.item_spinner_date,
                screeningDateTimesUiModel.dates(),
            )

        timeAdapter =
            ArrayAdapter(this, R.layout.item_spinner_date, screeningDateTimesUiModel.defaultTimes())

        dateSpinner.adapter = dateAdapter
        timeSpinner.adapter = timeAdapter

        dateSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long,
                ) {
                    timeAdapter.clear()
                    timeAdapter.addAll(screeningDateTimesUiModel.screeningTimeOfDate(position))
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {}
            }
    }

    companion object {
        const val EXTRA_SCREEN_MOVIE_ID: String = "screenMovieId"
        const val INVALID_SCREEN_MOVIE_ID = -1L
        private const val STATE_COUNT_ID = "count"

        fun getIntent(
            context: Context,
            reservationId: Long,
        ): Intent {
            return Intent(context, MovieReservationActivity::class.java).apply {
                putExtra(EXTRA_SCREEN_MOVIE_ID, reservationId)
            }
        }
    }
}
