//
// Created by kurya on 06.10.2022.
//

#ifndef FIRSTLAB_ANALYZERARGS_H
#define FIRSTLAB_ANALYZERARGS_H

#include <string>

namespace udpMulticast {
    class AnalyzerArgs {
    public:
        bool analyzeArgs(int argc, char **argv) {
            if (argc < 3 || argc > 4) {
                return false;
            } else {
                ipMulticast = argv[1];
                ipSocket = argv[2];
                return true;
            }
        }

        const std::string &getIpMulticast() const {
            return ipMulticast;
        }

        const std::string &getIpSocket() const {
            return ipSocket;
        }

    private:
        std::string ipMulticast;
        std::string ipSocket;
    };
}


#endif //FIRSTLAB_ANALYZERARGS_H
