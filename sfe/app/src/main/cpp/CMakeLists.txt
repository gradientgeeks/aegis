cmake_minimum_required(VERSION 3.22.1)

project("aegis_keys")

add_library(
        aegis_keys
        SHARED
        native-lib.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        aegis_keys
        ${log-lib}
)