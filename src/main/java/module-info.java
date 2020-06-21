module gomint.taglib {
    requires slf4j.api;
    requires lombok;
    requires io.netty.buffer;
    requires gomint.jni;

    exports io.gomint.taglib;
}