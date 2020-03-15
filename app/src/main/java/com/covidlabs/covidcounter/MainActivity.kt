package com.covidlabs.covidcounter

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


// TODO:    Check for connection before to stop from crashing
//          Also make some thwrows, trys and catches
//          On a side note maybe rethink some functions

// initiate timer
private lateinit var timer: CountDownTimer

class MainActivity : AppCompatActivity() {

    // making some class-wide strings
    var stringedHTML: String = ""
    var redrawName: String = "Total:"

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // run for the first time to get data and start timer
        getItAllDoneMan()

        // and we really rely on that first run to get data...
        val doc: Document = Jsoup.parse(stringedHTML)
        redrawTable(doc, redrawName)

        // set listener on view button
        viewButton.setOnClickListener {

            // separate function just because i used it twice ;)
            checkForData(doc)

            // clear focus to try to get the keyboard to hide
            selectCountryField.clearFocus()
        }
        // set listener for return key we pass sent key down
        selectCountryField.setOnKeyListener{ _, keyCode, event ->

            // to check for specific one and on release run same code as before
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {

                // ditto
                checkForData(doc)
                selectCountryField.clearFocus()
            }
            false // no fucking idea about this line. have to check! TODO!
        }
    }

    // fun little fuction that makes my code look smarter :]
    fun checkForData(doc: Document){

        // it just checks input field for any input
        if(selectCountryField.text.toString() != ""){

            // if present pass it to redrawTable to display the data
            redrawName = selectCountryField.text.toString()
            redrawTable(doc, redrawName)
        }
        // maybe implement better checks? make it case insensitive? TODO!
        else{

            // if no name was input display toast message with appropriate information
            val message = Toast.makeText(applicationContext, R.string.errorNoCountry, Toast.LENGTH_LONG)
            message.show()
        }
    }

    // another fun function that just makes life more bearable
    fun getItAllDoneMan(){

        // initiate timer to run for 100s with tick every second
        timer = object : CountDownTimer(100000, 1000) {

            // when it finishes counting down it repeats
            override fun onFinish() = getItAllDoneMan()

            // run every tick. in this case it's a 1s (check above)
            override fun onTick(millisUntilFinished: Long) {

                // update the progress bar to reflect time left to refresh
                var secondsToRefresh = millisUntilFinished / 1000
                progressBar.progress = secondsToRefresh.toInt() //mainly because it looked very static
            }
        }.start() // and start it

        // run getHTMLdataTask() and save HTML or throw exception
        var html = getHTMLdataTask().execute() ?: throw Exception("No HTML data collected")
        stringedHTML = html.get()

        // parse the data into nice HTML elements
        var doc: Document = Jsoup.parse(stringedHTML)

        // find specific css class in all that HTML and save each element separately
        var maincounterElements = doc.getElementsByClass("maincounter-number")
        var mainCasesElement = maincounterElements.get(0)
        var mainDeathsElement = maincounterElements.get(1)
        var mainRecoveredElement = maincounterElements.get(2)

        // get text from that class ie: <span><b>123</b></span> -> 123
        casesDisp.text = mainCasesElement.text() // and display it
        deathsDisp.text = mainDeathsElement.text()
        recoveredDisp.text = mainRecoveredElement.text()
        redrawTable(doc, redrawName) // also redraw table with possibly fresh data
    }

    // fascinating how easy it is to manipulate tables
    fun redrawTable(doc: Document, name: String){

        // find first table in all that mess and from it get all rows
        val table: Element = doc.select("table")[0] //select the first table.
        val rows: Elements = table.select("tr")

        // do this for all but first row, which has col names
        for (i in 1 until rows.size){

            // get current row and select all cols
            val row: Element = rows[i]
            val cols: Elements = row.select("td")

            // check if first col has name we're interested in
            if (cols[0].text() == name) {

                // if so just put all the data where it belongs
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
    // for future reference how to pop up messages ;)
    fun showAlertbox() {

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.popup)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
    }
 */
}


// separate class for separate task. it just outputs a String on finish
private class getHTMLdataTask : AsyncTask<Void, Void, String>() {

    // let's do it in the background so the rest of code can run freely
    override fun doInBackground(vararg p0:Void): String { // also return a String

        // try to connect to the address provided using GET (to get most plain HTML)
        val conn = Jsoup.connect("https://www.worldometers.info/coronavirus/").method(Connection.Method.GET)
        val resp = conn.execute()

        // save the code
        val html = resp.body()

        // return to sender
        return html
    }
}