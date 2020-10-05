package edu.ufp.pam.exemplos.hit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import edu.ufp.pam.exemplos.R
import kotlinx.android.synthetic.main.activity_main_hit.*

class MainHitActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_hit)

        /*
        * buttonHit.setOnClickListener(object: View.OnClickListener {
            override fun onClick(v: View) {
                var n:Int = textViewHit.text.toString().toInt();
                n++;
                textViewHit.text = "${n}";
            }
        } )
        * */

        buttonHit.setOnClickListener {
            var n: Int = textViewHit.text.toString().toInt();
            n++;
            textViewHit.text = n.toString();
        }
    }
}