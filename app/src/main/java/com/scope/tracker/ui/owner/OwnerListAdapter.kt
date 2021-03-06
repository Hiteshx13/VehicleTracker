package com.scope.tracker.ui.owner

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.scope.tracker.R
import com.scope.tracker.util.AppUtils
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.row_user_list.view.*


class OwnerListAdapter :
    RecyclerView.Adapter<OwnerListAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<OwnerResponse.Data>() {

        override fun areItemsTheSame(
            oldItem: OwnerResponse.Data,
            newItem: OwnerResponse.Data
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: OwnerResponse.Data,
            newItem: OwnerResponse.Data
        ): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.row_user_list,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val ownerData: OwnerResponse.Data? = differ.currentList[position]
        holder.itemView.apply {
            ownerData?.owner.let { owner ->
                tvOwnerName.text = owner?.getFullName()
                setOnClickListener {
                    onItemClickListener?.let { it(ownerData!!) }
                }

                /** removing extra string from image url and loading image**/
                val formattedImageUrl = AppUtils.getFormattedImageUrl(owner?.foto.toString())
                Log.d("USER_IMAGE", formattedImageUrl)
                if (formattedImageUrl.isNotEmpty()) {
                    Picasso.get().load(formattedImageUrl)
                        .placeholder(R.drawable.ic_user)
                        .resize(300, 300)
                        .transform(CropCircleTransformation())
                        .into(
                            ivProfile
                        )
                    ivProfile
                }
            }
        }
    }

    private var onItemClickListener: ((OwnerResponse.Data) -> Unit)? = null

    fun setOnItemClickListener(listener: (OwnerResponse.Data) -> Unit) {
        onItemClickListener = listener
    }

}