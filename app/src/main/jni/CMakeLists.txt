cmake_minimum_required(VERSION 3.4.1)

set(ncnn_DIR ${CMAKE_SOURCE_DIR}/${ANDROID_ABI}/lib/cmake/ncnn)
find_package(ncnn REQUIRED)

set(ncnn_DIR ${CMAKE_SOURCE_DIR}/ncnn-20220729-android-vulkan/${ANDROID_ABI}/lib/cmake/ncnn)


add_library(yolov5ncnn SHARED
        yolov5ncnn_jni.cpp
        jni_interface.cpp
        YoloV5.cpp
        YoloV4.cpp
        NanoDet.cpp)

target_link_libraries(yolov5ncnn
        ncnn
#        yolov5
        jnigraphics
        )
