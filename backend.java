package servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;

/**
 * Servlet implementation class CameraEGPS
 */
public class CameraEGPS extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CameraEGPS() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String requestBody = getRequestBody(request);
		Gson gson = new Gson();
		JsonObject json = gson.fromJson(requestBody, JsonObject.class);
		String lat = json.get("latPoint").getAsString();
		String lng = json.get("lngPoint").getAsString();
		String b64 = json.get("Imagem").getAsString();
		System.out.println("B64: " +b64+ "\nLat: "+lat+"\nLng: "+lng + "\nHorário da requisição: "+LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm - dd/MM/yy")).toString());
	}

	public static String getRequestBody(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}
