package ru.spb.trak.nifidistributedmapcacheserverbrowser.controllers;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.spb.trak.nifidistributedmapcacheserverbrowser.ProtocolVersionException;
import ru.spb.trak.nifidistributedmapcacheserverbrowser.model.Element;
import ru.spb.trak.nifidistributedmapcacheserverbrowser.model.ElementRepository;
import ru.spb.trak.nifidistributedmapcacheserverbrowser.model.Response;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;

@Slf4j
@RestController
@RequestMapping("/api")
public class CacheController {
    private ElementRepository elementRepository;

    public CacheController(ElementRepository elementRepository) {
        this.elementRepository = elementRepository;
    }

    @GetMapping("/keys")
    Response keys(@RequestParam(name = "host") String host,
                  @RequestParam(name = "port") int port,
                  @RequestParam(name = "page_size", required = false) int pageSize,
                  @RequestParam(name = "page_number", required = false) int pageNumber,
                  @RequestParam(name = "pattern", required = false) String pattern) throws UnknownHostException, IOException, ProtocolVersionException {
        if(pageSize <= 0 || pageNumber <= 0) {
            throw new IllegalArgumentException("invalid page size: " + pageSize + " or page number: " + pageNumber);
        }

        if (StringUtils.hasText(pattern)) {
            return elementRepository.findByPattern(host, port, pattern, pageSize, pageNumber);
        }
        return elementRepository.findAll(host, port, pageSize, pageNumber);
    }

}