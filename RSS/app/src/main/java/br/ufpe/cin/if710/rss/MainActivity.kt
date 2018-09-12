package br.ufpe.cin.if710.rss

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import br.ufpe.cin.if710.rss.ParserRSS.parse
import java.util.ArrayList


class MainActivity : Activity() {
    //ao fazer envio da resolucao, use este link no seu codigo!
    private val RSS_FEED = "http://leopoldomt.com/if1001/g1brasil.xml"
    //OUTROS LINKS PARA TESTAR...
    //http://rss.cnn.com/rss/edition.rss
    //http://pox.globo.com/rss/g1/brasil/
    //http://pox.globo.com/rss/g1/ciencia-e-saude/
    //http://pox.globo.com/rss/g1/tecnologia/

    //use ListView ao invés de TextView - deixe o atributo com o mesmo nome
    private lateinit var conteudoRSS:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        conteudoRSS = findViewById(R.id.conteudoRSS)
    }

    override fun onStart(){
        super.onStart()
        try{
            loadRSS().execute(RSS_FEED)
        } catch(e: IOException) {
            e.printStackTrace()
        }
    }

    //Opcional - pesquise outros meios de obter arquivos da internet - bibliotecas, etc.
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

        override fun onPostExecute(result: List<ItemRSS>) {
            super.onPostExecute(result)
            conteudoRSS.setText(result.toString())
        }
    }
}
