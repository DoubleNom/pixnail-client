package ca.doublenom.pixnails

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.InputStream

class Promo(context: AppCompatActivity) {
    private val dataSet = ArrayList<PromoItem>(5)

    data class PromoItem(var number: Int)

    class PromoAdapter(
        private val context: Context,
        private val dataSet: ArrayList<PromoItem>
    ) : RecyclerView.Adapter<PromoAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text: TextView = view.findViewById(R.id.promo_item_number)
            val img: ImageView = view.findViewById(R.id.promo_item_picture)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.promo_card_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            var number = dataSet[position].number
            val ims : InputStream = context.assets.open("pixnails/cards/promo/regular/cards_ressources_generations_promo_snails_normal_$number.png")
            val d = Drawable.createFromStream(ims, null)
            holder.text.text = "$number/28"
            holder.img.setImageDrawable(d)
            ims.close()
        }

        override fun getItemCount(): Int {
            return dataSet.size
        }
    }

    val adapter = PromoAdapter(context, dataSet)

    val queue = HTTPClient.getInstance(context)

    val recyclerView = context.findViewById<RecyclerView>(R.id.promo_layout_list)

    init {
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

    fun refresh() {
        queue.addToRequestQueueObject(
            "/settings",
            {
                val cards = it.getJSONArray("currentPromoDrop")

                dataSet.clear()
                for(i in 0 until cards.length()){
                    val obj = cards.getJSONObject(i)
                    dataSet.add(PromoItem(obj.getInt("index")))
                }
                adapter.notifyDataSetChanged()

            },
            {
                Log.e("HTTP", it.toString())
            }
        )
    }
}