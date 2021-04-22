
LOCAL_PATH:=$(call my-dir)  #，返回当前文件在系统中的路径，Android.mk文件开始时必须定义该变量。

#表明清除上一次构建过程的所有全局变量，因为在一个Makefile编译脚本中，
#会使用大量的全局变量，使用这行脚本表明需要清除掉所有的全局变量
include$(CLEAR_VARS)

#LOCAL_MODULE，编译目标项目名，如果是so文件，则结果会以lib项目名.so呈现
LOCAL_MODULE := libMp3Encoder

#要编译的C或者Cpp的文件，注意这里不需要列举头文件，构建系统会自动帮助开发者依赖这些文件

MY_CPP_LIST := $(wildcard $(LOCAL_PATH)/*.cpp)
MY_CPP_LIST += $(wildcard $(LOCAL_PATH)/libmp3lame/*.cpp)
MY_CPP_LIST += $(wildcard $(LOCAL_PATH)/libmp3lame/*.c)

LOCAL_SRC_FILES := $(MY_CPP_LIST:$(LOCAL_PATH)/%=%)

#LOCAL_SRC_FILES:=mp3-encoder.cpp \
#				 libmp3lame/bitstream.c \
#				 libmp3lame/psymodel.c \
#				 libmp3lame/lame.c \
#				 libmp3lame/takehiro.c \
#				 libmp3lame/encoder.c \
#				 libmp3lame/quantize.c \
#				 libmp3lame/util.c \
#				 libmp3lame/fft.c \
#				 libmp3lame/quantize_pvt.c \
#				 libmp3lame/vbrquantize.c \
#				 libmp3lame/gain_analysis.c \
#				 libmp3lame/reservoir.c \
#				 libmp3lame/VbrTag.c \
#				 libmp3lame/mpglib_interface.c \
#				 libmp3lame/id3tag.c \
#				 libmp3lame/newmdct.c \
#				 libmp3lame/set_get.c \
#				 libmp3lame/version.c \
#				 libmp3lame/presets.c \
#				 libmp3lame/tables.c


#所依赖的NDK动态和静态库。
LOCAL_LDLIBS := -llog -ljnigraphics -lz -landroid -lm -pthread -L$(SYSROOT)/usr/lib

#Linclude $(BUILD_SHARED_LIBRARY)，构建动态库
include $(BUILD_SHARED_LIBRARY)



