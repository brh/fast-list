package com.list.rados.recyclerviewlibrary

import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.list.rados.fast_list.bind
import com.list.rados.fast_list.update
import com.list.rados.recyclerviewlibrary.databinding.ActivityMainBinding
import com.list.rados.recyclerviewlibrary.databinding.ItemBinding

class MainActivity : AppCompatActivity() {

    data class Item(val value: String, val type: Int)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = ActivityMainBinding.inflate(layoutInflater)

        val list = listOf(Item("first", 2), Item("second", 2), Item("third", 1), Item("fourth", 1), Item("fifth", 1))
        val list2 = listOf(Item("first", 2), Item("third", 1), Item("fifth", 1), Item("sixth", 3))

        println("Matching ${binding is ViewBinding}")
        var adapter = binding.recyclerView.bind<Item,ViewBinding>(list, { parent-> ItemBinding.inflate(layoutInflater, parent, false) }) { item: Item ->
            (this as ItemBinding).itemText.text = item.value
            (this as ItemBinding).container.setOnClickListener {
                toast(item.value)
            }
        }.layoutManager(LinearLayoutManager(this))
        binding.recyclerView.adapter = adapter

        /*binding.recyclerView.bind(list)
                .map(, predicate = {it:Item, _ -> it.type == 1 }) { item: Item ->
                    item_text.text = item.value
                    container.setOnClickListener {
                        toast(item.value)
                    }
                }
                .map(layout = R.layout.item_second, predicate = {it:Item, _ ->  it.type == 2 }) { item: Item ->
                    item_second_text.text = item.value
                    container_second.setOnClickListener {
                        toast(item.value)
                    }
                }
                .map(layoutFactory = LocalFactory(this), predicate = {it:Item, _ ->  it.type == 3 }) { item: Item ->
                    item_custom_text.text = item.value
                    container_custom.setOnClickListener {
                        toast(item.value)
                    }
                }
                .layoutManager(LinearLayoutManager(this))*/

        //delay(2000) {
            binding.recyclerView.update<Item,ItemBinding>(list2)
                    //}

    }

    private fun toast(value: String) {
        Toast.makeText(this, value, Toast.LENGTH_SHORT).show()
    }
}

/*class LocalFactory(val activity: AppCompatActivity) : LayoutFactory {
    override fun createView(parent: ViewGroup, type: Int): View {
        return LayoutInflater.from(activity).inflate(R.layout.item_custom,
                parent, false)
    }
}*/

fun delay(delay: Long, func: () -> Unit) {
    val handler = Handler()
    handler.postDelayed({
        try {
            func()
        } catch (e: Exception) {
            println(e.toString())
        }
    }, delay)
}