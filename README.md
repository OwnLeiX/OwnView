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
