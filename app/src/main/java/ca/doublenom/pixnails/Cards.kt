package ca.doublenom.pixnails

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import java.io.InputStream

class Cards(context: AppCompatActivity) {
    private val completionAdapter = CompletionAdapter(context, ArrayList())
    private val rvCompletion = context.findViewById<RecyclerView>(R.id.completion_layout_list)
    private val cards = HashMap<Card, Int>()
    private val completion = HashMap<String, Array<Array<Boolean>>>()

    data class CompletionItem(val set: String, val percentages: Array<Float>)

    class CompletionAdapter(
        private val context: Context,
        var dataSet: ArrayList<CompletionItem>
    ) : RecyclerView.Adapter<CompletionAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val img: ImageView = view.findViewById(R.id.set_completion_set_icon)
            val title: TextView = view.findViewById(R.id.set_completion_set_title)
            val nv: TextView = view.findViewById(R.id.set_completion_set_normal_value)
            val sv: TextView = view.findViewById(R.id.set_completion_set_super_value)
            val gv: TextView = view.findViewById(R.id.set_completion_set_giga_value)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.set_completion_item, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val set = dataSet[position].set
            val ims: InputStream =
                context.assets.open("pixnails/icons/generations/cards_ressources_generations_${set}_icon.png")
            val d = Drawable.createFromStream(ims, null)
            holder.img.setImageDrawable(d)
            ims.close()

            holder.title.text = set
            holder.nv.text =
                "${(dataSet[position].percentages[Puddyness.Normal.ordinal] * 100).toInt()}"
            holder.sv.text =
                "${(dataSet[position].percentages[Puddyness.Super.ordinal] * 100).toInt()}"
            holder.gv.text =
                "${(dataSet[position].percentages[Puddyness.Giga.ordinal] * 100).toInt()}"
        }

        override fun getItemCount(): Int {
            return dataSet.size
        }
    }

    init {
        rvCompletion.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        val snapHelper = LinearSnapHelper()
//        snapHelper.attachToRecyclerView(rvCompletion)
        rvCompletion.adapter = completionAdapter
    }

    fun update(json: JSONArray) {
        // Initialize set if needed
        for (set in Generations.getGenerationsName()) {
            val setSize = Generations.getGenerationSize(set)
            completion[set] = Array(setSize) {
                Array(3) {
                    false
                }
            }
        }

        for (i in 0 until json.length()) {
            val e = json.getJSONObject(i)
            val q = e.getJSONObject("quantities")
            val card = Card(e, Puddyness.Normal)
            val qn = q.getInt(Puddyness.Normal.toUselessCorpRetardness())
            val qs = q.getInt(Puddyness.Super.toUselessCorpRetardness())
            val qg = q.getInt(Puddyness.Giga.toUselessCorpRetardness(false))
            cards[Card(e, Puddyness.Normal)] = qn
            cards[Card(e, Puddyness.Super)] = qs
            cards[Card(e, Puddyness.Giga)] = qg

            completion[card.set]!![card.number][Puddyness.Normal.ordinal] = qn != 0
            completion[card.set]!![card.number][Puddyness.Super.ordinal] = qs != 0
            completion[card.set]!![card.number][Puddyness.Giga.ordinal] = qg != 0
        }

        val c = getCompletionRates()
        val list = ArrayList<CompletionItem>(c.size)
        for (entry in c.entries) {
            list.add(CompletionItem(entry.key, entry.value))
        }
        completionAdapter.dataSet = list
        completionAdapter.notifyDataSetChanged()
    }

    fun findNewCards(draw: Array<Card>): Array<Card> {
        val newCards = ArrayList<Card>()
        for (card in draw) {
            if (cards[card] == null || cards[card] == 0) newCards.add(card)
        }
        return newCards.toTypedArray()
    }

    fun getCompletionRates(): HashMap<String, Array<Float>> {
        val rates = HashMap<String, Array<Float>>()
        for (set in Generations.getGenerationsName()) {
            val size = Generations.getGenerationSize(set)
            rates[set] = Array(size) {
                0.0f
            }
            for (i in 0 until size) {
                rates[set]!![Puddyness.Normal.ordinal] += if (completion[set]!![i][Puddyness.Normal.ordinal]) 1.0f else 0.0f
                rates[set]!![Puddyness.Super.ordinal] += if (completion[set]!![i][Puddyness.Super.ordinal]) 1.0f else 0.0f
                rates[set]!![Puddyness.Giga.ordinal] += if (completion[set]!![i][Puddyness.Giga.ordinal]) 1.0f else 0.0f
            }
            rates[set]!![Puddyness.Normal.ordinal] = rates[set]!![Puddyness.Normal.ordinal] / size
            rates[set]!![Puddyness.Super.ordinal] = rates[set]!![Puddyness.Super.ordinal] / size
            rates[set]!![Puddyness.Giga.ordinal] = rates[set]!![Puddyness.Giga.ordinal] / size
        }

        return rates
    }


}