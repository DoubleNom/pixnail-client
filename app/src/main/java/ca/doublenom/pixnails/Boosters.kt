package ca.doublenom.pixnails

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import java.lang.Integer.MAX_VALUE
import java.lang.Integer.min

@SuppressLint("NotifyDataSetChanged")
class Boosters(
    context: AppCompatActivity, private val callback: Callback
) {
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
        var silverShells: Int = 0,
        private val callback: Callback
    ) : RecyclerView.Adapter<BoosterAdapter.ViewHolder>() {
        interface Callback {
            fun onClick(quantity: Int, set: String, rank: String)
        }

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

        @SuppressLint("SetTextI18n")
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

            holder.bX1.visibility = if (hasEnoughMoney(
                    shells,
                    silverShells,
                    item.priceShells,
                    item.priceSilverShells,
                    1
                )
            ) View.VISIBLE else View.GONE
            holder.bX10.visibility = if (hasEnoughMoney(
                    shells,
                    silverShells,
                    item.priceShells,
                    item.priceSilverShells,
                    10
                )
            ) View.VISIBLE else View.GONE
            holder.bMax.visibility = holder.bX1.visibility

            holder.bX1.setOnClickListener {
                callback.onClick(1, item.set, item.rank)
            }
            holder.bX10.setOnClickListener {
                callback.onClick(10, item.set, item.rank)
            }
            holder.bMax.setOnClickListener {
                callback.onClick(
                    getMax(
                        shells,
                        silverShells,
                        item.priceShells,
                        item.priceSilverShells
                    ), item.set, item.rank
                )
            }
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

        private fun getMax(
            shells: Int,
            silverShells: Int,
            priceShells: Int,
            priceSilverShells: Int
        ): Int {
            val qs = if (priceShells != 0) shells / priceShells else MAX_VALUE
            val qss = if (priceSilverShells != 0) silverShells / priceSilverShells else MAX_VALUE
            return min(qs, qss)
        }

        override fun getItemCount(): Int {
            return dataSet.size
        }
    }

    private val adapter = BoosterAdapter(context, dataSet, 0, 0, object : BoosterAdapter.Callback {
        override fun onClick(quantity: Int, set: String, rank: String) {
            queue.addToRequestQueueArray("/generations/$set/boosters/$rank?quantity=$quantity",
                {
                    Log.d("Booster", it.toString())
                    val cards: HashSet<Card> = HashSet()
                    for(boosterIndex in 0 until it.length()) {
                        val array: JSONArray = it.getJSONArray(boosterIndex)
                        for (cardIndex in 0 until array.length()) {
                            val obj = array.getJSONObject(cardIndex)
                            val q = obj.getJSONObject("quantities")
                            val puddyness =
                                when {
                                    q.getInt(Puddyness.Normal.toUselessCorpRetardness()) != 0 -> Puddyness.Normal
                                    q.getInt(Puddyness.Super.toUselessCorpRetardness()) != 0 -> Puddyness.Super
                                    q.getInt(Puddyness.Giga.toUselessCorpRetardness(false)) != 0 -> Puddyness.Giga
                                    else -> Puddyness.None
                                }
                            cards.add(Card(obj, puddyness))
                        }
                    }
                    callback.onDraw(cards.toTypedArray())
                },
                {
                    Log.e("Boosters", it.toString())
                }
            )
        }
    })

    val queue = HTTPClient.getInstance(context)

    private val recyclerView: RecyclerView = context.findViewById(R.id.boosters_layout_list)

    init {
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        dataSet.add(BoosterItem("origin", "normal", 100, 0))
        dataSet.add(BoosterItem("origin", "premium", 1000, 0))
        dataSet.add(BoosterItem("promo", "normal", 500, 0))
        adapter.notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onShellsUpdated(shells: Int, silverShells: Int) {
        adapter.shells = shells
        adapter.silverShells = silverShells

        adapter.notifyDataSetChanged()
    }

    interface Callback {
        fun onPurchase()
        fun onDraw(cards: Array<Card>)
    }

}