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

class Boosters(context: AppCompatActivity, private val callback: Callback) {
    private val dataSet = ArrayList<BoosterItem>(3)

    data class BoosterItem(
        var set: String,
        var rank: String,
        var priceShells: Int,
        var priceSilverShells: Int
    )

    class BoosterAdapter(
        private val context: Context,
        private val dataSet: ArrayList<BoosterItem>,
        var shells: Int = 0,
        var silverShells: Int = 0
    ) : RecyclerView.Adapter<BoosterAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val price: TextView = view.findViewById(R.id.booster_item_price)
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
            holder.title.text = "${item.set} - ${item.rank}"

            val imgPath =
                "pixnails/boosters/cards_ressources_generations_${item.set}_booster_${item.rank}.png"
            val ims = context.assets.open(imgPath)
            val d = Drawable.createFromStream(ims, null)
            holder.img.setImageDrawable(d)
            ims.close()

            var text = ""
            if (item.priceShells != 0) text += "S: ${item.priceShells}"
            if (item.priceShells != 0 && item.priceSilverShells != 0) text += " & "
            if (item.priceSilverShells != 0) text += "SS: ${item.priceShells}"
            holder.price.text = text

            holder.bX1.visibility = if(hasEnoughMoney(shells, silverShells, item.priceShells, item.priceSilverShells, 1)) View.VISIBLE else View.GONE
            holder.bX10.visibility = if(hasEnoughMoney(shells, silverShells, item.priceShells, item.priceSilverShells, 10)) View.VISIBLE else View.GONE
            holder.bMax.visibility = holder.bX1.visibility
        }


        private fun hasEnoughMoney(
            shells: Int,
            silverShells: Int,
            priceShells: Int,
            priceSilverShells: Int,
            quantity: Int
        ): Boolean {
            if (shells - (priceShells * quantity) < 0) return false
            if (silverShells - (priceSilverShells * quantity) < 0) return false
            return true
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
        dataSet.add(BoosterItem("origin", "normal", 100, 0))
        dataSet.add(BoosterItem("origin", "premium", 1000, 0))
        dataSet.add(BoosterItem("promo", "normal", 500, 0))
        adapter.notifyDataSetChanged()
    }

    fun onShellsUpdated(shells: Int, silverShells: Int) {
        adapter.shells = shells
        adapter.silverShells = silverShells

        adapter.notifyDataSetChanged()
    }

    interface Callback {
        fun onPurchase()
    }

}