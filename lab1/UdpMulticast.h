//
// Created by kurya on 26.09.2022.
//

#ifndef TRY1_UDPMULTICAST_H
#define TRY1_UDPMULTICAST_H

#define STRING_TO_SEND boost::to_string(id_) + " " + localIpAddress_
#define SOCKET_PORT 6663
#define STOP_STRING "end"

#include "AnalyzerArgs.h"
#include "StringProcessing.h"
#include "GeneratorUniqueId.h"
#include "UsersDataCollection.h"

#include <iostream>
#include <boost/asio.hpp>
#include <boost/thread.hpp>
#include <boost/array.hpp>
#include <boost/asio/basic_socket.hpp>
#include <boost/asio/ip/multicast.hpp>

using namespace boost::asio;
namespace udpMulticast {
    class UdpMulticast {
    public:
        UdpMulticast(io_service &ioService, int argc, char **argv);

        void initializeMulticastSocket();

        void sendThread();

        void receiveThread();

        void stopThread();

    private:
        int id_;
        std::string localIpAddress_;
        ip::udp::socket udpMulticastSocket_;
        ip::udp::endpoint senderEndpoint_;
        ip::udp::endpoint receiverEndpoint_;
        boost::array<char, 1024> bufferReadingFromSocket_;

        UsersDataCollection usersDataCollection_;
        bool run = true;
    };
}
#endif //TRY1_UDPMULTICAST_H
