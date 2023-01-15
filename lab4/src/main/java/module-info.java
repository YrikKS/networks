module ru.nsu.org.main.lab4snake {
    requires javafx.fxml;
    requires javafx.controls;

    requires org.kordamp.bootstrapfx.core;
    requires lombok;
    requires com.google.protobuf;

    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    requires org.apache.commons.lang3;

    opens ru.nsu.org.main.lab4snake.view to javafx.fxml;
    exports ru.nsu.org.main.lab4snake.view;

    opens ru.nsu.org.main.lab4snake.controller to javafx.fxml;
    exports ru.nsu.org.main.lab4snake.controller;

    opens ru.nsu.org.main.lab4snake.model to javafx.fxml;
    exports ru.nsu.org.main.lab4snake.model;

    opens ru.nsu.org.main.lab4snake.backend.protoClass to com.google.protobuf;
    exports ru.nsu.org.main.lab4snake.backend.protoClass;

    opens ru.nsu.org.main.lab4snake to javafx.fxml;
    exports ru.nsu.org.main.lab4snake;

}