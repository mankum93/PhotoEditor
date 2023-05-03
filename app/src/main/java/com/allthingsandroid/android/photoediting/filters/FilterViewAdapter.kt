package com.allthingsandroid.android.photoediting.filters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.allthingsandroid.android.photoediting.R
import com.allthingsandroid.android.photoeditor.filter.PhotoFilter
import java.io.IOException
import java.util.ArrayList

class FilterViewAdapter(private val mFilterListener: FilterListener) :
    RecyclerView.Adapter<FilterViewAdapter.ViewHolder>() {
    private val mPairList: MutableList<Pair<String, com.allthingsandroid.android.photoeditor.filter.PhotoFilter>> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.row_filter_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val filterPair = mPairList[position]
        val fromAsset = getBitmapFromAsset(holder.itemView.context, filterPair.first)
        holder.mImageFilterView.setImageBitmap(fromAsset)
        holder.mTxtFilterName.text = filterPair.second.name.replace("_", " ")
    }

    override fun getItemCount(): Int {
        return mPairList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mImageFilterView: ImageView = itemView.findViewById(R.id.imgFilterView)
        val mTxtFilterName: TextView = itemView.findViewById(R.id.txtFilterName)

        init {
            itemView.setOnClickListener{
                mFilterListener.onFilterSelected(
                    mPairList[layoutPosition].second
                )
            }
        }
    }

    private fun getBitmapFromAsset(context: Context, strName: String): Bitmap? {
        val assetManager = context.assets
        return try {
            val istr = assetManager.open(strName)
            BitmapFactory.decodeStream(istr)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun setupFilters() {
        mPairList.add(Pair("filters/original.jpg", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.NONE))
        mPairList.add(Pair("filters/auto_fix.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.AUTO_FIX))
        mPairList.add(Pair("filters/brightness.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.BRIGHTNESS))
        mPairList.add(Pair("filters/contrast.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.CONTRAST))
        mPairList.add(Pair("filters/documentary.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.DOCUMENTARY))
        mPairList.add(Pair("filters/dual_tone.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.DUE_TONE))
        mPairList.add(Pair("filters/fill_light.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.FILL_LIGHT))
        mPairList.add(Pair("filters/fish_eye.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.FISH_EYE))
        mPairList.add(Pair("filters/grain.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.GRAIN))
        mPairList.add(Pair("filters/gray_scale.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.GRAY_SCALE))
        mPairList.add(Pair("filters/lomish.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.LOMISH))
        mPairList.add(Pair("filters/negative.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.NEGATIVE))
        mPairList.add(Pair("filters/posterize.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.POSTERIZE))
        mPairList.add(Pair("filters/saturate.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.SATURATE))
        mPairList.add(Pair("filters/sepia.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.SEPIA))
        mPairList.add(Pair("filters/sharpen.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.SHARPEN))
        mPairList.add(Pair("filters/temprature.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.TEMPERATURE))
        mPairList.add(Pair("filters/tint.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.TINT))
        mPairList.add(Pair("filters/vignette.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.VIGNETTE))
        mPairList.add(Pair("filters/cross_process.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.CROSS_PROCESS))
        mPairList.add(Pair("filters/b_n_w.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.BLACK_WHITE))
        mPairList.add(Pair("filters/flip_horizental.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.FLIP_HORIZONTAL))
        mPairList.add(Pair("filters/flip_vertical.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.FLIP_VERTICAL))
        mPairList.add(Pair("filters/rotate.png", com.allthingsandroid.android.photoeditor.filter.PhotoFilter.ROTATE))
    }

    init {
        setupFilters()
    }
}