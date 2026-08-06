cmake_minimum_required(VERSION 3.22.1)
project(touchpad_audio LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(
    touchpad_audio
    SHARED
    jni_bridge.cpp
    audio_engine.cpp
    dsp/voice.cpp
    dsp/filter.cpp
    dsp/reverb.cpp
    dsp/chorus.cpp
    dsp/echo.cpp
)

find_package(oboe REQUIRED CONFIG)

target_include_directories(touchpad_audio PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})

target_link_options(
    touchpad_audio
    PRIVATE
    "-Wl,-z,max-page-size=16384"
)

target_link_libraries(
    touchpad_audio
    oboe::oboe
    android
    log
)
