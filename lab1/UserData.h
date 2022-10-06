//
// Created by kurya on 02.10.2022.
//

#ifndef FIRSTLAB_USERDATA_H
#define FIRSTLAB_USERDATA_H

#include <string>
#include <ctime>
#include <boost/date_time.hpp>

class UserData {
public:
    UserData(const std::string &ipAddressUser) : ipAddressUser_(ipAddressUser), lastUpdate_(boost::posix_time::second_clock::local_time()) {}

    const std::string &getIpAddressUser() const {
        return ipAddressUser_;
    }

    void setIpAddressUser(const std::string &ipAddressUser) {
        ipAddressUser_ = ipAddressUser;
    }

    boost::posix_time::ptime getLastUpdate() const {
        return lastUpdate_;
    }

    void setLastUpdate(boost::posix_time::ptime lastUpdate) {
        UserData::lastUpdate_ = lastUpdate;
    }

private:
    boost::posix_time::ptime lastUpdate_;
    std::string ipAddressUser_;
};


#endif //FIRSTLAB_USERDATA_H
