package fr.sie.brique.web.edition;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

public class Util {
	
	/**
	 * 
	 * @param httpServletRequest
	 * @return
	 * @throws IOException
	 */
    public static String getSpecFromPostBody(HttpServletRequest httpServletRequest) throws IOException {
        BufferedReader data = httpServletRequest.getReader();
        try {
            StringBuilder spec = new StringBuilder();
            String cur;
            while ((cur = data.readLine()) != null) {
                spec.append(cur).append("\n");
            }
            return spec.toString();
        } finally {
            if(data != null) {
                data.close();
            }
        }
    }
}
