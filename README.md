# GreenLife
## 移动应用开发作业
本项目以android为前端，springboot为后端框架，MySQL为数据库进行前后端分离式开发，并将后端以及数据库部署在云端服务器上。此外本app融合了YOLOV5算法用以对垃圾进行目标检测利用opencv标记出垃圾的位置以及识别出的结果，搭载MobileNet3算法用以对昆虫进行分类识别，将安卓框架与AI算法融合，用户通过手机拍照或者手机相册上传照片后系统匹配相应算法进行识别并返回到前端安卓界面进行显示。

ps：本项目算法的数据集较小因此常常出现识别不准确的状况，对识别精度有要求的可以换成自己的.pt文件。

## 主页面的设置

三个模块主页、识别、历史都用fragment设置，且都是依附于mainActivity，默认展示的是主页页面通过replace()函数完成三个页面之间的fragment切换。

## Tipsfragment

使用recyclerView对环保小贴士进行展示

recyclerView：通过adapter设置recyclerView，可以减少缓存并且展示出后端数据，组成MVC格式。

## Identifyfragment

设置拍照上传和相册上传两种方式，相册上传就是调用系统相册进行选择；拍照上传是调用相机后将拍好的照片存在缓存空间中，然后再在identifyFragment中使用缓存中的照片，照片上传完毕后跳转到detailsActivity中进行具体处理。

## Historyfragment

同样通过recyclerView对数据进行展示，数据为从后端数据库获取的数据，通过adapter完成前后端分割并展示在前端。

## DetailsActivity

根据照片上传的信号选择是否读缓存中的照片（缓存中的照片会一直保存上一次拍照的结果），再通过identifyFragment传来的功能信号选择具体网络模型(YOLO or MobileNet)，识别结果通过adapter展示出来。

## LoginActivity and RegistActivity

都是通过简单的getText等获取写入的账号密码等数据，通过post请求传输到后端进行登录注册等功能...
