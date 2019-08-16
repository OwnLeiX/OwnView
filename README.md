# OwnView
自定义控件合集


## SingleTypeFlowLayout:(因虚构随机数据，在每次setData时都调用了notifyDataSetChanged()导致Demo略卡顿)
![image](https://github.com/OwnLeiX/OwnView/blob/master/exampleImages/SingleTypeFlowLayout.png)
![image](https://github.com/OwnLeiX/OwnView/blob/master/exampleImages/SingleTypeFlowLayout2.png)
* 只支持单一item类型的流式布局。如果需要(比如存在于ListView中或是频繁刷新)，可以自己设置复用池。

          flowLayout.setSingleTypeViewRecyclePool(pool);
          flowLayout.setAdapter(adapter);


**补充：1.如果item是LinearLayout，其内部的子View不能使用权重。而必须使用固定的宽高或者是WRAP_CONTENT，否则在复用时可能导致尺寸测量异常。**


* 在xml文件中可以通过 

          app:flowLayout_maxLines="3"设置最大行数
          app:flowLayout_horizontalPadding="10dp"设置行间距
          app:flowLayout_verticalPadding="5dp"设置列间距
          app:flowLayout_fillMode="false"设置是否填充之前因距离不够跳过的行
          app:flowLayout_dividerStartAndEnd="false"设置第0行之前和最末行之后是否需要设置行间距
          app:flowLayout_gravity="left"设置对齐方式


## DragSupportSingleTypeFlowLayout
![image](https://github.com/OwnLeiX/OwnView/blob/master/exampleImages/DragSupportFlowLayout.png)
* 可拖拽改变排序的单一item类型的流式布局。是SingleTypeFlowLayout的子类。

          //开始拖拽，设置拖拽监听(拖拽开始和完成时，会回调position的改变)和拖拽动效
          flowLayout.enableDragging(DragSupportSingleTypeFlowLayout.DraggingHelper,DragSupportSingleTypeFlowLayout.DefaultDraggingDecoration);
          //停止拖拽
          flowLayout.disableDragging();
          
* 在xml文件中可以通过 

          app:dragSupportSingleTypeFlowLayout_replaceDuration="500"设置长按开始拖拽所需时间(ms)


## SingleTypeExpandableVerticalLinearLayout
![image](https://github.com/OwnLeiX/OwnView/blob/master/exampleImages/SingleTypeExpandableVerticalLinearLayout1.png)
![image](https://github.com/OwnLeiX/OwnView/blob/master/exampleImages/SingleTypeExpandableVerticalLinearLayout2.png)
![image](https://github.com/OwnLeiX/OwnView/blob/master/exampleImages/SingleTypeExpandableVerticalLinearLayout3.png)
![image](https://github.com/OwnLeiX/OwnView/blob/master/exampleImages/SingleTypeExpandableVerticalLinearLayout4.png)
* 只支持单一item类型的可展开垂直线性布局。如果需要(比如存在于ListView中或是频繁刷新)，可以自己设置复用池。

          singleTypeExpandableVerticalLinearLayout.setSingleTypeViewRecyclePool(pool);
          singleTypeExpandableVerticalLinearLayout.setAdapter(adapter);
          singleTypeExpandableVerticalLinearLayout.shrink();
          singleTypeExpandableVerticalLinearLayout.expand();

* 在xml文件中可以通过

          app:expandableVerticalLinearLayout_shrinkItemCount="2"设置折叠时的行数
          app:expandableVerticalLinearLayout_hintTextGravity="right"设置提示文本的位置
          app:expandableVerticalLinearLayout_hintText="展开"设置提示文本
          app:expandableVerticalLinearLayout_hintTextColor="@color/colorPurple"设置提示文本的颜色
          app:expandableVerticalLinearLayout_hintTextGravity="right"设置提示文本的位置
          app:expandableVerticalLinearLayout_hintTextSize="14sp"设置提示文本的大小
          app:expandableVerticalLinearLayout_hintTextViewPaddingTop="5dp"设置提示文本的其他相关属性
          app:expandableVerticalLinearLayout_hintTextViewPaddingBottom="5dp"设置提示文本的其他相关属性
          app:expandableVerticalLinearLayout_hintTextViewPaddingLeft="10dp"设置提示文本的其他相关属性
          app:expandableVerticalLinearLayout_hintTextViewPaddingRight="10dp"设置提示文本的其他相关属性
          app:expandableVerticalLinearLayout_hintTextViewWidth="100dp"设置提示文本的其他相关属性
          app:expandableVerticalLinearLayout_hintTextViewHeight="20dp"设置提示文本的其他相关属性


## InfiniteRotateImageView
* 自动无限旋转的ImageView，适用于loading圈等物品，直接在xml里使用。

 
## SimpleCircleProgressBar
* 圆……呸，环形进度条，可以设置开口角度，开口位置，阴影
* 在xml文件中可以配置 

          <!-- 颜色 -->
          <attr name="simpleCircleProgressBar_color" format="color|reference" />
          <!-- 阴影颜色 -->
          <attr name="simpleCircleProgressBar_shadowColor" format="color|reference" />
          <!-- 阴影offsetX -->
          <attr name="simpleCircleProgressBar_shadowDx" format="dimension" />
          <!-- 阴影offsetY -->
          <attr name="simpleCircleProgressBar_shadowDy" format="dimension" />
          <!-- 宽度 -->
          <attr name="simpleCircleProgressBar_strokeWidth" format="dimension" />
          <!-- 顺时针 | 逆时针 -->
          <attr name="simpleCircleProgressBar_clockwise" format="boolean" />
          <!-- 开始角度 -->
          <attr name="simpleCircleProgressBar_startAngle" format="float" />
          <!-- 结束角度 -->
          <attr name="simpleCircleProgressBar_endAngle" format="float" />
          <!-- 满进度值 -->
          <attr name="simpleCircleProgressBar_max" format="integer" />
          <!-- 当前进度值 -->
          <attr name="simpleCircleProgressBar_progress" format="integer" />

## LyricView2
* 歌词控件
* 在xml文件中可以配置 

          <!-- 未走过进度的文字大小 -->
        <attr name="lyricView2_textSize" format="reference|dimension" />
          <!-- 未走过进度的文字颜色 -->
        <attr name="lyricView2_textColor" format="reference|color" />
          <!-- 已走过进度的文字大小 -->
        <attr name="lyricView2_usedTextColor" format="reference|color" />
          <!-- 未走过进度的文字是否加粗 -->
        <attr name="lyricView2_textBold" format="boolean" />
          <!-- 已走过进度的文字是否加粗 -->
        <attr name="lyricView2_usedTextBold" format="boolean" />
          <!-- 是否标记文字是否走过进度 -->
        <attr name="lyricView2_signUsedText" format="boolean" />
          <!-- 最大行数 -->
        <attr name="lyricView2_maxLines" format="integer" />
          <!-- 行间距 -->
        <attr name="lyricView2_linePadding" format="reference|dimension" />
          <!-- 安全边距校验(歌词到顶部或底部的时候不再滚动) -->
        <attr name="lyricView2_safeEdge" format="boolean" />
          <!-- 超边界绘制（不因为自身高度而限制歌词行数） -->
        <attr name="lyricView2_overDraw" format="boolean" />
          <!-- 展示完成 -->
        <attr name="lyricView2_showFinished" format="boolean" />
          <!-- 展示完成时展示的字符串 -->
        <attr name="lyricView2_finishedText" format="string|reference" />
          <!-- 展示无歌词 -->
        <attr name="lyricView2_emptyText" format="string|reference" />
          <!-- 换行滚动时长 -->
        <attr name="lyricView2_scrollDuration" format="string|reference" />
          <!-- 内容强行居中(歌词内容无法占满控件时) -->
        <attr name="lyricView2_contentForceCenter" format="boolean" />
          <!-- 当前正在进度的行对齐方式 -->
        <attr name="lyricView2_underwayLineGravity" format="enum">
            <enum name="top" value="1" />
            <enum name="center" value="2" />
            <enum name="bottom" value="3" />
        </attr>


## MuteLampView
* 静音灯控件
* 在xml文件中可以配置 

          <!-- 点缩放的最小半径 -->
        <attr name="muteLampView_minRadius" format="reference|dimension" />
          <!-- 点缩放的最大半径 -->
        <attr name="muteLampView_maxRadius" format="reference|dimension" />
          <!-- 点间距 -->
        <attr name="muteLampView_pointPadding" format="reference|dimension" />
          <!-- 点颜色 -->
        <attr name="muteLampView_color" format="reference|color" />
          <!-- 正已经进行过的点的颜色 -->
        <attr name="muteLampView_duringColor" format="reference|color" />
          <!-- 点的数量 -->
        <attr name="muteLampView_pointCount" format="integer" />
          <!-- 满进度值 -->
        <attr name="muteLampView_max" format="integer" />
          <!-- 进度值 -->
        <attr name="muteLampView_progress" format="integer" />
          <!-- 每个点延迟百分比动作(当前点共占用时长的百分比) -->
        <attr name="muteLampView_pointDelayPercent" format="float" />
          <!-- 最后是否要淡出 -->
        <attr name="muteLampView_fadeout" format="boolean" />
          <!-- 淡出时长 -->
        <attr name="muteLampView_fadeoutTime" format="integer" />


## LoopOverturnLayout
* 翻转布局，会轮流展示自己的子view(通过翻转动画切换)
* 在xml文件中可以配置 

          <!-- 翻转方向 -->
          <attr name="loopOverturnLayout_orientation" format="enum">
                    <enum name="horizontal" value="1" />
                    <enum name="vertical" value="2" />
          </attr>
          <!-- 停留时间 -->
          <attr name="loopOverturnLayout_displayDuration" format="integer" />
          <!-- 翻转时间 -->
          <attr name="loopOverturnLayout_overturnDuration" format="integer" />


## StateFrameLayout
* 状态帧布局，方便控制 loading failed empty succeed 等页面状态
* 在xml文件中可以配置 

          <!-- 失败样式layout -->
        <attr name="stateFrame_fail_layout" format="reference" />
          <!-- loading样式layout -->
        <attr name="stateFrame_loading_layout" format="reference" />
          <!-- 空样式layout -->
        <attr name="stateFrame_empty_layout" format="reference" />
          <!-- 初始化为什么样式 -->
        <attr name="stateFrame_init_state" format="enum">
            <enum name="loading" value="1" />
            <enum name="fail" value="2" />
            <enum name="success" value="4" />
            <enum name="empty" value="8" />
        </attr>
