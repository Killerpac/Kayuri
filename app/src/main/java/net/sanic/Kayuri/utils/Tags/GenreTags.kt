package net.sanic.Kayuri.utils.Tags

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import net.sanic.Kayuri.R
import net.sanic.Kayuri.databinding.TagsGenreBinding

class GenreTags(var context: Context){
    fun getGenreTag(genreName: String, genreUrl: String, onClickListener: View.OnClickListener): View{
        val view =LayoutInflater.from(context).inflate(R.layout.tags_genre, null)
        val genreBinding:TagsGenreBinding = TagsGenreBinding.bind(view)
        val button = genreBinding.genre
        button.text = genreName
        button.maxLines  = 1
        val rel_button1 = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        rel_button1.setMargins(8, 8, 8, 8)
        button.layoutParams = rel_button1
        button.setOnClickListener(onClickListener)
        return view
    }

}