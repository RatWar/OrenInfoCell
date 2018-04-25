package com.besaba.anvarov.oreninfocell

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.telephony.CellLocation
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyManager
import android.telephony.gsm.GsmCellLocation
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar
import com.besaba.anvarov.oreninfocell.Ads.showBottomBanner

class MainActivity : AppCompatActivity() {

    private lateinit var mAddr: TextView
    lateinit var st: String
    lateinit var tm: TelephonyManager
    private lateinit var mProgress: RoundCornerProgressBar
    //int g1[] = {4, 11};
    //int g2[] = {1, 2, 5, 7};
    //int g3[] = {3, 6, 8, 9, 10, 12, 14, 15};
    //int g4[] = {13};
    internal var netType = arrayOf("UNKNOWN", "EDGE", "GPRS", "UMTS", "CDMA", "EVDO_0", "EVDO_A", "1xRTT", "HSDPA", "HSUPA", "HSPA", "IDEN", "EVDO_B", "LTE", "EHPRD", "HSPAP")

    private val listener = object : PhoneStateListener() {
        override fun onCellLocationChanged(location: CellLocation) {
            val gctLoc = location as GsmCellLocation
            val mCell: TextView = findViewById(R.id.tvCell)
            mCell.text = (cellText(gctLoc.cid) + " ("
                    + gctLoc.lac.toString() + ")")
        }

        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            super.onSignalStrengthsChanged(signalStrength)
            val sig: Int
            val lev: Int
            if (typeNetworkToInt(tm.networkType) < 4) {
                lev = signalStrength.gsmSignalStrength
                sig = asu2dbm(lev)
            } else {
                val ssignal = signalStrength.toString()
                val parts = ssignal.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                lev = Integer.parseInt(parts[8]) * 2 - 113
                sig = asu2dbm(lev)
            }
            val mSignal: TextView = findViewById(R.id.tvSignal)
            mSignal.text = (dbm2text(sig) + "   (ASU=" + lev + ")" + "   "
                    + netType[tm.networkType])
            progressColor(lev)
            progressCur(lev)

        }

        private fun cellText(nCell: Int): String {
            st = ((nCell and 65535) + 10000).toString()
            return st.substring(1, 4) + "-" + st[4]
        }

        private fun typeNetworkToInt(typeNetwork: Int): Int {
            return when (typeNetwork) {
                1, 2 -> 2
                13 -> 4
                else -> 3
            }
        }

        private fun asu2dbm(nASU: Int): Int {
            return when {
                nASU == 99 -> -1
                nASU > 31 -> -51
                else -> -113 + 2 * nASU
            }
        }

        private fun dbm2text(ndbm: Int): String {
            return if (ndbm == -1) R.string.txtUnknown.toString() else ndbm.toString() + " dBm"
        }

        /*
    Arbitrary Strength Unit (ASU) is an integer value proportional to the received signal strength measured by the mobile phone.
    It is possible to calculate the real signal strength measured in dBm
    (and thereby power in Watts) by a formula. However, there are different formulas for 2G, 3G and 4G networks.

    In GSM networks, ASU maps to RSSI (received signal strength indicator, see TS 27.007[1] sub clause 8.5).
    dBm = 2 × ASU - 113, ASU in the range of 0..31 and 99 (for not known or not detectable).

    In UMTS networks, ASU maps to RSCP level (received signal code power, see TS 27.007[1] sub clause 8.69 and TS 27.133 sub clause 9.1.1.3).
    dBm = ASU - 116, ASU in the range of -5..91 and 255 (for not known or not detectable).

    In LTE networks, ASU maps to RSRP (reference signal received power, see TS 36.133, sub-clause 9.1.4).
    The valid range of ASU is from 0 to 97. For the range 1 to 96, ASU maps to
            (ASU - 141) ≤ dBm < (ASU - 140).
    The value of 0 maps to RSRP below -140 dBm and the value of 97 maps to RSRP above -44 dBm.

    ASU shouldn't be confused with "Active Set Update".
    The Active Set Update is a signalling message used in handover procedures of UMTS and CDMA mobile telephony standards.

    On Android phones, the acronym ASU has nothing to do with Active Set Update.
    It has not been declared precisely by Google developers.
    */
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        showBottomBanner(activity = this)

        mAddr = findViewById<View>(R.id.tvAddress) as TextView
        mAddr.setText(R.string.txtNotAddress)

        tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        tm.listen(listener, PhoneStateListener.LISTEN_CELL_LOCATION or PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)

        mProgress = findViewById<View>(R.id.progress) as RoundCornerProgressBar
        mProgress.progressColor = Color.RED
        mProgress.max = progressMax(false).toFloat()
        mProgress.progress = 10f

        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        return if (id == R.id.action_settings) {
            true
        } else super.onOptionsItemSelected(item)

    }

    private fun progressMax(isLTE: Boolean): Int {
        return if (isLTE) 97 else 31
    }

    private fun progressColor(asu: Int) {
        val color = asu / 4
        when (color) {
            0 -> mProgress.progressColor = Color.RED
            1 -> mProgress.progressColor = Color.YELLOW
            2 -> mProgress.progressColor = Color.GREEN
            else -> mProgress.progressColor = Color.BLUE
        }
    }

    private fun progressCur(asu: Int) {
        mProgress.progress = asu.toFloat()
    }

}
