#cmake_minimum_required(VERSION 3.4.1)
#
#project("jni")
#
#file(GLOB allCPP *.h *.c *.cpp)
#
#add_library(
#        jni
#        SHARED
#        ${allCPP})
#
#find_library(
#        log-lib
#        log)
#
#target_link_libraries(
#        jni
#        ${log-lib})


cmake_minimum_required(VERSION 3.4.1)
include_directories(libmp3lame)
aux_source_directory(libmp3lame SRC_LIST)

add_library(mp3encoder
        SHARED
        mp3-encoder.cpp ${SRC_LIST})

add_library( # Sets the name of the library.
        native-lib

        SHARED

        native-lib.cpp)


find_library( # Sets the name of the path variable.
        log-lib
        log)

target_link_libraries( # Specifies the target library.
        mp3encoder
        native-lib
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})