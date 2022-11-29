package ca.doublenom.pixnails

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
            val img: ImageView = view.findViewById(R.id.promo_item_picture)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.promo_card_item, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val number = dataSet[position].number
            try {
                val ims: InputStream =
                    context.assets.open("pixnails/cards/promo/regular/cards_ressources_generations_promo_snails_normal_$number.png")
                val d = Drawable.createFromStream(ims, null)
                holder.img.setImageDrawable(d)
                ims.close()
            } catch (e: Exception) {
                val ims: InputStream = context.assets.open("pixnails/cards/unknown.png")
                val d = Drawable.createFromStream(ims, null)
                holder.img.setImageDrawable(d)
                ims.close()
            }
        }

        override fun getItemCount(): Int {
            return dataSet.size
        }
    }

    private val adapter = PromoAdapter(context, dataSet)

    private val queue = HTTPClient.getInstance(context)

    private val recyclerView = context.findViewById<RecyclerView>(R.id.promo_layout_list)

    init {
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refresh() {
        queue.addToRequestQueueObject(
            "/settings",
            {
                val cards = it.getJSONArray("currentPromoDrop")

                dataSet.clear()
                for (i in 0 until cards.length()) {
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