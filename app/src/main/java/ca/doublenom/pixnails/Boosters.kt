package ca.doublenom.pixnails

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Boosters(val context: AppCompatActivity) {
    private val dataSet = ArrayList<BoosterItem>(3)

    data class BoosterItem(var set: String, var rank: String)

    class BoosterAdapter(
        private val context: Context,
        private val dataSet: ArrayList<BoosterItem>
    ) : RecyclerView.Adapter<BoosterAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.booster_item_title)
            val img: ImageView = view.findViewById(R.id.booster_item_image)
            val bX1: Button = view.findViewById(R.id.booster_item_x1)
            val bX10: Button = view.findViewById(R.id.booster_item_x10)
            val bMax: Button = view.findViewById(R.id.booster_item_max)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.booster_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = dataSet[position]
            val imgPath = "pixnails/boosters/cards_ressources_generations_${item.set}_booster_${item.rank}.png"
            val ims = context.assets.open(imgPath)
            val d = Drawable.createFromStream(ims, null)
            holder.title.text = "${item.set} - ${item.rank}"
            holder.img.setImageDrawable(d)
            ims.close()
        }

        override fun getItemCount(): Int {
            return dataSet.size
        }
    }

    val adapter = BoosterAdapter(context, dataSet)

    val queue = HTTPClient.getInstance(context)

    val recyclerView = context.findViewById<RecyclerView>(R.id.boosters_layout_list)

    init {
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        dataSet.add(BoosterItem("origin", "normal"))
        dataSet.add(BoosterItem("origin", "premium"))
        dataSet.add(BoosterItem("promo", "normal"))
        adapter.notifyDataSetChanged()
    }

}