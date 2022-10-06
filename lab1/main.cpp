#include "UdpMulticast.h"
#include <boost/thread.hpp>
#include <boost/asio/ip/multicast.hpp>

using namespace boost::placeholders;
using namespace boost::asio;

int main(int argc, char **argv) {
    io_service ioService;
    udpMulticast::UdpMulticast creator(ioService, argc, argv);
    creator.initializeMulticastSocket();
    return 0;
}
