package woowacourse.movie.selectseat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.CheckBox
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import woowacourse.movie.R
import woowacourse.movie.data.DummyMovies
import woowacourse.movie.moviereservation.uimodel.BookingInfoUiModel
import woowacourse.movie.reservationresult.ReservationResultActivity
import woowacourse.movie.selectseat.uimodel.PriceUiModel
import woowacourse.movie.selectseat.uimodel.SeatUiModel
import woowacourse.movie.selectseat.uimodel.SeatsUiModel
import woowacourse.movie.selectseat.uimodel.SelectResult
import woowacourse.movie.util.bundleParcelable
import woowacourse.movie.util.intentParcelable

class SelectSeatActivity : AppCompatActivity(), SelectSeatContract.View {
    private lateinit var seatTable: TableLayout
    private lateinit var title: TextView
    private lateinit var price: TextView
    private lateinit var reserveBtn: Button
    private lateinit var presenter: SelectSeatContract.Presenter
    private lateinit var seats: SeatsUiModel
    private lateinit var bookingInfoUiModel: BookingInfoUiModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_seat)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        bookingInfoUiModel =
            intent.intentParcelable(EXTRA_BOOKING_ID, BookingInfoUiModel::class.java)
                ?: error("bookingInfo에 대한 정보가 없습니다.")
        presenter = SelectSeatPresenter(this, DummyMovies)

        initView()
        clickReserveButton()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable(EXTRA_SEATS_ID, seats)
    }

    override fun onRestoreInstanceState(
        savedInstanceState: Bundle,
    ) {
        super.onRestoreInstanceState(savedInstanceState)

        val selectedSeats =
            savedInstanceState.bundleParcelable(EXTRA_SEATS_ID, SeatsUiModel::class.java)
        selectedSeats?.let {
            seats = selectedSeats
            seats.selectedSeats().forEach {
                val seatView = tableChildView(it.row, it.col)
                seatView.isChecked = true
            }
            presenter.calculatePrice(seats.selectedSeats())
        }
        reserveBtn.isEnabled = seats.selectedSeats().isNotEmpty()
    }

    private fun initView() {
        seatTable = findViewById<TableLayout>(R.id.tl_select_seat)
        title = findViewById(R.id.tv_select_seat_title)
        price = findViewById(R.id.tv_select_seat_price)
        reserveBtn = findViewById(R.id.btn_select_seat_reserve)
        reserveBtn.setOnClickListener {
            presenter.completeReservation(bookingInfoUiModel, seats.seats)
        }

        presenter.loadSeat(bookingInfoUiModel.movieId)
        presenter.loadReservationInfo(bookingInfoUiModel.movieId)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showSeat(theaterSeats: List<SeatUiModel>) {
        seats = SeatsUiModel(theaterSeats)
        theaterSeats.forEach { seatUiModel ->
            val seatView: CheckBox = tableChildView(seatUiModel.row, seatUiModel.col)
            seatView.text = seatUiModel.showPosition
            seatView.setTextColor(getColor(seatUiModel.rateColor.color))
            seatView.setOnClickListener {
                updateDate(seatUiModel)
            }
        }
    }

    private fun updateDate(seatUiModel: SeatUiModel) {
        seats = seats.changeState(seatUiModel)
        presenter.calculatePrice(seats.selectedSeats())
        reserveBtn.isEnabled = seats.selectedSeats().isNotEmpty()
    }

    override fun showMovieInfo(
        title: String,
        priceUiModel: PriceUiModel,
    ) {
        this.title.text = title
        this.price.text = priceUiModel.price
    }

    override fun updatePrice(updatedPrice: PriceUiModel) {
        price.text = updatedPrice.price
    }

    override fun navigateToResult(reservationId: Long) {
        startActivity(ReservationResultActivity.getIntent(this, reservationId))
    }

    private fun tableChildView(
        row: Int,
        col: Int,
    ): CheckBox {
        val tableRow = seatTable.getChildAt(row) as TableRow
        return tableRow.getChildAt(col) as CheckBox
    }

    private fun clickReserveButton() {
        reserveBtn.setOnClickListener {
            showClickResult()
        }
    }

    private fun showClickResult() {
        when (val selectResult = selectResult()) {
            is SelectResult.Exceed ->
                Toast.makeText(
                    this,
                    selectResult.message,
                    Toast.LENGTH_SHORT,
                ).show()

            is SelectResult.LessSelect ->
                Toast.makeText(
                    this,
                    selectResult.message,
                    Toast.LENGTH_SHORT,
                ).show()

            is SelectResult.Success -> confirmAlertDialog()
        }
    }

    private fun confirmAlertDialog() =
        AlertDialog.Builder(this)
            .setTitle("예매 확인")
            .setMessage("정말 예매하시겠습니까?")
            .setCancelable(false)
            .setPositiveButton("예매 완료") { _, _ ->
                presenter.completeReservation(
                    bookingInfoUiModel,
                    seats.selectedSeats(),
                )
            }
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }.show()

    private fun selectResult(): SelectResult {
        if (bookingInfoUiModel.maxSelectSize() < seats.selectedSeats().size) {
            return SelectResult.Exceed(
                "예매 인원을 초과하여 좌석을 선택할 수 없습니다.",
            )
        } else if (bookingInfoUiModel.maxSelectSize() > seats.selectedSeats().size) {
            return SelectResult.LessSelect(
                "좌석을 더 선택해주세요",
            )
        } else {
            return SelectResult.Success
        }
    }

    companion object {
        private const val EXTRA_BOOKING_ID: String = "bookingId"
        private const val EXTRA_SEATS_ID: String = "seatsId"

        fun getIntent(
            context: Context,
            bookingInfoUiModel: BookingInfoUiModel,
        ): Intent {
            return Intent(context, SelectSeatActivity::class.java).apply {
                putExtra(EXTRA_BOOKING_ID, bookingInfoUiModel)
            }
        }
    }
}
