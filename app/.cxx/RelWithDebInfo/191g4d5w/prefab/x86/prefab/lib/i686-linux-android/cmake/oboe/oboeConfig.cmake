if(NOT TARGET oboe::oboe)
add_library(oboe::oboe SHARED IMPORTED)
set_target_properties(oboe::oboe PROPERTIES
    IMPORTED_LOCATION "/Users/gareth/.gradle/caches/transforms-3/0c0d8f4dcde507cc74daace5e8fb494b/transformed/jetified-oboe-1.7.0/prefab/modules/oboe/libs/android.x86/liboboe.so"
    INTERFACE_INCLUDE_DIRECTORIES "/Users/gareth/.gradle/caches/transforms-3/0c0d8f4dcde507cc74daace5e8fb494b/transformed/jetified-oboe-1.7.0/prefab/modules/oboe/include"
    INTERFACE_LINK_LIBRARIES ""
)
endif()

