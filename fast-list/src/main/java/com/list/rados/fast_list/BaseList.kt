package com.list.rados.fast_list

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2

/**
 * Created by Radoslav Yankov on 29.06.2018
 * radoslavyankov@gmail.com
 */

/**
 * Dynamic list bind function. It should be followed by one or multiple .map calls.
 * @param items - Generic list of the items to be displayed in the list
 */
fun <T, B : ViewBinding> RecyclerView.bind(items: List<T>): FastListAdapter<T, B> {
    layoutManager = LinearLayoutManager(context)
    return FastListAdapter(items.toMutableList(), this)
}

/**
 * ViewPager2 update
 * Dynamic list bind function. It should be followed by one or multiple .map calls.
 * @param items - Generic list of the items to be displayed in the list
 */
fun <T, B : ViewBinding> ViewPager2.bind(items: List<T>): FastListAdapter<T, B> {
    return FastListAdapter(items.toMutableList(), vpList = this)
}

/**
 * Simple list bind function.
 * @param items - Generic list of the items to be displayed in the list
 * @param singleLayout - The layout that will be used in the list
 * @param singleBind - The "binding" function between the item and the layout. This is the standard "bind" function in traditional ViewHolder classes. It uses Kotlin Extensions
 * so you can just use the XML names of the views inside your layout to address them.
 */
fun <T, B : ViewBinding> RecyclerView.bind(items: List<T>, singleLayout: (parent : ViewGroup) -> B, singleBind: (B.(item: T) -> Unit)): FastListAdapter<T,B> {
    layoutManager = LinearLayoutManager(context)
    return FastListAdapter<T, B>(items.toMutableList(), this
    ).map(singleLayout, {item: T, idx: Int ->  true }, singleBind)
}

/**
 * ViewPager2 update
 * Simple list bind function.
 * @param items - Generic list of the items to be displayed in the list
 * @param singleLayout - The layout that will be used in the list
 * @param singleBind - The "binding" function between the item and the layout. This is the standard "bind" function in traditional ViewHolder classes. It uses Kotlin Extensions
 * so you can just use the XML names of the views inside your layout to address them.
 */
fun <T, B : ViewBinding> ViewPager2.bind(items: List<T>, singleLayout: (parent : ViewGroup) -> B, singleBind: (B.(item: T) -> Unit)): FastListAdapter<T,B> {
    return FastListAdapter<T, B>(items.toMutableList(), vpList = this
    ).map(singleLayout, {item: T, idx: Int ->  true }, singleBind)
}


/**
 * Updates the list using DiffUtils.
 * @param newItems the new list which is to replace the old one.
 *
 * NOTICE: The comparator currently checks if items are literally the same. You can change that if you want,
 * by changing the lambda in the function
 */
fun <T, B : ViewBinding> RecyclerView.update(newItems: List<T>) {
    (adapter as? FastListAdapter<T, B>)?.update(newItems) { o, n, _ -> o == n }
}

/**
 * ViewPager2 update
 * Updates the list using DiffUtils.
 * @param newItems the new list which is to replace the old one.
 *
 * NOTICE: The comparator currently checks if items are literally the same. You can change that if you want,
 * by changing the lambda in the function
 */
fun <T, B : ViewBinding> ViewPager2.update(newItems: List<T>) {
    (adapter as? FastListAdapter<T, B>)?.update(newItems) { o, n, _ -> o == n }
}

open class FastListAdapter<T, B : ViewBinding>(private var items: MutableList<T>, private var list: RecyclerView?=null, private var vpList : ViewPager2?=null)
    : RecyclerView.Adapter<FastListViewHolder<T, B>>() {

    init {
        if (vpList != null && list != null)
            throw IllegalArgumentException("You can only use either the Recycler(list) or the Pager(vpList)")
        if (vpList == null && list == null)
            throw IllegalArgumentException("You have to use either the Recycler(list) or the Pager(vpList)")

    }

    private inner class BindMap(val layout: (parent : ViewGroup) -> B, var type: Int = 0, val bind: B.(item: T) -> Unit, val predicate: (item: T, idx : Int) -> Boolean)

    private var bindMap = mutableListOf<BindMap>()
    private var typeCounter = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FastListViewHolder<T, B> {
        return bindMap.first { it.type == viewType }.let {
            return FastListViewHolder(it.layout(parent), viewType)
        }
    }

    override fun onBindViewHolder(holder: FastListViewHolder<T, B>, position: Int) {
        val item = items.get(position)
        val first = bindMap.first { it.type == holder.holderType }
        holder.bind(item, first.bind)
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) = try {
        bindMap.first { it.predicate(items[position], position) }.type
    } catch (e: Exception) {
        0
    }

    /**
     * The function used for mapping types to layouts
     * @param layout - The ID of the XML layout of the given type
     * @param predicate - Function used to sort the items. For example, a Type field inside your items class with different values for different types.
     * @param bind - The "binding" function between the item and the layout. This is the standard "bind" function in traditional ViewHolder classes. It uses Kotlin Extensions
     * so you can just use the XML names of the views inside your layout to address them.
     */
    fun map(builder : (parent : ViewGroup) -> B, predicate: (item: T, idx : Int) -> Boolean, bind: B.(item: T) -> Unit): FastListAdapter<T, B> {
        bindMap.add(BindMap(builder, typeCounter++, bind, predicate))
        list?.adapter = this
        vpList?.adapter = this
        return this
    }

    /**
     * Sets up a layout manager for the recycler view.
     */
    fun layoutManager(manager: RecyclerView.LayoutManager): FastListAdapter<T, B> {
        vpList?.let{ throw UnsupportedOperationException("layoumanager not needed for ViewPager2")}
        list!!.layoutManager = manager
        return this
    }

    fun update(newList: List<T>, compare: (T, T, Boolean) -> Boolean) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return compare(items[oldItemPosition], newList[newItemPosition], false)
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return compare(items[oldItemPosition], newList[newItemPosition], true)
            }

            override fun getOldListSize() = items.size

            override fun getNewListSize() = newList.size
        })
        if (newList is MutableList)
            items = newList
        else
            items = newList.toMutableList()
        diff.dispatchUpdatesTo(this)
    }
}

class FastListViewHolder<T, B : ViewBinding>(val binding: B, val holderType: Int) : RecyclerView.ViewHolder(binding.root) {
    fun bind(entry: T, func: B.(item: T) -> Unit) {
        binding.func(entry)
    }
}