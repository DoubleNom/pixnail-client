package ca.doublenom.pixnails

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Promo(val context: AppCompatActivity) {
    private val dataSet = ArrayList<PromoItem>(5)

    data class PromoItem(var text: String, var img: Int)

    class PromoAdapter(
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
            holder.text.text = "${dataSet[position].text}/28"
//            holder.img.setImageResource(R.mipmap.promo_sample)
        }

        override fun getItemCount(): Int {
            return dataSet.size
        }


    }

    val adapter = PromoAdapter(dataSet)

    val queue = HTTPClient.getInstance(context)

    val recyclerView = context.findViewById<RecyclerView>(R.id.promo_layout_list)

    init {
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

    fun refresh() {
        queue.addToRequestQueue(
            "/settings",
            {
                val cards = it.getJSONArray("currentPromoDrop")

                dataSet.clear()
                for(i in 0 until cards.length()){
                    val obj = cards.getJSONObject(i)
                    dataSet.add(PromoItem("${obj.getInt("index")}", R.mipmap.promo_sample_foreground))
                }
                adapter.notifyDataSetChanged()

            },
            {
                Log.e("HTTP", it.toString())
            }
        )
    }
}