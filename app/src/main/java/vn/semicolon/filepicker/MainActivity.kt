package vn.semicolon.filepicker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

internal class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        main_tv.setOnClickListener {
            FilePicker.Builder()
                .description("Chọn ảnh")
                .typesOf(FilePicker.TYPE_IMAGE)
                .start(this, 0)
        }
    }
}
