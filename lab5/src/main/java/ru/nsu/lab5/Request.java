package ru.nsu.lab5;

import java.nio.ByteBuffer;

public class Request {
    private final ByteBuffer buffer;
    private final OperationType type;

    public Request(ByteBuffer buffer, OperationType type) {
        this.buffer = buffer;
        this.type = type;
    }

    /* Check for valid request length */
    public boolean isRequest() {
        return switch (type) {
            case HELLO -> isHelloRequest();
            case HEADER -> isHeaderRequest();
            default -> true;
        };
    }

    private boolean isHelloRequest() {
        return buffer.position() > 1 && buffer.position() >= 2 + buffer.get(1);
    }

    private boolean isHeaderRequest() {
        if (buffer.position() < 5)
            return false;
        switch (buffer.get(3)) {
            case 0x01:
                if (buffer.position() != 10)
                    return false;
                break;
            case 0x04:
                if (buffer.position() != 22)
                    return false;
                break;
            case 0x03:
                if (buffer.position() != 7 + buffer.get(4))
                    return false;
        }
        return true;
    }

    /* Check request arguments for validity of processing it by our proxy */
    public boolean isValid() {
        return switch (type) {
            case HELLO -> isValidHello();
            case HEADER -> isValidHeader();
            default -> true;
        };
    }

    private boolean isValidHello() {
        return checkMethod();
    }

    private boolean isValidHeader() {
        if (buffer.get(1) != 0x01)
            return false;
        return buffer.get(3) != 0x04;
    }

    public boolean checkVersion() {
        return buffer.get(0) == 0x05;
    }

    /* Generate response for request */
    public byte[] getResponse() {
        return switch (type) {
            case HELLO -> getHelloResponse();
            case HEADER -> getHeaderResponse();
            default -> new byte[2];
        };
    }

    private byte[] getHelloResponse() {
        byte[] data = new byte[2];
        data[0] = 0x05;
        data[1] = checkMethod() ? 0x00 : (byte) 0xFF;
        return data;
    }

    private byte[] getHeaderResponse() {
        byte[] data = new byte[4 + buffer.get(4) + 3];
        buffer.get(data);
        if (data[1] != 0x01) {
            data[1] = 0x07;
            return data;
        }
        if (data[3] == 0x04) {
            data[1] = 0x08;
            return data;
        }
        data[1] = 0x00;

        return data;
    }

    /* Generate response with information about connection failure */
    public byte[] getDisconnectResponse() {
        byte[] data = new byte[4 + buffer.get(4) + 3];
        buffer.get(data);
        data[1] = 0x04;

        return data;
    }

    public byte getAddressType() {
        return buffer.get(3);
    }

    public byte[] getAddress() {
        switch (buffer.get(3)) {
            case 0x01 -> {
                byte[] data = new byte[buffer.get(4)];
                buffer.get(data, 4, 4);
                return data;
            }
            case 0x03 -> {
                byte[] data = new byte[buffer.get(4)];
                buffer.get(5,data, 0, buffer.get(4));
                return data;
            }
            default -> {
                return null;
            }
        }
    }

    public Short getPort() {
        switch (buffer.get(3)) {
            case 0x01 -> {
                byte[] data = new byte[2];
                buffer.get(data, 8, 2);
                return ByteBuffer.wrap(data).getShort();
            }
            case 0x03 -> {
                byte[] data = new byte[2];
                buffer.get(5 + buffer.get(4), data, 0, 2);
                return ByteBuffer.wrap(data).getShort();
            }
            default -> {
                return null;
            }
        }
    }

    private boolean checkMethod() {
        for (int i = 0; i < buffer.get(1); ++i) {
            if (0x00 == buffer.get(i + 2)) {
                return true;
            }
        }
        return false;
    }
}
