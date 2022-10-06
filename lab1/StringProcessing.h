//
// Created by kurya on 02.10.2022.
//

#ifndef TRY1_STRINGPROCESSING_H
#define TRY1_STRINGPROCESSING_H

#include <iostream>
#include <boost/regex.hpp>
#include <boost/array.hpp>

class StringProcessing {
public:
    static void separationString(const boost::array<char, 1024> buf, std::size_t read_bytes, std::string *readId,
                                 std::string *readIp) {
        std::string readData;
        auto it = buf.begin();
        for (int i = 0; i < read_bytes; it++, i++) {
            readData.push_back(*it);
        }
        int spacePosition = readData.find(' ');
        (*readId) = readData.substr(0, spacePosition - 1);
        (*readIp) = readData.substr(spacePosition + 1, readData.length() - 1);
    }
};

#endif //TRY1_STRINGPROCESSING_H
