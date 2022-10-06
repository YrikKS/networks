//
// Created by kurya on 02.10.2022.
//

#include "UsersDataCollection.h"

void UsersDataCollection::processingUser(int uniqueId, std::string ipAddress) {
    auto it = mapUsers_.find(uniqueId);
    if (it == mapUsers_.end()) {
        std::cout << "user: id = " << uniqueId << " ip = " << ipAddress << " connect" << std::endl;
        mapUsers_.insert(std::make_pair(uniqueId, UserData(ipAddress)));
        printCollection();
    } else {
        (*it).second.setLastUpdate(boost::posix_time::second_clock::local_time());
    }
}

UsersDataCollection::~UsersDataCollection() {
    mapUsers_.clear();
}

void UsersDataCollection::updateListUsers() {
    bool isUpdate = false;
    boost::posix_time::time_duration diff;
    for (auto it = mapUsers_.begin(); it != mapUsers_.end();) {
        diff = boost::posix_time::second_clock::local_time() - it->second.getLastUpdate();
        if (diff.abs().seconds() > 5) {
            std::cout << "user: id = " << it->first << " ip = " << it->second.getIpAddressUser() << " disconnected"
                      << std::endl;
            it = mapUsers_.erase(it);
            isUpdate = true;
        } else {
            it++;
        }
    }
    if (isUpdate) {
        printCollection();
    }
}

void UsersDataCollection::printCollection() {
    for (auto it = mapUsers_.begin(); it != mapUsers_.end(); it++) {
        std::cout << "id = " << (*it).first << " ipv6 = " << (*it).second.getIpAddressUser() << std::endl;
    }
}
