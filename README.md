# OwnView
自定义控件合集


## SingleTypeFlowLayout:
![image](https://github.com/OwnLeiX/OwnView/blob/master/exampleImages/SingleTypeFlowLayout.png)
![image](https://github.com/OwnLeiX/OwnView/blob/master/exampleImages/SingleTypeFlowLayout2.png)
只支持单一item类型的流式布局。如果需要(比如存在于ListView中或是频繁刷新)，可以自己设置复用池。

          flowLayout.setSingleTypeViewRecyclePool(pool);
          
          flowLayout.setAdapter(adapter);


在xml文件中可以通过 

          app:flowLayout_maxLines="3"设置最大行数
          
          app:flowLayout_horizontalPadding="10dp"设置行间距
          
          app:flowLayout_verticalPadding="5dp"设置列间距
