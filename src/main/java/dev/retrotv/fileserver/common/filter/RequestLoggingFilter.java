package dev.retrotv.fileserver.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.retrotv.framework.foundation.common.util.IPUtils;
import dev.retrotv.framework.foundation.common.wrapper.RequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * HttpServletRequest/HttpServletResponse의 요청/응답을 로깅하는 필터
 */
public class RequestLoggingFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(wrappedRequest, wrappedResponse);
    }

    protected void doFilterWrapped(
        RequestWrapper request,
        ContentCachingResponseWrapper response,
        FilterChain filterChain
    ) throws IOException, ServletException {
        try {
            filterChain.doFilter(request, response);
            logRequest(request);
        } finally {
            logResponse(response);
            response.copyBodyToResponse();
        }
    }

    private static void logRequest(RequestWrapper request) throws IOException {
        String method = request.getMethod();
        String queryString = request.getQueryString();
               queryString = queryString == null ? request.getRequestURI() : request.getRequestURI() + queryString;
        String contentType = request.getContentType() == null ? "" : request.getContentType();
        String ipAddress = IPUtils.getIPAddr(request);

        log.debug(
            """
            ========================================================================================================
            | Method Type : {}
            | URI         : {}
            | Content-Type: {}
            | IP Address  : {}
            ========================================================================================================
            """
            , method
            , queryString
            , contentType
            , ipAddress
        );

        logPayload("Request", contentType, request.getInputStream());
    }

    private static void logResponse(ContentCachingResponseWrapper response) throws IOException {
        String contentType = response.getContentType() == null ? "" : response.getContentType();
        logPayload("Response", contentType, response.getContentInputStream());
    }

    private static void logPayload(String prefix, String contentType, InputStream inputStream) throws IOException {
        boolean visible = isVisible(
            MediaType.valueOf(
                contentType == null || contentType.isEmpty() ? "application/json" : contentType
            )
        );

        if (visible) {
            byte[] content = StreamUtils.copyToByteArray(inputStream);

            if (content.length > 0) {
                String contentString = new String(content);
                Object jsonObject = objectMapper.readValue(contentString, Object.class);
                String jsonContentString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);

                log.debug(
                     """
                     ========================================================================================================
                     {}
                     
                     Payload:
                     {}
                     ========================================================================================================
                     """
                   , prefix
                   , jsonContentString
                );
            }
        } else {
            log.debug(
                  """
                  {}
                  
                  Payload:
                  Binary Content
                  """
                , prefix
            );
        }
    }

    private static boolean isVisible(MediaType mediaType) {
        final List<MediaType> visibleTypes = Arrays.asList(
            MediaType.valueOf("text/*"),
            MediaType.APPLICATION_FORM_URLENCODED,
            MediaType.APPLICATION_JSON,
            MediaType.APPLICATION_XML,
            MediaType.valueOf("application/*+json"),
            MediaType.valueOf("application/*+xml"),
            MediaType.MULTIPART_FORM_DATA
        );

        return visibleTypes.stream()
                           .anyMatch(visibleType -> visibleType.includes(mediaType));
    }
}
