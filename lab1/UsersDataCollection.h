//
// Created by kurya on 02.10.2022.
//

#ifndef FIRSTLAB_USERSDATACOLLECTION_H
#define FIRSTLAB_USERSDATACOLLECTION_H

#include <map>
#include "UserData.h"
#include <iostream>
#include <utility>

class UsersDataCollection {
public:
    void processingUser(int uniqueId, std::string ipAddress);

    void updateListUsers();

    virtual ~UsersDataCollection();

    void printCollection();

private:
    std::map<int, UserData> mapUsers_;
};


#endif //FIRSTLAB_USERSDATACOLLECTION_H
