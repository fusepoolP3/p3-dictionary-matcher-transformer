package eu.fusepool.p3.transformer.dictionarymatcher;

import eu.fusepool.p3.transformer.HttpRequestEntity;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

/**
 *
 * @author Gabor
 */
public class Utils {

    /**
     * Get query parameters from a query string.
     *
     * @param queryString the query string
     * @return HashMap containing the query parameters
     * @throws java.io.UnsupportedEncodingException
     */
    protected static Map<String, String> getQueryParams(String queryString) throws ArrayIndexOutOfBoundsException, UnsupportedEncodingException {
        Map<String, String> temp = new HashMap<>();
        // query string should not be empty or blank
        if (StringUtils.isNotBlank(queryString)) {
            String[] params = queryString.split("&");
            String[] param;
            for (String item : params) {
                param = item.split("=", 2);
                temp.put(param[0], URLDecoder.decode(param[1], "UTF-8"));
            }
        }
        return temp;
    }

    /**
     * Get query parameter from a query string by name.
     *
     * @param queryString the query string
     * @param paramName the name of the parameter
     * @return HashMap containing the query parameters
     * @throws java.io.UnsupportedEncodingException
     */
    protected static String getQueryParam(String queryString, String paramName) throws ArrayIndexOutOfBoundsException, UnsupportedEncodingException {
        // query string should not be empty or blank
        if (StringUtils.isNotBlank(queryString)) {
            String[] params = queryString.split("&");
            String[] param;
            for (String item : params) {
                if (item.equals(paramName)) {
                    param = item.split("=", 2);
                    return URLDecoder.decode(param[1], "UTF-8");
                }
            }
        }
        return null;
    }

    /**
     * Get docuemt URI either from content location header, or generate one if it's null.
     *
     * @param entity
     * @return
     */
    protected static String getDocuementURI(HttpRequestEntity entity) {
        String documentURI;

        if (entity.getContentLocation() == null) {
            HttpServletRequest request = entity.getRequest();
            String baseURL = getBaseURL(request);
            String requestID = request.getHeader("X-Request-ID");

            if (StringUtils.isNotEmpty(requestID)) {
                documentURI = baseURL + requestID;
            } else {
                documentURI = baseURL + UUID.randomUUID().toString();
            }
        } else {
            documentURI = entity.getContentLocation().toString();
        }

        return documentURI;
    }

    /**
     * Returns the base URL.
     *
     * @param request
     * @return
     */
    protected static String getBaseURL(HttpServletRequest request) {
        if ((request.getServerPort() == 80) || (request.getServerPort() == 443)) {
            return request.getScheme() + "://" + request.getServerName() + "/";
        } else {
            return request.getScheme() + "://" + request.getServerName() + ":"
                    + request.getServerPort() + "/";
        }
    }

    /**
     * For testing purposes.
     *
     * @param request
     */
    public static void printHeaders(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {

            String headerName = headerNames.nextElement();
            System.out.print(headerName);

            Enumeration<String> headers = request.getHeaders(headerName);
            while (headers.hasMoreElements()) {
                String headerValue = headers.nextElement();
                System.out.println("\t" + headerValue);
            }
        }
    }

    /**
     * Check if URI is a valid URL.
     *
     * @param uriString
     * @return
     */
    protected static Boolean isURLValid(String uriString) {
        String[] schemes = {"http", "https"};
        UrlValidator urlValidator = new UrlValidator(schemes,UrlValidator.ALLOW_LOCAL_URLS);
        return urlValidator.isValid(uriString);
    }
}
