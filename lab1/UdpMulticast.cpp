//
// Created by kurya on 26.09.2022.
//

#include "UdpMulticast.h"

namespace udpMulticast {
    void UdpMulticast::initializeMulticastSocket() {

        udpMulticastSocket_.open(receiverEndpoint_.protocol());
        udpMulticastSocket_.set_option(ip::udp::socket::reuse_address(true));
        udpMulticastSocket_.bind(receiverEndpoint_);
        udpMulticastSocket_.set_option(ip::multicast::join_group(senderEndpoint_.address()));

        boost::thread sendThreadFunc(&UdpMulticast::sendThread, this);
        boost::thread receiveThreadFunc(&UdpMulticast::receiveThread, this);
        stopThread();
        sendThreadFunc.join();
        receiveThreadFunc.join();
    }


    UdpMulticast::UdpMulticast(io_service &ioService, int argc, char **argv)
            : udpMulticastSocket_(ioService) {
        AnalyzerArgs analyzer;
        if (!analyzer.analyzeArgs(argc, argv)) {
            std::cerr << "Invalid arguments" << std::endl;
            exit(1);
        }

        receiverEndpoint_ = ip::udp::endpoint(ip::address::from_string(analyzer.getIpSocket()), 6663);
        senderEndpoint_ = ip::udp::endpoint(ip::address::from_string(analyzer.getIpMulticast()), 6663);

        id_ = GeneratorUniqueId::generateUniqId();
        ip::udp::resolver resolver(ioService);
        localIpAddress_ = resolver.resolve(ip::host_name(), "")->endpoint().address().to_string();
    }

    void UdpMulticast::sendThread() {
        while (run) {
            udpMulticastSocket_.send_to(buffer(STRING_TO_SEND), senderEndpoint_);
            boost::this_thread::sleep(boost::posix_time::seconds(1));
        }
    }

    void UdpMulticast::receiveThread() {
        while (run) {
            std::size_t read_bytes = udpMulticastSocket_.receive_from(buffer(bufferReadingFromSocket_),
                                                                      receiverEndpoint_);
            std::string readId;
            std::string readIp;
            StringProcessing::separationString(bufferReadingFromSocket_, read_bytes, &readId, &readIp);
            usersDataCollection_.processingUser(std::stoi(readId), readIp);
            usersDataCollection_.updateListUsers();
        }
    }


    void UdpMulticast::stopThread() {
        std::string readStatusWork;
        while (run) {
            std::cin >> readStatusWork;
            if (readStatusWork == STOP_STRING)
                run = false;
        }
    }
}


