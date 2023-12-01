package com.example.rootcheck

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.rootcheck.databinding.FragmentAbstractBinding


/**
 * A simple [Fragment] subclass.
 * Use the [AbstractFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AbstractFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var mParam1: String? = null
    private var mParam2: String? = null
    private lateinit var viewBinding: FragmentAbstractBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = requireArguments().getString(ARG_PARAM1)
            mParam2 = requireArguments().getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        viewBinding = FragmentAbstractBinding.inflate(inflater, container, false)
        val webView = viewBinding.webView
        webView.settings.javaScriptEnabled = true

        webView.registerHandler("submitFromWeb") { data, function ->
            Log.i("TAG", "handler = submitFromWeb, data from web = $data")
            function.onCallBack("submitFromWeb exe, response data from Java")
        }

        viewBinding.buttom.setOnClickListener {
//            webView.loadUrl("http://yytest.ths8.com:8081/idiyun-web-h5/#/promotion?app=kc")
            webView.loadUrl("file:///android_asset/test.html")
        }
        return viewBinding.root
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
         * @return A new instance of fragment AbstractFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): AbstractFragment {
            val fragment = AbstractFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.setArguments(args)
            return fragment
        }
    }
}