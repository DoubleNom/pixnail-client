package ca.doublenom.pixnails

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import java.io.InputStream

class DraftDialog(
    private val newCards: Array<Card>,
    private val draw: Array<Card>
) : DialogFragment() {

    init {
        newCards.sort()
        draw.sort()
    }

    class DraftAdapter(
        private val context: Context,
        private val dataSet: Array<Card>
    ) : RecyclerView.Adapter<DraftAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text: TextView = view.findViewById(R.id.card_item_number)
            val img: ImageView = view.findViewById(R.id.card_item_image)
            val set: ImageView = view.findViewById(R.id.card_item_set)
            val rarity: ImageView = view.findViewById(R.id.card_item_rank)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.card_item, parent, false)
            return ViewHolder(view)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = dataSet[position]
            val imgPath = if (item.puddyness != Puddyness.Giga)
                "pixnails/cards/${item.set}/regular/cards_ressources_generations_${item.set}_snails_normal_${item.number}.png"
            else
                "pixnails/cards/${item.set}/shiny/cards_ressources_generations_${item.set}_snails_super_shiny_${item.number}.png"
            var ims: InputStream = context.assets.open(imgPath)
            var d = Drawable.createFromStream(ims, null)
            holder.img.setImageDrawable(d)
            ims.close()

            ims =
                context.assets.open("pixnails/icons/generations/cards_ressources_generations_${item.set}_icon.png")
            d = Drawable.createFromStream(ims, null)
            holder.set.setImageDrawable(d)
            ims.close()

            ims = context.assets.open(
                "pixnails/icons/rarity/cards_ressources_template_rarity_${
                    item.rarity.toString().lowercase()
                }_${item.puddyness.toUselessCorpRetardness()}.png"
            )
            d = Drawable.createFromStream(ims, null)
            holder.rarity.setImageDrawable(d)
            ims.close()

            holder.text.text = "${item.number}"
        }

        override fun getItemCount(): Int {
            return dataSet.size
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val view = inflater.inflate(R.layout.draft_modal, null)

            val noNewCards = view.findViewById<TextView>(R.id.draft_model_nothing_new)
            val aNewCards = DraftAdapter(it, newCards)
            val rwNewCards = view.findViewById<RecyclerView>(R.id.draft_modal_new_cards_list)
            rwNewCards.adapter = aNewCards

            if (this.newCards.isEmpty()) {
                rwNewCards.visibility = View.GONE
                noNewCards.visibility = View.VISIBLE
            } else {
                rwNewCards.visibility = View.VISIBLE
                noNewCards.visibility = View.GONE
            }

            val aDraw = DraftAdapter(it, draw)
            val rwDraw = view.findViewById<RecyclerView>(R.id.draft_modal_list)
            rwDraw.adapter = aDraw

            builder.setView(view).create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}