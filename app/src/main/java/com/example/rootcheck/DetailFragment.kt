package com.example.rootcheck

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.rootcheck.databinding.FragmentDetailBinding

/**
 * A simple [Fragment] subclass.
 * Use the [DetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private lateinit var binding: FragmentDetailBinding
    private var isExpand = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        val rootView=binding.root
        val listOf = listOf(
            "测试",
            "搜  萨达索",
            "仍然稳定",
            "山东",
            "十五",
            "烦烦烦",
            "鹅鹅鹅 ",
            "撒旦撒",
            "二二方法",
            "我为人人",
            "撒啊啊我日日",
            "飒飒的是",
            "撒","测试",
            "搜  萨达索",
            "仍然稳定",
            "山东",
            "十五",
            "烦烦烦",
            "鹅鹅鹅 ",
            "撒旦撒",
            "二二方法",
            "我为人人",
            "撒啊啊我日日",
            "飒飒的是"
        )

        binding.group.removeAllViews()
        for (i in listOf){
            val itemLayout = inflater.inflate(R.layout.item_view,container,false)
            val label = itemLayout.findViewById<TextView>(R.id.textView)
            label.text=i
            binding.group.addView(itemLayout)
            val bHasMultiline = binding.group.getNumber()
//            Log.d("TAG",bHasMultiline.toString())
        }

        binding.button.setOnClickListener{
            binding.group.requestShowMeasure(isExpand)
            isExpand = !isExpand


        }

        return rootView
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment DetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): DetailFragment {
            val fragment = DetailFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }
}