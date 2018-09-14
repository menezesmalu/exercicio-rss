package br.ufpe.cin.if710.rss

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import br.ufpe.cin.if710.rss.ParserRSS.parse

class MainActivity : Activity() {
    private var RSS_FEED = ""

    //conteudoRSS é um recycler view
    private lateinit var conteudoRSS: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        conteudoRSS = findViewById(R.id.conteudoRSS)
        viewManager = LinearLayoutManager(this)
        //iniciando o RSS url com a url no res
        RSS_FEED = getString(R.string.rssfeed)

    }

    override fun onStart(){
        super.onStart()
        try{
            loadRSS().execute(RSS_FEED)
        } catch(e: IOException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("StaticFieldLeak")
    internal inner class loadRSS: AsyncTask<String, Void, List<ItemRSS>>(){
        override fun doInBackground(vararg feed: String): List<ItemRSS> {
            var in_: InputStream? = null
            var rssFeed = ""
            try {
                val url = URL(feed[0])
                val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
                in_ = conn.getInputStream()
                val out = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var count: Int = in_.read(buffer)
                while (count != -1) {
                    out.write(buffer, 0, count)
                    count = in_.read(buffer)
                }
                val response = out.toByteArray()
                rssFeed = String(response, charset("UTF-8"))

            } catch (e: IOException) {
                e.printStackTrace()
            }finally {
                if(in_ != null) {
                    in_.close()
                }
            }
            return parse(rssFeed)

        }
        //atualizanodo a activity após parsear as informações
        override fun onPostExecute(result: List<ItemRSS>) {
            super.onPostExecute(result)
            viewAdapter = RssAdapter(result)
            conteudoRSS = findViewById<RecyclerView>(R.id.conteudoRSS).apply {
                setHasFixedSize(true)
                layoutManager = viewManager
                adapter = viewAdapter
            }
        }
    }
    //adapter para lidar com o RSS Feed
    private inner class RssAdapter(private val result: List<ItemRSS>) :
            RecyclerView.Adapter<CardChangeHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardChangeHolder {
            val v = layoutInflater.inflate(R.layout.itemlista, parent, false)
            return CardChangeHolder(v)
        }

        override fun onBindViewHolder(holder: CardChangeHolder, position: Int) {
            holder.bindModel(result.get(position))
            //quando clicar no TITULO ir pro site
            holder.title.setOnClickListener {
                val url = result[position].link
                val uri = Uri.parse(url)
                val intents = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intents)
            }
        }
        override fun getItemCount(): Int {
            return result.size
        }

    }

    //CardHolder com as informações que vão aparecer na tela de cada página
    internal class CardChangeHolder (row: View) : RecyclerView.ViewHolder(row){
        var title: TextView
        var pubDate: TextView

        init {
            title = row.findViewById(R.id.item_titulo)
            pubDate = row.findViewById(R.id.item_data)
        }

        fun bindModel(rss: ItemRSS) {
            title.text = rss.title
            pubDate.text = rss.pubDate
        }

    }

}
