package ru.spb.trak.nifidistributedmapcacheserverbrowser.model;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;
import ru.spb.trak.nifidistributedmapcacheserverbrowser.ProtocolVersionException;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@Slf4j
@Component
public class ElementRepository implements Repository<Element, Long> {

    private static final int ATTEMPTS = 20;

    public Response findAll(String host, int port, int pageSize, int pageNumber) throws UnknownHostException, IOException, ProtocolVersionException {
        final Collection<Element> elements = new ArrayList<Element>();
        fillWithKeys(elements, host, port);
        fillValues(elements, host, port, ".*");
        Response response = new Response();
        response.setTotal(elements.size());
        response.setData(elements);
        return response;
    }

    public Response findByPattern(String host, int port, String pattern, int pageSize, int pageNumber) throws UnknownHostException, IOException, ProtocolVersionException {
        final Collection<Element> elements = new ArrayList<Element>();
        fillWithKeys(elements, host, port);
        fillValues(elements, host, port, ".*" + pattern + ".*");
        Response response = new Response();
        response.setTotal(elements.size());
        response.setData(elements);
        return response;
    }


    private void fillValues(Collection<Element> elements, final String host, final int port, final String pattern) throws PatternSyntaxException, UnknownHostException, IOException, ProtocolVersionException {
        Pattern key_pattern = Pattern.compile(pattern);
        List<Element> elementsForRemove = new ArrayList<Element>();
        elements.forEach(element -> {
            Matcher matcher = key_pattern.matcher(element.getKey());
            if (!matcher.matches()) {
                elementsForRemove.add(element);
            }
        });
        elements.removeAll(elementsForRemove);
        try (Socket socket = new Socket(host, port);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            setupProtocolVersion(input, output);
            for (Element element : elements) {
                byte[] key = element.getKey().getBytes(StandardCharsets.UTF_8);
                output.writeUTF("get");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                baos.write(key);
                output.writeInt(baos.size());
                baos.writeTo(output);
                output.flush();
                int valueSize = input.readInt();
                byte[] bytes = new byte[valueSize];
                input.readFully(bytes);
                element.setValue(new String(bytes));
            }
        }
    }

    private void fillWithKeys(Collection<Element> elements,final String host, final int port) throws UnknownHostException, IOException, ProtocolVersionException {
        try (Socket socket = new Socket(host, port);
             DataInputStream input = new DataInputStream(socket.getInputStream());
             DataOutputStream output = new DataOutputStream(socket.getOutputStream())) {
            setupProtocolVersion(input, output);
            output.writeUTF("keySet");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            output.writeInt(baos.size());
            baos.writeTo(output);
            output.flush();
            int keysCount = input.readInt();
            log.info(String.format("Keys count is %d", keysCount));
            for (int i = 0; i < keysCount; i++) {
                int keyNameSize = input.readInt();
                byte[] bytes = new byte[keyNameSize];
                input.readFully(bytes);
                Element element = new Element(new String(bytes));
                elements.add(element);
            }
        }
    }

    private void setupProtocolVersion(DataInputStream input, DataOutputStream output) throws IOException, ProtocolVersionException {
        int protocolVersion = 1;
        output.write("NiFi".getBytes(StandardCharsets.UTF_8));
        output.writeInt(protocolVersion);
        output.flush();
        int status = input.read();
        int attempt = 0;
        while (21 == status) {
            attempt++;
            if (attempt > ATTEMPTS)
                throw new ProtocolVersionException(String.format("Can't set protocol in %d attempts", ATTEMPTS));
            protocolVersion = input.read();
            output.writeInt(protocolVersion);
            output.flush();
            status = input.readInt();
        }
    }
}
