package com.example.eric.asyncr

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.stream.Collectors

fun Context.toast(message: CharSequence) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

class GithubActivity : AppCompatActivity() {
    lateinit var input : EditText
    lateinit var button : Button
    lateinit var progress : ProgressBar
    lateinit var nameField : TextView
    lateinit var repoField : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_github)

        // Save refs for all interesting stuff
        button = findViewById(R.id.btnLookup) as Button
        input = findViewById(R.id.inputName) as EditText
        progress = findViewById(R.id.progress) as ProgressBar
        nameField = findViewById(R.id.textUserName) as TextView
        repoField = findViewById(R.id.textRepo) as TextView

        // Hook up event listener
        button.setOnClickListener {
            val txt = input.text

            if (txt.isEmpty()) {
                toast("Please enter a name")
            } else {
                AsyncFetch(progress, {
                    r -> populate(JSONObject(r))
                }).execute(txt.toString())
            }
        }
    }

    @SuppressLint("SetTextI18n") // <-- Screw i18n. It's a stupid abbreviation anyway.
    fun populate(obj: JSONObject) {
        nameField.text = obj.getString("name")
        repoField.text = "${obj.get("login")} has ${obj.get("public_repos")} " +
                         "public repos and ${obj.get("followers")} followers"
    }
}

internal class AsyncFetch(val progressBar: ProgressBar, val action: (String) -> Unit)
    : AsyncTask<String, Unit, String>() {

    init {
        progressBar.visibility = View.VISIBLE
    }

    override fun doInBackground(vararg input: String?): String? {
        try {
            val conn = URL("https://api.github.com/users/${input[0]}").openStream()
            val str = BufferedReader(InputStreamReader(conn))
                    .lines()
                    .collect(Collectors.joining("\n"))
            return str
        } catch (ex : Exception) {
            Log.e("http", Log.getStackTraceString(ex))
            return null
        }
    }

    override fun onPostExecute(result: String?) {
        Log.i("http", result)
        action(result.orEmpty())
        progressBar.visibility = View.INVISIBLE
    }
}
