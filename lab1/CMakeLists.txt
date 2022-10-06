cmake_minimum_required(VERSION 3.1)
project(firstLab)

set(CMAKE_CXX_STANDARD 11)
FIND_PACKAGE(Boost)

add_executable(lab1
        main.cpp
        UdpMulticast.cpp
        StringProcessing.h
        GeneratorUniqueId.h
        UsersDataCollection.cpp
        UserData.h
        AnalyzerArgs.h)

if (WIN32)
    set(BOOST_COMPONENTS ${BOOST_COMPONENTS} thread_win32)
endif ()

include_directories(${Boost_INCLUDE_DIRS})
target_link_libraries(lab1
        ${Boost_LIBRARIES}
        ${BOOST_ROOT}/libs/regex/src/regex.cpp
        ${BOOST_ROOT}/libs/regex/src/static_mutex.cpp
        ${BOOST_ROOT}/libs/thread/src/win32/thread.cpp
        )

if (WIN32)
    target_link_libraries(lab1 wsock32 ws2_32)
endif ()

install(TARGETS lab1 RUNTIME DESTINATION bin)
