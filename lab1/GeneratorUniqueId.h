//
// Created by kurya on 02.10.2022.
//

#ifndef TRY1_GENERATORUNIQUEID_H
#define TRY1_GENERATORUNIQUEID_H

#define NULL_TIME 0

#include <boost/random.hpp>
#include <ctime>

class GeneratorUniqueId {
public:
    static int generateUniqId() {
        boost::random::mt19937 generationInt{static_cast<std::uint32_t>(std::time(NULL_TIME))};
        return generationInt();
    }
};

#endif //TRY1_GENERATORUNIQUEID_H
