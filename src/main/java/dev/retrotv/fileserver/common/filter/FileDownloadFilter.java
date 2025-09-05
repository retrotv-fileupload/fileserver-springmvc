package dev.retrotv.fileserver.common.filter;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import dev.retrotv.fileserver.common.wrapper.GzipHttpServletResponseWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileDownloadFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/v1/files/download")) {
            GzipHttpServletResponseWrapper gzipResponse = new GzipHttpServletResponseWrapper(response);
            filterChain.doFilter(request, gzipResponse);

            // 정상 응답(200)일 때만 압축 적용
            if (gzipResponse.getStatus() == HttpServletResponse.SC_OK) {
                gzipResponse.setHeader("Content-Encoding", "gzip");
                gzipResponse.close();
            } else {
                filterChain.doFilter(request, response);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }
}
