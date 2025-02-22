package io.github.clay121217.tableclocks

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import io.github.clay121217.tableclocks.databinding.FragmentThemeDrawingBinding

//augments
private const val THEME_NAME = "theme_name"
private const val MONTH = "month"


class ThemeDrawingFragment : Fragment() {

    private var themeName: String? = null
    private var month: Int? = null

    private var _binding: FragmentThemeDrawingBinding? = null
    private val binding get() = _binding!!

//    private var _handler = Looper.myLooper()?.let { Handler(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            themeName = it.getString(THEME_NAME)
            month = it.getInt(MONTH)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentThemeDrawingBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        生成時テーマ描画

        //アニメーターのセット
        val fadeAnimator = ObjectAnimator.ofFloat(binding.overBlack, View.ALPHA, 1f, 0f)
        fadeAnimator.duration = 400

        //描画
        themeImageSet(themeName ?: "jp_seasons", month ?: 1)

        //フェードイン
        fadeAnimator.start()


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param themeName Drawing theme name.
         * @param month Drawing month.
         * @return A new instance of fragment ThemeDrawingFragment.
         */
        @JvmStatic
        fun newInstance(themeName: String, month: Int) =
            ThemeDrawingFragment().apply {
                arguments = Bundle().apply {
                    putString(THEME_NAME, themeName)
                    putInt(MONTH, month)
                }
            }
    }

    //テーマ画像セット
    //生で呼び出すのは生成時だけの予定
    //他の場合はフェードをつけたthemeImageChange()で対応したい
    @SuppressLint("DiscouragedApi")
    private fun themeImageSet(newThemeName: String = "keep", newMonth: Int = -1) {

        //引数があるときはメンバ変数を置き換える
        if (newThemeName !== "keep") {
            themeName = newThemeName
        }
        if (newMonth != -1) {
            month = newMonth
        }

        //データ整形 jp_seasons_m_01のようにする
        val mainImgName = themeName + "_m_" + month.toString().padStart(2, '0')
        val mainImgBGColor = themeName + "_col_" + month.toString().padStart(2, '0')
        val coverImgName = themeName + "_cover"

        //メインイメージセット
        binding.drawThemeImage.setImageResource(
            resources.getIdentifier(
                mainImgName,
                "drawable",
                requireContext().packageName
            )
        )        //getIdentifierを使う方法、Stringが使えるので引き出しやすそう

        //制約をセットしてメインイメージの重力を切り替え
        val constraintSet = ConstraintSet()
        constraintSet.clone(binding.ThemeDrawingFragmentWrap)

        //テーマ情報から重力方向取得
        val mainImgPosition: String
        val mainImgPositionId = resources.getIdentifier(
            themeName + "_pos_" + month.toString().padStart(2, '0'),
            "string",
            requireContext().packageName
        )
        //見つからない場合はBOTに
        mainImgPosition = if( mainImgPositionId == 0){
            "BOT"
        }else{
            getString(mainImgPositionId)
        }

        //設定をもとに制約セット実行
        //上制約　MIDとTOPのとき有効
        if(mainImgPosition == "MID" || mainImgPosition == "TOP"){
            constraintSet.connect(binding.drawThemeImage.id,ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP)
        }else{
            constraintSet.clear(binding.drawThemeImage.id,ConstraintSet.TOP)
        }
        //下制約　MIDとBOTのとき有効
        if(mainImgPosition == "MID" || mainImgPosition == "BOT"){
            constraintSet.connect(binding.drawThemeImage.id,ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID,ConstraintSet.BOTTOM)
        }else{
            constraintSet.clear(binding.drawThemeImage.id,ConstraintSet.BOTTOM)
        }

        // 編集した制約をConstraintLayoutに反映させる
        constraintSet.applyTo(binding.ThemeDrawingFragmentWrap)


        //背景色セット
        binding.drawThemeBGColor.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                resources.getIdentifier(mainImgBGColor, "color", context?.packageName)
            )
        )

        //カバー画像セット　テーマ引数があるときだけ
        if (newThemeName !== "keep") {
            binding.drawThemeCover.setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    resources.getIdentifier(coverImgName, "drawable", context?.packageName),
                    null
                )
            )
        }
    }

    //テーマや月の変更、フェード付き
    fun themeImageChange(newThemeName: String = "keep", newMonth: Int = -1) {
        //アニメーションセット
        val fadeAnimator = ObjectAnimator.ofFloat(binding.overBlack, View.ALPHA, 1f, 0f)
        fadeAnimator.duration = 400

        //変更開始
        fadeAnimator.reverse()      //フェードアウト

        Handler(Looper.getMainLooper()).postDelayed({    //遅延実行
            themeImageSet(newThemeName, newMonth) //画像セット
            fadeAnimator.start()    //フェードイン
        }, 600)

    }

    override fun onDestroyView() {

        super.onDestroyView()
        _binding = null //ビューバインディングをちゃんと破棄する

    }


}