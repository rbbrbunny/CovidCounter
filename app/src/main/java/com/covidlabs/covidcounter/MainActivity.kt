package com.covidlabs.covidcounter

import android.util.Log
import android.app.Dialog
import android.view.Window

import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

import android.os.AsyncTask
import android.os.Bundle
import android.os.CountDownTimer

import android.view.KeyEvent
import android.view.View

import android.widget.Toast

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements


private const val TAG = "MyActivity"
private lateinit var timer: CountDownTimer

class MainActivity : AppCompatActivity() {

    var stringedHTML: String = ""
    var redrawName: String = "Total:"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getItAllDoneMan()

        val doc: Document = Jsoup.parse(stringedHTML)
        redrawTable(doc, redrawName)

        viewButton.setOnClickListener {

            checkForData(doc)
            selectCountryField.clearFocus()
        }
        selectCountryField.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->

            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {

                checkForData(doc)
                selectCountryField.clearFocus()
            }
            false
        })
    }

    fun checkForData(doc: Document){

        if(selectCountryField.text.toString() != ""){

            redrawName = selectCountryField.text.toString()
            redrawTable(doc, redrawName)
        }
        else{

            val message = Toast.makeText(applicationContext, R.string.errorNoCountry, Toast.LENGTH_LONG)
            message.show()
        }
    }

    fun getItAllDoneMan(){

        timer = object : CountDownTimer(100000, 1000) {

            override fun onFinish() = getItAllDoneMan()
            override fun onTick(millisUntilFinished: Long) {

                var secondsToRefresh = millisUntilFinished / 1000
                progressBar.progress = secondsToRefresh.toInt()
            }
        }.start()

        var html = getHTMLdataTask().execute()
        stringedHTML = html.get()

        var doc: Document = Jsoup.parse(stringedHTML)

        var elementyMaincounter = doc.getElementsByClass("maincounter-number")
        var pierwszyElement = elementyMaincounter.get(0)
        var drugiElement = elementyMaincounter.get(1)
        var trzeciElement = elementyMaincounter.get(2)

        casesDisp.text = pierwszyElement.text()
        deathsDisp.text = drugiElement.text()
        recoveredDisp.text = trzeciElement.text()
        redrawTable(doc, redrawName)
    }

    fun redrawTable(doc: Document, name: String){

        val table: Element = doc.select("table")[0] //select the first table.
        val rows: Elements = table.select("tr")
        for (i in 1 until rows.size) { //first row is the col names so skip it.

            val row: Element = rows[i]
            val cols: Elements = row.select("td")
            if (cols[0].text() == name) {

                totalCasesDisp.text = cols[1].text()
                newCasesDisp.text = cols[2].text()
                totalDeathsDisp.text = cols[3].text()
                newDeathsDisp.text = cols[4].text()
                totalRecoveredDisp.text = cols[5].text()
                activeCasesDisp.text = cols[6].text()
                seriousDisp.text = cols[7].text()
                totCasesPerMDisp.text = cols[8].text()
            }
        }
    }
/*
    fun showAlertbox() {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }
 */
}

private class getHTMLdataTask : AsyncTask<Void, Void, String>() {

    override fun doInBackground(vararg p0:Void): String {

        val conn = Jsoup.connect("https://www.worldometers.info/coronavirus/").method(Connection.Method.GET)
        val resp = conn.execute()
        val html = resp.body()

        return html
    }
}