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
import java.net.SocketException;
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
        Collection<Element> elements = new ArrayList<Element>();
        Response response = new Response();
        fillWithKeys(elements, host, port);
        response.setTotal(elements.size());
        log.info("time for paging " + elements.size());
        elements = makePagination(elements, pageSize, pageNumber);
        log.info("after paging " + elements.size());
        fillValues(elements, host, port, ".*");
        response.setData(elements);
        return response;
    }

    public Response findByPattern(String host, int port, String pattern, int pageSize, int pageNumber) throws UnknownHostException, IOException, ProtocolVersionException {
        Collection<Element> elements = new ArrayList<Element>();
        Response response = new Response();
        fillWithKeys(elements, host, port);
        filter(elements, pattern);
        response.setTotal(elements.size());
        log.info("time for paging " + elements.size());
        elements = makePagination(elements, pageSize, pageNumber);
        log.info("after paging " + elements.size());
        fillValues(elements, host, port, ".*" + pattern + ".*");
        response.setData(elements);
        return response;
    }


    private Collection<Element> makePagination(Collection<Element> elements, int pageSize, int pageNumber) {
        int fromIndex = (pageNumber - 1) * pageSize;
        if(elements == null || elements.size() <= fromIndex){
            return Collections.emptyList();
        } else {
            return  ((ArrayList<Element>)elements).subList(fromIndex, Math.min(fromIndex + pageSize, elements.size()));
        }
    }

    private void fillValues(Collection<Element> elements, final String host, final int port, final String pattern) throws PatternSyntaxException, UnknownHostException, IOException, ProtocolVersionException {
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

    private void filter(Collection<Element> elements, String pattern) {
        Pattern key_pattern = Pattern.compile(pattern);
        List<Element> elementsForRemove = new ArrayList<Element>();
        elements.forEach(element -> {
            Matcher matcher = key_pattern.matcher(element.getKey());
            if (!matcher.matches()) {
                elementsForRemove.add(element);
            }
        });
        elements.removeAll(elementsForRemove);
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
                log.info(" Taking key: " + i );
                try {
                int keyNameSize = input.readInt();
                byte[] bytes = new byte[keyNameSize];
                input.readFully(bytes);
                Element element = new Element(new String(bytes));
                elements.add(element);
                } catch (SocketException e) {
                    log.info("Looks like the key count has changed. Returning all we have got so far: ("+ (i-1) + ")");
                    return;
                }
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
