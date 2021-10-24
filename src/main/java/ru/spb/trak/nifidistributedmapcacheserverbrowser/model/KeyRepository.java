package ru.spb.trak.nifidistributedmapcacheserverbrowser.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.Repository;
import ru.spb.trak.nifidistributedmapcacheserverbrowser.ProtocolVersionException;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;

public class KeyRepository implements  Repository<Key, Long> {

    private static final int ATTEMPTS = 20;
    private final Logger log = LoggerFactory.getLogger(KeyRepository.class);
    public Collection<Key> findAll(String host, int port) throws UnknownHostException, IOException, ProtocolVersionException {
        Collection<Key> result = new ArrayList<Key>();

        try (Socket socket = new Socket(host, port);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            int protocolVersion  = 1;
            output.write("NiFi".getBytes(StandardCharsets.UTF_8));
            output.writeInt(protocolVersion);
            output.flush();
            int status = input.readInt();
            int attempt = 0;
            while (21 == status) {
                attempt++;
                if (attempt > ATTEMPTS)
                    throw new ProtocolVersionException(String.format("Can't set protocol in %d attempts", ATTEMPTS));
                protocolVersion = input.readInt();
                output.writeInt(protocolVersion);
                output.flush();
                status = input.readInt();
            }
            output.writeUTF("keySet");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            output.writeInt(baos.size());
            baos.writeTo(output);
            output.flush();
            int keysCount = input.readInt();
            log.debug(String.format("Keys count is %d", keysCount));
            for (int i = 0; i<  keysCount; i++) {
                int keyNameSize = input.readInt();
                byte[] bytes = new byte[keyNameSize];
                input.readFully(bytes);
                Key key = new Key(new String(bytes));
                result.add(key);
                log.debug(String.format("Get key: %s", key));
            }

        }
        return result;
    }
}
